package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.changgou.goods.feign"})
@EnableEurekaClient
public class PageApplication {
    public static void main(String[] args) {

        SpringApplication.run(PageApplication.class, args);

    }
}
