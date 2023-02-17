package com.wangwei.mall.web.controller;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.web.inner.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class OrderController {

    @Autowired
    private IOrderService orderService;

    /**
     * 确认订单
     * @param model
     * @return
     */
    @GetMapping("trade.html")
    public String trade(Model model) {
        Result<Map<String, Object>> result = orderService.trade();

        model.addAllAttributes(result.getData());
        return "order/trade";
    }

    /**
     * 我的订单
     * @return
     */
    @GetMapping("myOrder.html")
    public String myOrder() {
        return "order/myOrder";
    }
}
