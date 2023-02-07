package com.wangwei.mall.web.inner.fallback;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.web.inner.service.ICartService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class ICartDegradeService implements ICartService {
    @Override
    public Result addToCart(Long skuId, Integer skuNum, HttpServletRequest request) {

        return null;
    }
}
