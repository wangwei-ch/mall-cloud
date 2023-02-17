package com.wangwei.mall.common.service;


import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     *发送普通确认消息
     * @param exchange
     * @param routingKey
     * @param message
     * @return
     */
    public boolean sendMessage(String exchange,String routingKey,Object message){

        this.rabbitTemplate.convertAndSend(exchange,routingKey,message);


        return true;
    }


    /**
     *
     * @param exchange
     * @param routingKey
     * @param message
     * @param delayTime 延迟时间
     * @return
     */
    public boolean sendDelayMessage(String exchange,String routingKey,Object message,int delayTime){

        this.rabbitTemplate.convertAndSend(exchange, routingKey, message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(delayTime*1000);

                return message ;
            }
        });

        return  true;
    }

}

