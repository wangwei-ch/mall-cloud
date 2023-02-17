package com.wangwei.mall.mq.receiver;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConfirmReceiver {

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm",autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm",autoDelete = "ture"),
            key = {"routing.confirm"}))
    public void process(Message message, Channel channel){
        System.out.println("RabbitListener:"+new String(message.getBody()));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
