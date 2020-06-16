package com.changgou.order.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    //交换机
    public static final String EX_ORDERPOINT ="ex_orderpoint";
    //发送积分消息
    public static final String QU_SETPOINT ="qu_setpoint";
    //删除积分消息
    public static final String QU_FINISHPOINT ="qu_finishpoint";
    //发送积分路由
    public static final String KEY_SETPOINT ="key_setpoint";
    //删除积分路由
    public static final String KEY_FINISHPOINT ="key_finishpoint";


    //支付完成操作订单
    public static final String QU_UPDATEORDER = "qu_updateorder";




    @Bean(QU_UPDATEORDER)
    public Queue QU_UPDATEORDER(){

        return QueueBuilder.durable(QU_UPDATEORDER).build();
    }



    @Bean(EX_ORDERPOINT)
    public Exchange EX_ORDERPOINT(){
        return ExchangeBuilder.directExchange(EX_ORDERPOINT).durable(true).build();
    }

    @Bean(QU_SETPOINT)
    public Queue QU_SETPOINT(){
        return QueueBuilder.durable(QU_SETPOINT).build();
    }

    @Bean(QU_FINISHPOINT)
    public Queue QU_FINISHPOINT(){
        return QueueBuilder.durable(QU_FINISHPOINT).build();
    }

    @Bean
    public Binding SETPOINT_EX_ORDERPOINT(@Qualifier(QU_SETPOINT) Queue queue, @Qualifier(EX_ORDERPOINT) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(KEY_SETPOINT).noargs();
    }


    @Bean
    public Binding FINISHPOINT_EX_ORDERPOINT(@Qualifier(QU_FINISHPOINT) Queue queue, @Qualifier(EX_ORDERPOINT) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(KEY_FINISHPOINT).noargs();
    }
}
