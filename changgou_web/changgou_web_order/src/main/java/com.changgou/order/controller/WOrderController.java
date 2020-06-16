package com.changgou.order.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.feign.CartFeign;
import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.user.feign.UserFeign;
import com.changgou.user.pojo.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/worder")
@Controller
public class WOrderController {


    @Autowired
    private UserFeign userFeign;

    @Autowired
    private CartFeign cartFeign;

    @GetMapping("/payPage")
    public String payPage(Model model) {


        //封装地址信息
        List<Address> addressList = userFeign.addrList().getData();
        model.addAttribute("address", addressList);


        Map map = cartFeign.list().getData();
        List<OrderItem> orderItemList = (List<OrderItem>) map.get("orderItemList");
        model.addAttribute("carts", orderItemList);
        model.addAttribute("totalMoney", map.get("totalMoney"));
        model.addAttribute("totalNum", map.get("totalNum"));

        //封装默认收货人地址
        for (Address address : addressList) {

            if ("1".equals(address.getIsDefault())) {
                model.addAttribute("deAddr", address);
            }

        }

        return "order";
    }


    @Autowired
    private OrderFeign orderFeign;


    //添加订单
    @PostMapping("/addOrder")
    @ResponseBody
    public Result addOrder(@RequestBody Order order) {
        Result result = orderFeign.add(order);
        return result;
    }


    //前往订单与支付页
    @GetMapping("/toPayPage")
    public String toPayPage(String orderId, Model model) {

        Order order = orderFeign.findById(orderId).getData();

        model.addAttribute("orderId", orderId);
        model.addAttribute("payMoney", order.getPayMoney());

        return "pay";

    }


    @GetMapping("/tosuccess/{payMoney}")
    public String toSuccess(@PathVariable("payMoney") String payMoney, Model model){

        model.addAttribute("payMoney",payMoney);

        return "paysuccess";
    }


    @GetMapping("/hello")
    @ResponseBody
    public String hello(){

        return "hello";
    }


}
