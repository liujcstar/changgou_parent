package com.changgou.user.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.pojo.Task;
import com.changgou.user.config.RabbitMqConfig;
import com.changgou.user.service.PointService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
public class TaskListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PointService pointService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMqConfig.QU_SETPOINT)
    public void taskListener(String message){

        //解析内容
        Task task = JSON.parseObject(message, Task.class);


        //监听到消息后先判断redis中是否有数据（幂等性，消息发送过来还没消费完，防止二次发送，多次消费）
        String taskId = (String) redisTemplate.boundValueOps(task.getId()).get();
        if ( StringUtils.isNotEmpty(taskId))
            return;

        //调用本服务方法，查询数据库中是否添加了积分日志信息到积分日志表中

        int i = pointService.addPoint(task);
        if (i==0)
            return;

        rabbitTemplate.convertAndSend(RabbitMqConfig.EX_ORDERPOINT,RabbitMqConfig.KEY_FINISHPOINT,JSON.toJSONString(task));


    }

}
