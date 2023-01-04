package com.wangwei.mall.web.inner.service;

import com.wangwei.mall.common.result.Result;

import com.wangwei.mall.web.inner.fallback.IProductDegradeService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "mallsystem-service-product",fallback = IProductDegradeService.class)
public interface IProductService {

    /**
     * 获取全部分类信息
     * @return
     */
    @GetMapping("/api/product/getBaseCategoryList")
    Result getBaseCategoryList();
}
