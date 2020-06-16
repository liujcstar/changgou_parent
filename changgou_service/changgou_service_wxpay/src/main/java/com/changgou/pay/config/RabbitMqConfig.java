package com.changgou.pay.config;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {


    public static final String QU_UPDATEORDER = "qu_updateorder";



    @Bean(QU_UPDATEORDER)
    public Queue QU_UPDATEORDER(){

        return QueueBuilder.durable(QU_UPDATEORDER).build();
    }



}
