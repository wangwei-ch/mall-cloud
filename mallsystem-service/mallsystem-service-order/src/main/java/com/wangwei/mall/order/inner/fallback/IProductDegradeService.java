package com.wangwei.mall.order.inner.fallback;

import com.wangwei.mall.order.inner.service.IProductService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class IProductDegradeService implements IProductService {
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        return null;
    }
}
