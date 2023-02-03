package com.wangwei.mall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
public class MallServiceGatewayApplication {
    public static void main(String[] args) {

        SpringApplication.run(MallServiceGatewayApplication.class,args);
    }
}
