package com.changgou.user.feign;

import com.changgou.entity.Result;
import com.changgou.user.pojo.Address;
import com.changgou.user.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "user")
public interface UserFeign {


    @GetMapping("/user/load/{username}")
    public Result<User> findUserById(@PathVariable("username") String username);


    @GetMapping("/address/addrList")
    public Result<List<Address>> addrList();
}
