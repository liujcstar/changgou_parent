package com.itheima.canal.config;

import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Queue;

@Configuration
public class BusinessMQConfig {

    private final String AD_UPDATE_QUEUE = "AD_UPDATE_QUEUE"

    @Bean
    public Queue getQueue(){
        return QueueBuilder.durable()
    }

}

