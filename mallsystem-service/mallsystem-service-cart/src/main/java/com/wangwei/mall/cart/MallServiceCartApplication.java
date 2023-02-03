package com.wangwei.mall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = "com.wangwei.mall")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.wangwei.mall")
public class MallServiceCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallServiceCartApplication.class,args);

    }
}
