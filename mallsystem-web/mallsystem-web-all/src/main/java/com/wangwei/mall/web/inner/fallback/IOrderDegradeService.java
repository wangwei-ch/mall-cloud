package com.wangwei.mall.web.inner.fallback;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.web.inner.service.IOrderService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IOrderDegradeService implements IOrderService {

    @Override
    public Result<Map<String, Object>> trade() {
        return Result.fail();
    }
}
