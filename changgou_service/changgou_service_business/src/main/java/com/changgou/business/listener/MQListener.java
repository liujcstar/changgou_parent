package com.changgou.business.listener;


import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;

@Component
public class MQListener {

    @Autowired
    private RestTemplate restTemplate;

    @RabbitListener(queues = "ad_update_queue")
    public void canalServiceListener(String message){


        HashSet<String> hashSet = JSON.parseObject(message, HashSet.class);

        System.out.println(hashSet);

        for (String s : hashSet) {

            String forObject = restTemplate.getForObject("http://192.168.200.128/ad_update?position=" + s, String.class);
            System.out.println(forObject);

        }
    }


}
