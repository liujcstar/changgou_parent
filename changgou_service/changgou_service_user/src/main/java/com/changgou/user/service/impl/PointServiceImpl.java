package com.changgou.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.order.pojo.Task;
import com.changgou.user.config.RabbitMqConfig;
import com.changgou.user.dao.PointLogMapper;
import com.changgou.user.dao.UserMapper;
import com.changgou.user.pojo.PointLog;
import com.changgou.user.service.PointService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class PointServiceImpl implements PointService{

    @Autowired
    private PointLogMapper pointLogMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 添加积分
     * @return
     */
    @Override
    public int addPoint(Task task) {

        //解析内容
        String s = task.getRequestBody();
        if (StringUtils.isEmpty(s))
            return 0;

        Map map = JSON.parseObject(s, Map.class);
        String orderId = map.get("orderId").toString();
        String username = map.get("username").toString();
        int point  = (int) map.get("point");

        //1. 判断当前日志信息有没有存储进去,存储进入之后排除幂等性
        PointLog pointLog = pointLogMapper.selectByPrimaryKey(orderId);
        if (pointLog != null) {
            //并且发送消息删除任务表数据
            rabbitTemplate.convertAndSend(RabbitMqConfig.EX_ORDERPOINT, RabbitMqConfig.KEY_FINISHPOINT, JSON.toJSONString(task));
            return 0;
        }


        //0. 保存redis信息,并设置过期时间
        redisTemplate.boundValueOps(task.getId()).set(task.getId()+" exist",15, TimeUnit.SECONDS);

        //2. 添加积分信息
        int i = userMapper.updatePoint(username, point);
        if (i<=0)
            return 0;

//        int a =1/0;

        //3. 添加pointLog日志信息
        PointLog addPointLog = new PointLog();
        addPointLog.setOrder_id(orderId);
        addPointLog.setPoint(point);
        addPointLog.setUser_id(username);
        int pointResult = pointLogMapper.insertSelective(addPointLog);
        if (pointResult<=0)
            return 0;


//        int a =1/0;

        //4. 删除redis信息
        redisTemplate.delete(orderId);

        return 1;
    }
}
