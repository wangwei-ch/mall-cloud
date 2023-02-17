package com.wangwei.mall.common.config;



import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 1.非静态
     * 2.没有返回值 void
     * 3.时机：服务器加载serverlet是时加载，之执行一次
     *         在构造方法执行之后
     *
     */
    @PostConstruct
    public  void init(){

        this.rabbitTemplate.setConfirmCallback(this);
        this.rabbitTemplate.setReturnCallback(this);


    }


    /**
     *ConfirmCallback:之确认消息是否到达了交换机
     *   如果没有到达交换机 confirm 返回ack=false
     *   如果没有到达交换机 confirm 返回ack=true
     *
     * @param correlationData
     * @param ack  true false
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("消息发送成功：" + JSON.toJSONString(correlationData));
        } else {
            log.info("消息发送失败：" + cause + " 数据：" + JSON.toJSONString(correlationData));
        }

    }

    /**
     *
     * ReturnCallback: 消息没有正确到达交换机触发，正确到了不执行
     *
     * exchange --queue 不执行回调
     * exchange --X-- 执行回调
     *
     * @param message
     * @param replyCode 应答码
     * @param replyText 描述
     * @param exchange 交换机
     * @param routingKey 路由
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {

        // 反序列化对象输出
        System.out.println("消息主体: " + new String(message.getBody()));
        System.out.println("应答码: " + replyCode);
        System.out.println("描述：" + replyText);
        System.out.println("消息使用的交换器 exchange : " + exchange);
        System.out.println("消息使用的路由键 routing : " + routingKey);

        ///再次重试发送 处理业务
    }

}
