package com.changgou.oauth.service;


import com.changgou.oauth.util.AuthToken;

public interface LoginService {

    //发送消息到oauth2服务获取jwt令牌
    AuthToken login(String username, String password);
}
