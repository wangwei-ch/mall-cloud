package com.wangwei.mall.web.inner.service;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.web.inner.fallback.IItemDegradeService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "mallsystem-service-item",fallback = IItemDegradeService.class)
public interface IItemService {


    /**
     * @param skuId
     * @return
     */
    @GetMapping("/api/item/{skuId}")
    Result getItem(@PathVariable("skuId") Long skuId);

    //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
}
