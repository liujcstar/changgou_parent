package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Sku;
import com.changgou.order.pojo.OrderItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("goods")
public interface SkuFeign {

    @GetMapping("/sku/search/{spuId}")
    public List<Sku> findSkuBySpuId(@PathVariable("spuId")String spuId);

    @GetMapping("/sku/{id}")
    public Result<Sku> findById(@PathVariable("id") String id);

    @PostMapping("/sku/sellSku")
    public Result<Integer> sellSkuByOrderItem(@RequestBody OrderItem orderItem);

}
