package com.wangwei.mall.common.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wangwei.mall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class MallCacheAspect {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @SneakyThrows
    @Around("@annotation(com.wangwei.mall.common.cache.MallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint){

        //1. 声明一个对象
        Object obj = new Object();
        //2. 在环绕通知中处理业务逻辑{分布式锁}
        //  获取到注解,使用在方法上
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();


        //3. 获取到注解
        MallCache mallCache = signature.getMethod().getAnnotation(MallCache.class);
        // 获取注解上的前缀
        String prefix = mallCache.prefix();

        // 获取方法传入的参数
        Object[] args = joinPoint.getArgs();

        //  组成缓存的key 需要前缀+方法传入的参数
        String key = prefix+ Arrays.asList(args).toString();

        try {
            obj = cacheHit(key,signature);

            if (null == obj){
                //4. 没有从缓存中查询到数据,查询数据库,需要加锁
                String lockKey = prefix + ":lock";

                RLock lock = redissonClient.getLock(lockKey);
                boolean flag = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);

                //5. 尝试获取锁
                if (flag){
                    try {
                        //6. 执行方法体
                        obj = joinPoint.proceed(joinPoint.getArgs());
                        //判断有没有查到数据
                        if (null == obj){
                            //空对象也存入缓存
                            redisTemplate.opsForValue().set(key, JSON.toJSONString(obj),RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                            return obj;
                        }
                        redisTemplate.opsForValue().set(key, JSON.toJSONString(obj),RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        return obj;

                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }else {
                    //上锁失败 重新执行方法
                    Thread.sleep(1000);
                    return cacheAroundAdvice(joinPoint);
                }

            }else {
               //从缓存中查到数据
               return obj;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        //如果出现问题 数据库兜底
        return joinPoint.proceed(joinPoint.getArgs());
    }

    private Object cacheHit(String key, MethodSignature signature) {
        //从redis中获取数据
        String strJson = (String) redisTemplate.opsForValue().get(key);

        if (!StringUtils.isEmpty(strJson)){
            Class type = signature.getReturnType();
            return JSON.parseObject(strJson, type);
        }
        return null;
    }

}
