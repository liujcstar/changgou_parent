package com.changgou.web.filter;


import com.changgou.web.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements GlobalFilter, Ordered {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AuthService authService;

    private static final String LOGIN_URL = "http://localhost:8001/api/oauth/loginPage";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();

        //如果是登录信息，放行
        if ("/api/oauth/login".equals(path) || !UrlFilter.hasAuthorize(path)) {
            return chain.filter(exchange);
        }


        //判断是否携带jti
        String jti = authService.getJTI(request, "tokenJti");
        if (StringUtils.isEmpty(jti)) {

            /* response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();*/
            return this.toLoginPage(LOGIN_URL + "?FROM=" + request.getURI().getPath(), exchange);
        }

        //判断redis中是否存在jwt
        String jwt = redisTemplate.boundValueOps(jti).get();
        if (StringUtils.isEmpty(jwt)) {
           /* response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();*/
            return this.toLoginPage(LOGIN_URL + "?FROM=" + path, exchange);
        }

        //增强请求头，放行
        request.mutate().header("Authorization", "Bearer " + jwt);


        return chain.filter(exchange);
    }

    //返回登录界面
    private Mono<Void> toLoginPage(String url, ServerWebExchange exchange) {

        ServerHttpResponse response = exchange.getResponse();
        //重定向状态码
        response.setStatusCode(HttpStatus.SEE_OTHER);

        response.getHeaders().set("Location", url);

        return response.setComplete();
    }

    //跳转到登录界面


    @Override
    public int getOrder() {
        return 0;
    }
}
