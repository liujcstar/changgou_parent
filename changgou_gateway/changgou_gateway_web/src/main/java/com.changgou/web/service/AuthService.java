package com.changgou.web.service;

import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {


    public String getJTI(ServerHttpRequest request, String tokenJti) {
        String jti = null;
        HttpCookie httpCookie = request.getCookies().getFirst(tokenJti);
        if (httpCookie!=null){
            jti = httpCookie.getValue();
        }
        return jti;
    }
}
