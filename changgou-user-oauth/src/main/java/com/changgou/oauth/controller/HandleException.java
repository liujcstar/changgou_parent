package com.changgou.oauth.controller;


import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class HandleException {

    @ExceptionHandler(value = Exception.class)
    public Result handle(Exception e){
        e.printStackTrace();
       return new Result(false, StatusCode.ERROR,"任务失败");
    }

    @ExceptionHandler(value = InvalidGrantException.class)
    public Result loginHandle(Exception e){
        e.printStackTrace();
        return new Result(false,StatusCode.ERROR,"没有登录权限");
    }

}
