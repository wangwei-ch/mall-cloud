package com.wangwei.mall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.wangwei.mall.model.enums.PaymentType;
import com.wangwei.mall.model.payment.PaymentInfo;
import com.wangwei.mall.payment.config.AlipayConfig;
import com.wangwei.mall.payment.service.AlipayService;
import com.wangwei.mall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/payment/alipay")
public class AlipayController {




    @Autowired
    private AlipayService alipayService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${app_id}")
    private String app_id;

    /**
     * 生成二维码
     * @param orderId
     * @return
     */
    @RequestMapping("submit/{orderId}")
    @ResponseBody
    public String submitOrder(@PathVariable Long orderId){
        String from = alipayService.createaliPay(orderId);
        return from;
    }


    /**
     * 支付宝回调
     * @return
     */
    @RequestMapping("callback/return")
    public String callBack() {
        // 同步回调给用户展示信息
        return "redirect:" + AlipayConfig.return_order_url;
    }


    //  异步请求： http://rjsh38.natappfree.cc/api/payment/alipay/callback/notify 需要做内网穿透：支付宝主动发起的！
    //  https: //商家网站通知地址?voucher_detail_list=[{"amount":"0.20","merchantContribute":"0.00","name":"5折券","otherContribute":"0.20","type":"ALIPAY_DISCOUNT_VOUCHER","voucherId":"2016101200073002586200003BQ4"}]&fund_bill_list=[{"amount":"0.80","fundChannel":"ALIPAYACCOUNT"},{"amount":"0.20","fundChannel":"MDISCOUNT"}]&subject=PC网站支付交易&trade_no=2016101221001004580200203978&gmt_create=2016-10-12 21:36:12&notify_type=trade_status_sync&total_amount=1.00&out_trade_no=mobile_rdm862016-10-12213600&invoice_amount=0.80&seller_id=2088201909970555&notify_time=2016-10-12 21:41:23&trade_status=TRADE_SUCCESS&gmt_payment=2016-10-12 21:37:19&receipt_amount=0.80&passback_params=passback_params123&buyer_id=2088102114562585&app_id=2016092101248425&notify_id=7676a2e1e4e737cff30015c4b7b55e3kh6& sign_type=RSA2&buyer_pay_amount=0.80&sign=***&point_amount=0.00
    @PostMapping("callback/notify")
    @ResponseBody
    public String callbackNotify(@RequestParam Map<String, String> paramsMap){
        System.out.println("死鬼你回来了....");
        //  Map<String, String> paramsMap = ... //将异步通知中收到的所有参数都存放到map中
        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //  获取到支付宝发送的参数：
        String outTradeNo = paramsMap.get("out_trade_no");
        //  获取到的数据0.01
        String totalAmount = paramsMap.get("total_amount");
        //  seller_id 暂时不比较！
        //  app_id
        String appId = paramsMap.get("app_id");
        //  获取交易状态
        String tradeStatus = paramsMap.get("trade_status");
        //  同一个请求notify_id 不变
        String notifyId = paramsMap.get("notify_id");
        if(signVerified){
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            //  校验是否一致：
            PaymentInfo paymentInfoQuery = this.paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());
            if (paymentInfoQuery==null || new BigDecimal("0.01").compareTo(new BigDecimal(totalAmount))!=0
                    || !appId.equals(app_id)){
                //  说明不一致：
                return "failure";
            }
            //  并且过滤重复的通知结果数据: setnx key value;
            Boolean result = this.redisTemplate.opsForValue().setIfAbsent(notifyId, "1", 24 * 60 + 22, TimeUnit.MINUTES);
            //  判断
            if (!result){
                return "failure";
            }
            //  判断交易状态：
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)){
                //  更新交易记录状态:paymentInfo - 目的对账！
                //  更新内容：payment_status callback_time callback_content trade_no
                //  订单状态后面使用 mq 解决。
                paymentService.paySuccess(outTradeNo, PaymentType.ALIPAY.name(),paramsMap);
                //  返回成功
                return "success";
            }
        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }

        return "failure";
    }


    //  发起退款请求：
    @RequestMapping("refund/{orderId}")
    @ResponseBody
    public Boolean refund(@PathVariable Long orderId){
        //  调用服务层方法
        Boolean flag = this.alipayService.refund(orderId);
        return flag;
    }



}
