package com.wangwei.mall.payment.inner.fallback;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.model.order.OrderInfo;
import com.wangwei.mall.payment.inner.service.IOrderService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IOrderDegradeService implements IOrderService {
    @Override
    public Result<Map<String, Object>> trade() {
        return null;
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return null;
    }
}
