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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
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
    @GetMapping("callback/return")
    public String callBack() {
        // 同步回调给用户展示信息
        return "redirect:" + AlipayConfig.return_order_url;
    }


    //  异步请求： http://rjsh38.natappfree.cc/api/payment/alipay/callback/notify 需要做内网穿透：支付宝主动发起的！
    //  https: //商家网站通知地址?voucher_detail_list=[{"amount":"0.20","merchantContribute":"0.00","name":"5折券","otherContribute":"0.20","type":"ALIPAY_DISCOUNT_VOUCHER","voucherId":"2016101200073002586200003BQ4"}]&fund_bill_list=[{"amount":"0.80","fundChannel":"ALIPAYACCOUNT"},{"amount":"0.20","fundChannel":"MDISCOUNT"}]&subject=PC网站支付交易&trade_no=2016101221001004580200203978&gmt_create=2016-10-12 21:36:12&notify_type=trade_status_sync&total_amount=1.00&out_trade_no=mobile_rdm862016-10-12213600&invoice_amount=0.80&seller_id=2088201909970555&notify_time=2016-10-12 21:41:23&trade_status=TRADE_SUCCESS&gmt_payment=2016-10-12 21:37:19&receipt_amount=0.80&passback_params=passback_params123&buyer_id=2088102114562585&app_id=2016092101248425&notify_id=7676a2e1e4e737cff30015c4b7b55e3kh6& sign_type=RSA2&buyer_pay_amount=0.80&sign=***&point_amount=0.00
    @PostMapping("/callback/notify")
    @ResponseBody
//    @PostMapping("/callback/notify")
    public String callBackNotify(@RequestParam Map<String,String> paramsMap) throws AlipayApiException {

        //使用 RSA 的验签方法
        boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名


        //获取异步回调参数 --支付宝传递的
        //out_trade_no
        String outTradeNo = paramsMap.get("out_trade_no");
        //total_amount
        String totalAmount = paramsMap.get("total_amount");
        //app_id
        String appId = paramsMap.get("app_id");
        //获取交易状态
        String tradeStatus = paramsMap.get("trade_status");

        //获取notify_id
        String notifyId = paramsMap.get("notify_id");

        //查询支付详情
        PaymentInfo paymentInfo=this.paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());


        try {
            if(signVerified){
                // TODO 验签成功后，按照支付结果异步通知中的描述，
                //  对支付结果中的业务内容进行二次校验，
                //  校验成功后在response中返回success并继续商户自身业务处理，
                //  校验失败返回failure

                //paymentInfo.getTotalAmount()
                //进行业务验证
                if(paymentInfo==null ||new BigDecimal("0.01").compareTo(new BigDecimal(totalAmount))!=0||
                        !app_id.equals(appId)){

                    return "failure";
                }

                //幂等性处理 setnx
                Boolean flag = redisTemplate.opsForValue().setIfAbsent(notifyId, notifyId, 1462, TimeUnit.MINUTES);

                //判断 false
                if(!flag){

                    return "failure";
                }

                //判断
                if("TRADE_SUCCESS".equals(tradeStatus)||"TRADE_FINISHED".equals(tradeStatus)){

                    //修改订单状态

                    this.paymentService.paySuccess(outTradeNo, PaymentType.ALIPAY.name(),paramsMap);


                    return "success";
                }


            }
        } catch (Exception e) {
            //如果出现异常，进行记录日志，后期进行追踪。
            e.printStackTrace();
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
