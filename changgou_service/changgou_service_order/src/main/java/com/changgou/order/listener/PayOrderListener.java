package com.changgou.order.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.config.RabbitMqConfig;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PayOrderListener {

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = RabbitMqConfig.QU_UPDATEORDER)
    public void payOrder(String message){

        Map map = JSON.parseObject(message, Map.class);
        if (map == null)
            throw new RuntimeException("监听到支付记录不能为空");

        String orderId = (String) map.get("orderId");
        String transaction_id = (String) map.get("transaction_id");

        //调用方法更新order订单
         orderService.payOrder(orderId,transaction_id);

    }

}
