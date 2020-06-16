package com.changgou.order;

import com.changgou.interceptor.FeignInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableFeignClients({"com.changgou.order.feign","com.changgou.user.feign","com.changgou.pay.feign"})
@EnableEurekaClient
public class WCartApplication{

    public static void main(String[] args) {

        SpringApplication.run(WCartApplication.class,args);
    }


    @Bean
    public FeignInterceptor getFeignInterceptor(){
        return new FeignInterceptor();
    }
}
