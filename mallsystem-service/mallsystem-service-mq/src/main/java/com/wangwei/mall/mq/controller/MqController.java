package com.wangwei.mall.mq.controller;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mq")
public class MqController {


    @Autowired
    private RabbitService rabbitService;

    /**
     * 消息发送
     */
    @GetMapping("sendConfirm")
    public Result sendConfirm(){
        rabbitService.sendMessage("exchange.confirm", "routing.confirm", "加油华为,加油china！");
//        rabbitService.sendDelayMessage("exchange.confirm", "routing.confirm", "来人了，开始接客吧！",1);
        return Result.ok();
    }

}
