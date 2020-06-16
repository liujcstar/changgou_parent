package com.changgou;

import com.changgou.interceptor.FeignInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.changgou.user.dao"})
@EnableFeignClients(basePackages = {"com.changgou.order.feign"})
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run( UserApplication.class);
    }


    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }
}
