package com.changgou.order.service;

import com.changgou.order.pojo.OrderItem;

import java.util.List;
import java.util.Map;

public interface CartService {

    void addCard(String skuId, Integer num,String username);

    Map list(String username);
}
