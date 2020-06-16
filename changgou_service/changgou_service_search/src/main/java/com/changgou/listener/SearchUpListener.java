package com.changgou.listener;


import com.changgou.config.BusinessMQConfig;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.service.SkuSearchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchUpListener {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuSearchService skuSearchService;

    @RabbitListener(queues = BusinessMQConfig.SEARCH_UP_QUEUE)
    public void searchUp(String spuId){

        //监听到数据库上架消息,发送消息查询哪些Sku需要进行上架

        List<Sku> skuBySpuId = skuFeign.findSkuBySpuId(spuId);

        //调用搜索服务方法，更新索引库中数据
        skuSearchService.importBySku(skuBySpuId);

    }

}
