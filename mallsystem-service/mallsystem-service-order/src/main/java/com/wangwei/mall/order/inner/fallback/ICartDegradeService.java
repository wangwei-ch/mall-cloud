package com.wangwei.mall.order.inner.fallback;

import com.wangwei.mall.model.cart.CartInfo;
import com.wangwei.mall.order.inner.service.ICartService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ICartDegradeService implements ICartService {

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        return null;
    }
}
