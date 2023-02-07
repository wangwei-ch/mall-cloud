package com.wangwei.mall.cart.service;

import com.wangwei.mall.model.cart.CartInfo;

import java.util.List;

public interface CartService {
    void addToCart(Long skuId, String userId, Integer skuNum);

    List<CartInfo> getCartList(String userId, String userTempId);
}
