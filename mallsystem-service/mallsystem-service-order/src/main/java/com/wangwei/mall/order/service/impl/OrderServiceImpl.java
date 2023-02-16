package com.wangwei.mall.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangwei.mall.model.enums.OrderStatus;
import com.wangwei.mall.model.enums.ProcessStatus;
import com.wangwei.mall.model.order.OrderDetail;
import com.wangwei.mall.model.order.OrderInfo;
import com.wangwei.mall.order.mapper.OrderDetailMapper;
import com.wangwei.mall.order.mapper.OrderInfoMapper;
import com.wangwei.mall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {



    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;



    @Override
    @Transactional
    public Long saveOrderInfo(OrderInfo orderInfo) {

        //计算总价格
        orderInfo.sumTotalAmount();
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        String outTradeNo = "WANGWEI" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setCreateTime(new Date());

        // 定义为1天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());

        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        // 获取订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        StringBuffer tradeBody = new StringBuffer();
        for (OrderDetail orderDetail : orderDetailList) {
            tradeBody.append(orderDetail.getSkuName()+" ");
        }

        if (tradeBody.toString().length()>100){
            orderInfo.setTradeBody(tradeBody.toString().substring(0,100));
        }else {
            orderInfo.setTradeBody(tradeBody.toString());
        }

        orderInfoMapper.insert(orderInfo);

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
        }
        return orderInfo.getId();

    }



}
