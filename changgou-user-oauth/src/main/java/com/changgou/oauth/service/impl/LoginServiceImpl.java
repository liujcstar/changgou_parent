package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.LoginService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Value("${auth.ttl}")
    private Long ttl;

    @Override
    public AuthToken login(String username, String password) {


        //封装url
        ServiceInstance choose = loadBalancerClient.choose("user-auth");
        URI uri = choose.getUri();
        String url = uri + "/oauth/token";

        //封装BasedHttp头信息
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String baseHttp = this.getBaseHttp(clientId, clientSecret);
        headers.add("Authorization", baseHttp);

        //封装请求体
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);


        //401，400请求不处理，不会抛出这个异常导致程序失败
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if ((response.getRawStatusCode() != 400) && (response.getRawStatusCode() != 401)) {
                    super.handleError(response);
                }
            }
        });

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        Map map = exchange.getBody();

        //密码错误会返回一个map，但是map中存在key，但是值为null（400与401进行数据放行导致异常无法报出，会继续执行到这）
        if (map == null || map.get("access_token") == null || map.get("refresh_token") == null || map.get("jti") == null) {

            throw new RuntimeException("申请令牌失败");
        }

        AuthToken authToke = new AuthToken();
        authToke.setAccessToken(map.get("access_token") + "");
        authToke.setJti(map.get("jti") + "");
        authToke.setRefreshToken(map.get("refresh_token") + "");


        //将jwt存储到redis,并设置过期时间
        redisTemplate.boundValueOps(authToke.getJti()).set(authToke.getAccessToken(), ttl, TimeUnit.SECONDS);


        return authToke;
    }

    //basic http认证方式拼接
    private String getBaseHttp(String clientId, String clientSecret) {

        String baseHttp = clientId + ":" + clientSecret;
        byte[] encode = Base64Utils.encode(baseHttp.getBytes());
        return "Basic " + new String(encode);
    }
}
