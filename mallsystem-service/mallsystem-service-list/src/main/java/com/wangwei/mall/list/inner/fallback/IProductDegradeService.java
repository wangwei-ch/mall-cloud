package com.wangwei.mall.list.inner.fallback;

import com.wangwei.mall.list.inner.service.IProductService;
import com.wangwei.mall.model.product.BaseAttrInfo;
import com.wangwei.mall.model.product.BaseCategoryView;
import com.wangwei.mall.model.product.BaseTrademark;
import com.wangwei.mall.model.product.SkuInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IProductDegradeService implements IProductService {

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return null;
    }

    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return null;
    }

    @Override
    public BaseTrademark getTrademark(Long tmId) {
        return null;
    }

    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        return null;
    }

}
