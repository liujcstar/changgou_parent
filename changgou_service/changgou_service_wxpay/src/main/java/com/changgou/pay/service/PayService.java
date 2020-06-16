package com.changgou.pay.service;

import java.util.Map;

public interface PayService {


    Map pay(String orderId) throws Exception;


    Map queryPay(String out_trade_no);
}
