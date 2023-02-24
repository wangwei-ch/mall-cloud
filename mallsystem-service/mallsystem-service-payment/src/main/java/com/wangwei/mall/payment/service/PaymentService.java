package com.wangwei.mall.payment.service;

import com.wangwei.mall.model.order.OrderInfo;
import com.wangwei.mall.model.payment.PaymentInfo;

import java.util.Map;

public interface PaymentService {

    /**
     * 保存交易记录
     * @param orderInfo
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */
    void savePaymentInfo(OrderInfo orderInfo, String paymentType);

    /**
     * 获取paymentInfo 对象
     * @param outTradeNo
     * @param name
     * @return
     */
    PaymentInfo getPaymentInfo(String outTradeNo, String name);

    /**
     * 支付成功更新交易记录方法
     * @param outTradeNo
     * @param name
     * @param paramMap
     */
    void paySuccess(String outTradeNo, String name, Map<String, String> paramMap);

    /**
     * 根据outTradeNo 支付方式name 更新数据
     * @param outTradeNo
     * @param name
     * @param paymentInfo
     */
    void updatePaymentInfo(String outTradeNo, String name, PaymentInfo paymentInfo);

}
