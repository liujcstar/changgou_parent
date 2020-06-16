package com.changgou.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.config.BusinessMQConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CanalEventListener
public class SkuListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ListenPoint(schema = "changgou_goods", table = "tb_spu")
    public void adUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {

        //更新后数据
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        Map<String, String> newMap = new HashMap<>();
        for (CanalEntry.Column column : afterColumnsList) {
            newMap.put(column.getName(), column.getValue());
        }

        //更新前数据
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();

        Map<String, String> oldMap = new HashMap<>();
        beforeColumnsList.forEach(c -> oldMap.put(c.getName(), c.getValue()));


        //商品上架
        if ("0".equals(oldMap.get("is_marketable")) && "1".equals(newMap.get("is_marketable"))) {

            rabbitTemplate.convertAndSend(BusinessMQConfig.SEARCH_UP_EXCHANGE,"search.up" , newMap.get("id"));
        }


        //商品下架
        if ("1".equals(oldMap.get("is_marketable")) && "0".equals(newMap.get("is_marketable"))) {

            rabbitTemplate.convertAndSend(BusinessMQConfig.SEARCH_UP_EXCHANGE,"search.down" , newMap.get("id"));
        }


        if ("0".equals(oldMap.get("status")) && "1".equals(newMap.get("status"))){
            rabbitTemplate.convertAndSend(BusinessMQConfig.SEARCH_UP_EXCHANGE,"up.upDetail" , newMap.get("id"));
        }

    }
}
