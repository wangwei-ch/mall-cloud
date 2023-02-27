package com.wangwei.mall.activity.receiver;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import com.wangwei.mall.activity.mapper.SecKillGoodsMapper;
import com.wangwei.mall.common.constant.MqConst;
import com.wangwei.mall.common.constant.RedisConst;
import com.wangwei.mall.common.util.DateUtil;
import com.wangwei.mall.model.activity.SeckillGoods;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class SecKillReceiver {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SecKillGoodsMapper seckillGoodsMapper;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_1,durable = "ture",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_1}))
    public void importToRedis(Message message, Channel channel){

        try {
            QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
            seckillGoodsQueryWrapper.eq("status", 1).gt("stock_count", 0);
            seckillGoodsQueryWrapper.eq("DATE_FORMAT(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));

            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(seckillGoodsQueryWrapper);


            for (SeckillGoods seckillGoods : seckillGoodsList) {

                Boolean flag = redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).hasKey(seckillGoods.getSkuId().toString());
                if (flag){
                    continue;
                }

                redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(seckillGoods.getSkuId().toString(),seckillGoods);

                //  将每个商品对应的库存剩余数，放入redis-list 集合中！
                for (Integer i = 0; i < seckillGoods.getStockCount(); i++) {
                    //  放入list  key = seckill:stock:skuId;
                    String key = RedisConst.SECKILL_STOCK_PREFIX+seckillGoods.getSkuId();
                    redisTemplate.opsForList().leftPush(key,seckillGoods.getSkuId().toString());
                    //  redisTemplate.boundListOps(key).leftPush(seckillGoods.getSkuId());
                }

                redisTemplate.convertAndSend("seckillpush",seckillGoods.getSkuId()+":1");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

    }

}
