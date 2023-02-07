package com.wangwei.mall.web.inner.service;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.web.inner.fallback.ICartDegradeService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@FeignClient(value = "mallsystem-service-cart",fallback = ICartDegradeService.class)
public interface ICartService {


    @RequestMapping("/api/cart/addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("skuNum") Integer skuNum,
                            HttpServletRequest request);
}
