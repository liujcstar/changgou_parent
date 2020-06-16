package com.changgou.listener;

import com.changgou.config.BusinessMQConfig;
import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Spu;
import com.changgou.service.PageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DetailListener {

    @Autowired
    private PageService pageService;


    @RabbitListener(queues = BusinessMQConfig.DETAIL_UP_QUEUE)
    public void detail_up(String spuId){
        pageService.createPage(spuId);
    }

}
