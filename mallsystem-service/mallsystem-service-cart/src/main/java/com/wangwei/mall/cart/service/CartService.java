package com.wangwei.mall.cart.service;

import com.wangwei.mall.model.cart.CartInfo;

import java.util.List;

public interface CartService {
    void addToCart(Long skuId, String userId, Integer skuNum);

    List<CartInfo> getCartList(String userId, String userTempId);

    /**
     * 更新选中状态
     *
     * @param userId
     * @param isChecked
     * @param skuId
     */
    void checkCart(String userId, Integer isChecked, Long skuId);
}
