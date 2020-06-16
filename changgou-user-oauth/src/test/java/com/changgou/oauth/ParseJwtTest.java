package com.changgou.oauth;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

public class ParseJwtTest {

    @Test
    public void parseJwt() {
        //基于公钥去解析jwt
        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoiYmVpamluZyIsImNvbXBhbnkiOiJoZWltYSJ9.KOoAcJxis5zQhDtGNnrAmyPkinMtpdlTHnOYSk-0GPAqKFgMLrUUgZGXSa2nJBKeQ6GJ7GtBTboSDckMcu16hDxofrle1YRVQUqS7ofeMVHTw5zXusz5ljX68VGwtIZgnINNRqxkvl2jkOxADOrwciFDz-WXYTm9eVG3LP8AfbCnDPSozq8vuPznfEwbMVVetTVM3D_pvZJ_mRwd1MaxvgNkun-95o72TRKXUlNJ9gMlOOQXNZbqXQ583P2RiMnmE_7T1bB7aifNYD8Kgm1ItVPRyVscTsaVnBpF7LtojzmHAYVl_Ows9oS7QABezLvoeAu_-spWpc0qyn38HkAV3A";

        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr69vjafyIZie68qnLsoX5FOHOfalWRTcLXJx7UYgNhRtD2EapbkB8WEXhcnObhm/iTa8HtPBZT7HEXOIDbfSmN0Azlfj0dbcB8nNyPgxFi9PV3pBmziXgJo2wm2ZDPYDHB6p6kFGDOEcKSZIfcbWMfCYBR9eY/TsLr4hF41nGXzsNcz2a+9o1mk5rqMINYUiuoDDikiG+e09c2HGxXq5RQgfvfK2asqxZXZqS+7cqnqDIP0+zQfE6zPYbJMnxvjYHjRjrS8nFoJJfSCdZl6C9YqMZEpjR3ZuaanknUSw/nj/762afpd8NiquqJU0Gaj2623oM9rqzB7qvJgt12OsgQIDAQAB-----END PUBLIC KEY-----";
        Jwt token = JwtHelper.decodeAndVerify(jwt, new RsaVerifier(publicKey));

        String claims = token.getClaims();
        System.out.println(claims);
    }
}
