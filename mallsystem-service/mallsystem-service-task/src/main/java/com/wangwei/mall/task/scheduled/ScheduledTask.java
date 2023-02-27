package com.wangwei.mall.task.scheduled;


import com.wangwei.mall.common.constant.MqConst;
import com.wangwei.mall.common.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void task1() {
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,  MqConst.ROUTING_TASK_1, "");
    }
}
