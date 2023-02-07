package com.wangwei.mall.web.inner.fallback;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.model.product.SkuInfo;
import com.wangwei.mall.web.inner.service.IProductService;
import org.springframework.stereotype.Component;


@Component
public class IProductDegradeService implements IProductService {
    @Override
    public Result getBaseCategoryList() {
        return null;
    }

    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return null;
    }
}
