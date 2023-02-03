package com.wangwei.mall.web.inner.fallback;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.web.inner.service.IItemService;
import org.springframework.stereotype.Component;

@Component
public class IItemDegradeService implements IItemService {

    @Override
    public Result getItem(Long skuId) {

        return null;
    }
}
