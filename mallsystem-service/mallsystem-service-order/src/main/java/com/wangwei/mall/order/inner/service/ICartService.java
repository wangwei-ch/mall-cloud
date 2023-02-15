package com.wangwei.mall.order.inner.service;

import com.wangwei.mall.model.cart.CartInfo;
import com.wangwei.mall.order.inner.fallback.ICartDegradeService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "mallsystem-service-cart",fallback = ICartDegradeService.class)
public interface ICartService {

    //  获取选中购物车列表！
    @GetMapping("/api/cart/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable String userId);
}
