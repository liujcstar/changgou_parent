package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.pay.config.RabbitMqConfig;
import com.changgou.pay.service.PayService;
import com.changgou.util.ConvertUtils;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {


    @Autowired
    private PayService payService;


    @GetMapping("/pay/{orderId}/{money}")
    public Result<Map> pay(@PathVariable("orderId") String OrderId, @PathVariable("money") Integer money) {
        try {
            Map pay = payService.pay(OrderId);


            return new Result<>(true, StatusCode.OK, "发送成功", pay);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(false, StatusCode.OK, "发送失败");
        }
    }


    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 支付回调
     */
    @RequestMapping("/notify")
    public void notifyPay(HttpServletRequest request, HttpServletResponse response) {

        try {
            //微信传输的数据是以流的形式传递过来，先将流转为字符串
            String xml = ConvertUtils.convertToString(request.getInputStream());
            Map<String, String> map = WXPayUtil.xmlToMap(xml);

            System.out.println(map);

            //查询订单结果
            Map queryMap = payService.queryPay(map.get("out_trade_no"));

            if (queryMap == null)
                System.out.println(map.get("err_code_des"));

            //判断订单成功与失败的状态
            if (!queryMap.get("trade_state").equals("SUCCESS") || !queryMap.get("return_code").equals("SUCCESS"))
                System.out.println(map.get("err_code_des"));
                //中间可以再判断一次请求的域名是否来自微信，防止有人恶意攻击


            Map<String,String> orderMap = new HashMap();
            orderMap.put("transaction_id",map.get("transaction_id"));
            orderMap.put("orderId",map.get("out_trade_no") );

            //发送消息处理订单业务,修改订单支付状态
            rabbitTemplate.convertAndSend("", RabbitMqConfig.QU_UPDATEORDER, JSON.toJSONString(orderMap));

            //发送Stomp协议，推送支付通知
            rabbitTemplate.convertAndSend("paynotify","" ,orderMap.get("orderId"));

            response.setContentType("text/xml");
            String data = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
            response.getWriter().write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
