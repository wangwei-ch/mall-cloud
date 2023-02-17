package com.wangwei.mall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        /**
         * 核心线程数
         * 拥有最多线程数
         * 表示空闲线程的存活时间
         * 存活时间单位
         * 用于缓存任务的阻塞队列
         * 省略：
         *  threadFactory：指定创建线程的工厂
         *  handler：表示当workQueue已满，且池中的线程数达到maximumPoolSize时，线程池拒绝添加新任务时采取的策略。
         */
        return new ThreadPoolExecutor(50,500,30, TimeUnit.SECONDS,new ArrayBlockingQueue<>(10000));

    }

}
