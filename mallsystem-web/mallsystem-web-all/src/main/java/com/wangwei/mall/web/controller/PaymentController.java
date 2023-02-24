package com.wangwei.mall.web.controller;

import com.wangwei.mall.model.order.OrderInfo;
import com.wangwei.mall.web.inner.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PaymentController {

    @Autowired
    private IOrderService orderService;

    /**
     * 支付页
     * @param request
     * @return
     */
    @GetMapping("pay.html")
    public String success(HttpServletRequest request, Model model) {
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(Long.parseLong(orderId));
        model.addAttribute("orderInfo", orderInfo);
        return "payment/pay";
    }

    /**
     * 支付成功页
     * @return
     */
    @GetMapping("pay/success.html")
    public String success() {
        return "payment/success";
    }

}
