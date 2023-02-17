package com.wangwei.mall.order.inner.service;

import com.wangwei.mall.order.inner.fallback.IProductDegradeService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(value = "mallsystem-service-product",fallback = IProductDegradeService.class)
public interface IProductService {


    /**
     * 获取sku最新价格
     *
     * @param skuId
     * @return
     */
    @GetMapping("/api/product/inner/getSkuPrice/{skuId}")
    BigDecimal getSkuPrice(@PathVariable(value = "skuId") Long skuId);
}
