package com.wangwei.mall.item.inner.fallback;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.item.inner.service.IListService;
import org.springframework.stereotype.Component;

@Component
public class IListDegradeService implements IListService {
    @Override
    public Result incrHotScore(Long skuId) {
        return null;
    }
}
