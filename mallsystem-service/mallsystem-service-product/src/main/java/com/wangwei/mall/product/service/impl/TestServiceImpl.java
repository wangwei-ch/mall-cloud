package com.wangwei.mall.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.wangwei.mall.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 使用synchronized 本地锁
     */
//    @Override
    public synchronized void testLock1() {

        // 查询redis中的num值
        String value = (String)this.redisTemplate.opsForValue().get("num");
        // 没有该值return
        if (StringUtils.isBlank(value)){
            return ;
        }
        // 有值就转成成int
        int num = Integer.parseInt(value);
        // 把redis中的num值+1
        this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
    }


    /**
     * 基于redis实现分布式锁
     */
//    @Override
    public void testLock2(){

        //1. 从redis中获取锁
        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", "111");


        if (lock){
            //2. 查询redis中num的值
            String value = redisTemplate.opsForValue().get("num");

            // 没有该值return
            if (StringUtils.isBlank(value)){
                return ;
            }
            // 有值就转成成int
            int num = Integer.parseInt(value);
            // 把redis中的num值+1
            redisTemplate.opsForValue().set("num", String.valueOf(++num));

            //3. 释放锁
            this.redisTemplate.delete("lock");
        }else {
            try {
                //没有获取到锁,重试
                Thread.sleep(100);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Autowired
    private RedissonClient redissonClient;

    /**
     * 基于redisson实现分布式锁
     */
    @Override
    public void testLock(){


        //1. 创建锁
        String skuId = "22";
        String locKey = "lock:" + skuId;

        //2. 使用redisson客户端获取锁
        RLock lock = redissonClient.getLock(locKey);

        //3. 开始加锁
        lock.lock(10, TimeUnit.SECONDS);

        //4. 业务逻辑代码
        //5. 获取数据
        String value = redisTemplate.opsForValue().get("num");

        if (StringUtils.isBlank(value)){
            return;
        }
        // 将value 变为int
        int num = Integer.parseInt(value);
        // 将num +1 放入缓存
        redisTemplate.opsForValue().set("num",String.valueOf(++num));
        //6. 解锁
//        lock.unlock();



    }

}
