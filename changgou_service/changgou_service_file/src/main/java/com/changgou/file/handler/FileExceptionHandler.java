package com.changgou.file.handler;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class FileExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(){

        return new Result(false, StatusCode.OK,"系统繁忙，稍后再试");

    }

}
