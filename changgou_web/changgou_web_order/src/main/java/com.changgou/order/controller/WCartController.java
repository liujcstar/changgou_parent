package com.changgou.order.controller;


import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.feign.CartFeign;
import com.changgou.order.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@RequestMapping("/wcart")
@Controller
public class WCartController {

    @Autowired
    private CartFeign cartFeign;

    @GetMapping("/list")
    public String list(Model model){

        Map map = cartFeign.list().getData();

        model.addAttribute("items",map);

        return "cart";
    }


    @GetMapping("/addCart")
    @ResponseBody
    public Result<Map> addCard(String skuId,Integer num){

        cartFeign.addCard(skuId,num);

        Map map = cartFeign.list().getData();

        return new Result(true, StatusCode.OK,"添加成功",map);
    }

    @GetMapping("/refCart")
    public Result<Map> refCart(String skuId,Integer num){

        Integer refNum = 0;

        Map map = cartFeign.list().getData();
        List<OrderItem> orderList = (List<OrderItem>) map.get("orderItemList");
        for (OrderItem orderItem : orderList) {
            if (skuId.equals(orderItem.getSkuId())){
                refNum = num - orderItem.getNum() ;
            }
        }

        cartFeign.addCard(skuId,refNum );

        Map data = cartFeign.list().getData();


        return  new Result<>(true,StatusCode.OK,"刷新成功",data);
    }

}
