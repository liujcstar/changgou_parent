package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;


    private static final String CART_USER = "cart_";

    @Autowired
    private RedisTemplate redisTemplate;

    //添加购物车
    @Override
    public void addCard(String skuId, Integer num ,String username) {
        Sku sku = skuFeign.findById(skuId).getData();



        //1. 如果购物车中有对应数据就更新数量
            //根据用户名和sku查询
        OrderItem orderItem = (OrderItem) redisTemplate.boundHashOps(CART_USER + username).get(skuId);

        if (orderItem!=null){
            //该用户购物车中已经存在该商品，增加商品数量，修改总价
            orderItem.setNum(orderItem.getNum()+num);
            orderItem.setMoney(orderItem.getNum()*orderItem.getPrice());
            orderItem.setWeight(orderItem.getWeight()+num*sku.getWeight());

        }else {
            //2. 如果没有就添加数据(使用hash的数据结构存储)
            orderItem = this.getOrderItem(sku,num);
        }

        //添加到redis中
        redisTemplate.boundHashOps(CART_USER+username).put(skuId,orderItem);

    }

    /**
     * 查询购物车列表
     * @param username
     * @return
     */
    @Override
    public Map list(String username) {
        Map map = new HashMap<String,Object>();

        List<OrderItem> orderItemList = redisTemplate.boundHashOps(CART_USER+username).values();
        if (orderItemList!=null){

            map.put("orderItemList", orderItemList);

            Integer totalNum = 0;
            Integer totalMoney = 0;

            for (OrderItem orderItem : orderItemList) {
                totalNum += orderItem.getNum();
                totalMoney +=orderItem.getMoney();
            }

            map.put("totalNum",totalNum );
            map.put("totalMoney",totalMoney );
        }

        return map;
    }



    //封装购物车数据
    private OrderItem getOrderItem(Sku sku, Integer num) {

        OrderItem orderItem = new OrderItem();

        //封装sku相关数据
        orderItem.setImage(sku.getImage());
        orderItem.setPrice(sku.getPrice());
        orderItem.setName(sku.getName());
        orderItem.setMoney(orderItem.getPrice()*num);
        orderItem.setPayMoney(orderItem.getMoney());
        orderItem.setSkuId(sku.getId());
        orderItem.setWeight(sku.getWeight());

        //封装spu相关数据
        Spu spu = spuFeign.findById(sku.getSpuId()).getData();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());

        //封装数量
        orderItem.setNum(num);

        return orderItem;

    }

}
