package com.wangwei.mall.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangwei.mall.model.enums.PaymentStatus;
import com.wangwei.mall.model.order.OrderInfo;
import com.wangwei.mall.model.payment.PaymentInfo;
import com.wangwei.mall.payment.mapper.paymentInfoMapper;
import com.wangwei.mall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private paymentInfoMapper paymentInfoMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void savePaymentInfo(OrderInfo orderInfo, String paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderInfo.getId());
        queryWrapper.eq("payment_type", paymentType);
        Integer count = paymentInfoMapper.selectCount(queryWrapper);
        if(count > 0) return;

        // 保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        //paymentInfo.setSubject("test");
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());

        paymentInfoMapper.insert(paymentInfo);
    }


    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo, String name) {
        //  select * from payment_info where out_trade_no = ? and payment_type = ?
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("out_trade_no",outTradeNo);
        paymentInfoQueryWrapper.eq("payment_type",name);
        return paymentInfoMapper.selectOne(paymentInfoQueryWrapper);
    }

    @Override
    public void paySuccess(String outTradeNo, String paymentType, Map<String, String> paramsMap) {

        //  根据outTradeNo，paymentType 查询
        PaymentInfo paymentInfoQuery = this.getPaymentInfo(outTradeNo, paymentType);
        if (paymentInfoQuery==null){
            return;
        }
        try {
            //  改造一下更新的方法！
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
            paymentInfo.setCallbackContent(paramsMap.toString());
            paymentInfo.setTradeNo(paramsMap.get("trade_no"));
            //  查询条件也可以作为更新条件！
            this.updatePaymentInfo(outTradeNo, paymentType, paymentInfo);
        } catch (Exception e) {
            //  删除key
            this.redisTemplate.delete(paramsMap.get("notify_id"));
            e.printStackTrace();
        }

    }

    //  更新交易状态记录！
    public void updatePaymentInfo(String outTradeNo, String name, PaymentInfo paymentInfo) {
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("out_trade_no",outTradeNo);
        paymentInfoQueryWrapper.eq("payment_type",name);
        paymentInfoMapper.update(paymentInfo,paymentInfoQueryWrapper);
    }
}
