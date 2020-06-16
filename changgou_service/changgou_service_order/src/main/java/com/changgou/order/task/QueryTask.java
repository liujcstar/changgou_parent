package com.changgou.order.task;

import com.alibaba.fastjson.JSON;
import com.changgou.order.config.RabbitMqConfig;
import com.changgou.order.pojo.Task;
import com.changgou.order.dao.TaskMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class QueryTask {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Scheduled(cron = "0/10 * * * * ?")
    public void queryTask(){

        //每隔一段时间就查询一次tb_task任务表，查看有没有任务没被消费(当前时间)
        List<Task> taskList = taskMapper.selectTasks(new Date());

        //如果没有任务就结束当前方法
        if (taskList == null || taskList.size() <= 0 )
            return;

        //有任务就调用mq发送消息到另外的服务
        for (Task task : taskList) {

            rabbitTemplate.convertAndSend(RabbitMqConfig.EX_ORDERPOINT,RabbitMqConfig.KEY_SETPOINT , JSON.toJSONString(task));

        }

    }


}
