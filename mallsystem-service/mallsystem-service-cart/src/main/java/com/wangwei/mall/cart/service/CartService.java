package com.wangwei.mall.cart.service;

public interface CartService {
    void addToCart(Long skuId, String userId, Integer skuNum);
}
