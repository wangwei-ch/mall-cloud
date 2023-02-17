package com.wangwei.mall.list.receiver;

import com.rabbitmq.client.Channel;
import com.wangwei.mall.common.constant.MqConst;
import com.wangwei.mall.list.service.SearchService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ListReceiver {

    @Autowired
    private SearchService searchService;

    //  开启消息监听 监听商品上架！
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_UPPER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = {MqConst.ROUTING_GOODS_UPPER}))
    public void upperGoodsToEs(Long skuId, Message message, Channel channel){

        try {
            //获取skuId，并判断
            if (null != skuId){
                //调用商品上架功能
                searchService.upperGoods(skuId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用es上架功能错误,请查看商品id{}是否正确",skuId);
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }



    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = {MqConst.ROUTING_GOODS_LOWER}))
    public void lowerGoodsToEs(Long skuId, Message message, Channel channel){

        try {
            if (null != skuId){
                searchService.lowerGoods(skuId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用es下架功能错误,请查看商品id{}是否正确",skuId);
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


}
