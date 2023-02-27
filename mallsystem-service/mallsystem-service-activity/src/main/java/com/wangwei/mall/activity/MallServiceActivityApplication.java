package com.wangwei.mall.activity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.wangwei.mall"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.wangwei.mall"})
public class MallServiceActivityApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallServiceActivityApplication.class, args);
    }
}
