package com.wangwei.mall.payment.service;

public interface AlipayService {


    String createaliPay(Long orderId);

    boolean refund(Long orderId);
}
