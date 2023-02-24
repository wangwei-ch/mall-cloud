package com.wangwei.mall.payment.inner.service;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.model.order.OrderInfo;

import com.wangwei.mall.payment.inner.fallback.IOrderDegradeService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(value = "mallsystem-service-order",fallback = IOrderDegradeService.class)
public interface IOrderService {
    @GetMapping("/api/order/auth/trade")
    Result<Map<String, Object>> trade();

    /**
     * 获取订单
     * @param orderId
     * @return
     */
    @GetMapping("/api/order/inner/getOrderInfo/{orderId}")
    OrderInfo getOrderInfo(@PathVariable(value = "orderId") Long orderId);
}
