package com.wangwei.mall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.wangwei.mall.model.enums.PaymentStatus;
import com.wangwei.mall.model.enums.PaymentType;
import com.wangwei.mall.model.order.OrderInfo;
import com.wangwei.mall.model.payment.PaymentInfo;
import com.wangwei.mall.payment.config.AlipayConfig;
import com.wangwei.mall.payment.inner.service.IOrderService;
import com.wangwei.mall.payment.service.AlipayService;
import com.wangwei.mall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@Service
public class AlipayServiceImpl implements AlipayService {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private AlipayClient alipayClient;

    @Override
    public String createaliPay(Long orderId) {

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        if ("PAID".equals(orderInfo.getOrderStatus()) || "CLOSED".equals(orderInfo.getOrderStatus())){
            return "该订单已经完成或已经关闭!";
        }
        //  调用保存交易记录方法！
        paymentService.savePaymentInfo(orderInfo, PaymentType.ALIPAY.name());

        //创建AlipayClient对象 注入到spring容器中
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest(); //创建api对应的request
        //同步回调 http://api.gmall.com/api/payment/alipay/callback/return
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);

        //异步回调
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        //  看api 各个字段解释说明：
        JSONObject bizContent = new JSONObject();
        //  使用第三方交易编号：作为商户的订单号，并没有直接使用orderId;
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        bizContent.put("total_amount", 0.01); //    单位：元  微信单位：分 3.0 | 2.0
        bizContent.put("subject", orderInfo.getTradeBody());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        //  绝对超时时间：  有具体年月日时分秒： 相对时间：timeout_expres 从现在开始20分钟之后
        //  设置一个绝对超时时间：
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //  +10m 后失效
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,10);
        bizContent.put("time_expire", simpleDateFormat.format(calendar.getTime()));
        //  bizContent.put("timeout_expres", "10m");
        alipayRequest.setBizContent(bizContent.toJSONString());
        //  二维码有过期时间：

        String form = "";

        try  {
            form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
        }  catch  (AlipayApiException e) {
            e.printStackTrace();
        }
        //  返回字符串:
        return form;
    }

    @Override
    public boolean refund(Long orderId) {

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderInfo.getOutTradeNo());
        map.put("refund_amount", orderInfo.getTotalAmount());
        map.put("refund_reason", "颜色浅了点");
        // out_request_no

        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            // 更新交易记录 ： 关闭
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus(PaymentStatus.CLOSED.name());
            paymentInfo.setUpdateTime(new Date());
            paymentService.updatePaymentInfo(orderInfo.getOutTradeNo(),PaymentType.ALIPAY.name(), paymentInfo);
            return true;
        } else {
            return false;
        }
    }


}
