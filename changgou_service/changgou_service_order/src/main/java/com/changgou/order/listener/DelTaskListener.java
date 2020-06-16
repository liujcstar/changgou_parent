package com.changgou.order.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.config.RabbitMqConfig;
import com.changgou.order.dao.TaskHisMapper;
import com.changgou.order.dao.TaskMapper;
import com.changgou.order.pojo.Task;
import com.changgou.order.pojo.TaskHis;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DelTaskListener {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskHisMapper taskHisMapper;

    /**
     * 监听积分事务是否完成
     * @param message
     */
    @RabbitListener(queues = RabbitMqConfig.QU_FINISHPOINT)
    public void delTaskListener(String message){

        Task task = JSON.parseObject(message, Task.class);

        if (task == null )
            return;

        //拷贝task、到taskHis
        TaskHis taskHis = new TaskHis();
        BeanUtils.copyProperties(task, taskHis);

        //增加taskHis历史任务表
        taskHis.setDeleteTime(new Date());
        taskHisMapper.insertSelective(taskHis);

        //根据id删除task
        task.setId(taskHis.getId());
        taskMapper.deleteByPrimaryKey(task.getId());

    }

}
