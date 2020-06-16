package com.changgou.oauth.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.oauth.service.LoginService;
import com.changgou.oauth.util.AuthToken;
import com.changgou.oauth.util.CookieUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.jws.WebParam;
import javax.servlet.http.HttpServletResponse;

@RequestMapping("/oauth")
@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;
    @Value("${auth.cookieDomain}")
    private String domain;

    @Value("${auth.cookieMaxAge}")
    private int maxAge;

    @PostMapping("/login")
    @ResponseBody
    public Result login( String username, String password, HttpServletResponse response) {

        if (StringUtils.isEmpty(username)){
            throw new RuntimeException("用户名不能为空");
        }

        if (StringUtils.isEmpty(password)){
            throw new RuntimeException("密码不能为空");
        }

        AuthToken authToken = loginService.login(username, password);

        //将数据存到cookie中，设置关闭浏览器就过期
        CookieUtil.addCookie(response,
                domain, //设置cookie可以跨域共享
                "/", //设置同一个域下所有请求都可以携带此Cookie
                "tokenJti",
                authToken.getJti(),
                maxAge, //最大过期时间，-1默认浏览器关闭就消失
                false);

        return new Result(true, StatusCode.OK,"登录成功",authToken);
    }


    @GetMapping("/loginPage")
    public String loginPage(@RequestParam(value = "FROM",required = false,defaultValue = "index") String from, Model model){

        model.addAttribute("from",from);

        return "login";
    }


}
