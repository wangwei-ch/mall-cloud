package com.wangwei.mall.order.receiver;

import com.rabbitmq.client.Channel;
import com.wangwei.mall.common.constant.MqConst;
import com.wangwei.mall.model.order.OrderInfo;
import com.wangwei.mall.order.service.OrderService;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderReceiver {


    @Autowired
    private OrderService orderService;


    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void cancelOrder(Long orderId, Message message, Channel channel){

        //判断当前订单id不能为空
        try {
            if (null != orderId){
                //发过来的是订单id，需要判断一下是否已经支付了
                //未支付的情况下 关闭订单
                OrderInfo orderInfo = orderService.getById(orderId);

                if (orderInfo != null && "UNPAID".equals(orderInfo.getOrderStatus()) && "UNPAID".equals(orderInfo.getProcessStatus())){
                    orderService.execExpiredOrder(orderId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //手动消息确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }


}
