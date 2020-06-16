package com.changgou.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.config.BusinessMQConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;

@CanalEventListener //标注这是一个canal监听类,当数据库日志发生改变执行该类
public class BusinessListener {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     *
     * @param eventType 监听事件对象
     * @param rowData 监听发生数据改变的行信息
     */
    @ListenPoint(schema = "changgou_business",table = "tb_ad")
    public void adUpdate(CanalEntry.EventType eventType,CanalEntry.RowData rowData){


        //获取到监听的表数据发生改变后的行数据
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        HashSet<String> hashSet = new HashSet<>();
        for (CanalEntry.Column column : afterColumnsList) {
            //获取位置信息
            if ("position".equals(column.getName())){
                //信息发生改变后，获取发生改变的图片位置字段，发送消息更新该位置图片
                hashSet.add(column.getValue());
            }
        }

        //获取到监听的表数据发生改变前的行数据
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : beforeColumnsList) {
            if ("position".equals(column.getName())){
                //信息发生改变后，获取发生改变的图片位置字段，发送消息更新该位置图片
                hashSet.add(column.getValue());
            }
        }

        rabbitTemplate.convertAndSend("", BusinessMQConfig.AD_UPDATE_QUEUE, JSON.toJSONString(hashSet) );


    }

}
