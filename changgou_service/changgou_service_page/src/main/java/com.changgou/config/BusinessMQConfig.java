package com.changgou.config;


import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BusinessMQConfig {

    public final static String AD_UPDATE_QUEUE = "ad_update_queue";

    public final static String SEARCH_UP_EXCHANGE = "search_up_exchange";

    public final static String SEARCH_UP_QUEUE = "search_up_queue";

    public final static String SEARCH_DOWN_QUEUE = "search_down_queue";

    public final static String DETAIL_UP_QUEUE = "detail_up_queue";



    @Bean
    public Queue getQueue(){
        return QueueBuilder.durable(AD_UPDATE_QUEUE).build();
    }


    @Bean(SEARCH_UP_EXCHANGE)
    public Exchange SEARCH_UP_EXCHANGE(){

        return ExchangeBuilder.topicExchange(SEARCH_UP_EXCHANGE).durable(true).build();

    }

    @Bean(SEARCH_DOWN_QUEUE)
    public Queue SEARCH_DOWN_QUEUE(){
        return QueueBuilder.durable(SEARCH_DOWN_QUEUE).build();
    }

    @Bean(SEARCH_UP_QUEUE)
    public Queue SEARCH_UP_QUEUE(){
        return QueueBuilder.durable(SEARCH_UP_QUEUE).build();
    }


    @Bean(DETAIL_UP_QUEUE)
    public Queue DETAIL_UP_QUEUE(){
        return QueueBuilder.durable(DETAIL_UP_QUEUE).build();
    }


    @Bean
    public Binding SEARCH_UP_BINDING(@Qualifier(SEARCH_UP_QUEUE) Queue queue,@Qualifier(SEARCH_UP_EXCHANGE)Exchange exchange ){
        return BindingBuilder.bind(queue).to(exchange).with("#.up").noargs();
    }

    @Bean
    public Binding SEARCH_DOWN_BINDING(@Qualifier(SEARCH_DOWN_QUEUE) Queue queue,@Qualifier(SEARCH_UP_EXCHANGE)Exchange exchange ){
        return BindingBuilder.bind(queue).to(exchange).with("#.down").noargs();
    }


    @Bean
    public Binding DETAIL_UP_BINDING(@Qualifier(DETAIL_UP_QUEUE) Queue queue,@Qualifier(SEARCH_UP_EXCHANGE)Exchange exchange ){
        return BindingBuilder.bind(queue).to(exchange).with("#.upDetail").noargs();
    }


}

