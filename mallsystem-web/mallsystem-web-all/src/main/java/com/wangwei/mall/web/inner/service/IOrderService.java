package com.wangwei.mall.web.inner.service;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.web.inner.fallback.IOrderDegradeService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(value = "mallsystem-service-order",fallback = IOrderDegradeService.class)
public interface IOrderService {
    @GetMapping("/api/order/auth/trade")
    Result<Map<String, Object>> trade();
}
