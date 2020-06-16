package com.changgou.pay.service.impl;

import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.pay.service.PayService;
import com.changgou.util.ConvertUtils;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class PayServiceImpl implements PayService {


    @Value("${wxpay.notify_url}")
    private String notify_url;

    @Autowired
    private WXPay wxPay;

    @Autowired
    private OrderFeign orderFeign;

    /**
     * 发送请求到微信支付
     *
     * @param orderId
     * @return
     */
    @Override
    public Map pay(String orderId) throws Exception {

        //查询金额
        Order order = orderFeign.findById(orderId).getData();
        Integer payMoney = order.getPayMoney();

        Map<String, String> map = new HashMap();
        map.put("out_trade_no", orderId);
        map.put("body", "安安销魂肠");
        map.put("notify_url", notify_url);
        map.put("spbill_create_ip", "127.0.0.1");

        BigDecimal bigDecimal = new BigDecimal("0.01");
        BigDecimal fen = bigDecimal.multiply(new BigDecimal("100"));
        fen = fen.setScale(0, BigDecimal.ROUND_UP);//向上取整
        map.put("total_fee", fen.toString());

        map.put("trade_type", "NATIVE");

        Map<String, String> resultMap = wxPay.unifiedOrder(map);

        for (String key : resultMap.keySet()) {
            System.out.println(key+": "+map.get(key));
        }

        return resultMap;
    }


    //查询微信支付结果
    @Override
    public Map queryPay(String out_trade_no) {

        Map map = new HashMap();

        map.put("out_trade_no", out_trade_no);
        Map queryMap = null;
        try {
            queryMap = wxPay.orderQuery(map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return queryMap;
    }


}
