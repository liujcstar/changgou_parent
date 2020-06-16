package com.changgou.oauth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;


@SpringBootTest
@RunWith(SpringRunner.class)
public class CodeGetJwt {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Test

    public void test(){

        //封装路径
        ServiceInstance serviceInstance = loadBalancerClient.choose("USER-AUTH");

        URI uri = serviceInstance.getUri();

        String url = uri + "/oauth/token";

        //封装BasedHttp信息
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String,String>();

        String baseHttp = getBaseHttp("changgou","changgou");
        headers.add("Authorization", baseHttp);

        //封装请求体信息
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", "itheima");
        body.add("password", "itheima");


        HttpEntity<MultiValueMap<String,String>> requesrEntity = new HttpEntity<MultiValueMap<String,String>>(body,headers);

        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, requesrEntity, Map.class);

        Map map = exchange.getBody();

        System.out.println(map);

    }

    private String getBaseHttp(String changgou, String changgoou) {

        String baseHttp = changgoou+":"+changgou;
        byte[] encode = Base64Utils.encode(baseHttp.getBytes());
        return "Basic "+ new String(encode);
    }

}
