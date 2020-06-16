package com.changgou.order.controller;

import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.pay.feign.PayFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping("/wxpay")
@Controller
public class PayController {


    /**
     *跳转wxpay页面
     * @param orderId
     * @param model
     * @return
     */

    @Autowired
    private PayFeign payFeign;

    @Autowired
    private OrderFeign orderFeign;

    @GetMapping
    public String wxPay(String orderId, Model model){

        if (orderId == null)
            throw new RuntimeException("订单号不能为空");

        Order order = orderFeign.findById(orderId).getData();
        if (order==null)
            return "fail";

        Map data = payFeign.pay(orderId,order.getPayMoney()).getData();

        if (data==null)
            return "fail";


        model.addAttribute("orderId",orderId);
        model.addAttribute("payMoney",order.getPayMoney());

        model.addAttribute("code_url",data.get("code_url"));

        return "wxpay";
    }


    @GetMapping("/tosuccess/{payMoney}")
    public String toSuccess(@PathVariable("payMoney") String payMoney, Model model){

        model.addAttribute("payMoney",payMoney);

        return "paysuccess";
    }


}
