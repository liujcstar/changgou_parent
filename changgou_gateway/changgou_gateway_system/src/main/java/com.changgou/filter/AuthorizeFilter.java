package com.changgou.filter;


import com.changgou.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizeFilter implements GlobalFilter,Ordered{
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //获取request

        ServerHttpRequest request = exchange.getRequest();

        //获取response
        ServerHttpResponse response = exchange.getResponse();

        //判断是否为登录资源，是登录资源进行放行
        if (request.getURI().getPath().contains("/admin/login")){
           return chain.filter(exchange);
        }

        //获取JWT请求头信息，判断jwt是否为空，如果为空就说明用户没有登录，返回401未通过认证信息
        HttpHeaders headers = request.getHeaders();
        String jwt = headers.getFirst("jwt");
        if (StringUtils.isEmpty(jwt)){

            //响应给用户401未通过认证信息状态码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //解析JWT判断JWT令牌是否正确

        try {
            Claims claims = JwtUtil.parseJWT(jwt);
            System.out.println(claims);
        } catch (Exception e) {

            e.printStackTrace();
            //出现异常，验证未通过，鉴权失败，返回401认证失败信息
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        return 1;
    }
}
