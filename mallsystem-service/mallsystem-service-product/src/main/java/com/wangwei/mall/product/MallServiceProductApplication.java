package com.wangwei.mall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.wangwei.mall"})
@EnableFeignClients(basePackages= {"com.wangwei.mall"})
public class MallServiceProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallServiceProductApplication.class, args);
    }
}
