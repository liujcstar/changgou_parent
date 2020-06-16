package com.changgou.pay.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient("pay")
public interface PayFeign {

    @GetMapping("/pay/pay/{orderId}/{money}")
    public Result<Map> pay(@PathVariable("orderId") String OrderId, @PathVariable("money") Integer money);
}
