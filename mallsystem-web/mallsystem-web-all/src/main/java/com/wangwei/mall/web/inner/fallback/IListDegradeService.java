package com.wangwei.mall.web.inner.fallback;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.model.list.SearchParam;
import com.wangwei.mall.web.inner.service.IListService;
import org.springframework.stereotype.Component;

@Component
public class IListDegradeService implements IListService {

    @Override
    public Result list(SearchParam searchParam) {
        return Result.fail();
    }

    @Override
    public Result upperGoods(Long skuId) {
        return null;
    }

    @Override
    public Result lowerGoods(Long skuId) {
        return null;
    }
}
