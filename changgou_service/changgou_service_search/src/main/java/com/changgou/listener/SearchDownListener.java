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
public class SearchDownListener {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuSearchService skuSearchService;

    @RabbitListener(queues = BusinessMQConfig.SEARCH_DOWN_QUEUE)
    public void searchDel(String spuId){

        List<Sku> skuBySpuId = skuFeign.findSkuBySpuId(spuId);

        skuSearchService.delBySkuList(skuBySpuId);

    }

}
