package com.wangwei.mall.mq.controller;

import com.wangwei.mall.common.config.DeadLetterConfig;
import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.common.service.RabbitService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/mq")
public class MqController {


    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 消息发送
     */
    @GetMapping("sendConfirm")
    public Result sendConfirm(){
        rabbitService.sendMessage("exchange.confirm", "routing.confirm", "加油华为,加油china！");
//        rabbitService.sendDelayMessage("exchange.confirm", "routing.confirm", "来人了，开始接客吧！",1);
        return Result.ok();
    }


    /**
     * 测试死信队列
     * @return
     */
    @GetMapping("sendDeadLetter")
    public Result sendDeadLetter() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.rabbitTemplate.convertAndSend(DeadLetterConfig.exchange_dead, DeadLetterConfig.routing_dead_1, "ok");
        System.out.println(sdf.format(new Date()) + " Delay sent.");
        return Result.ok();
    }

}
