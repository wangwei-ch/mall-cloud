package com.wangwei.mall.item.inner.service;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.item.inner.fallback.IListDegradeService;
import com.wangwei.mall.item.inner.fallback.IProductDegradeService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "mallsystem-service-list",fallback = IListDegradeService.class)
public interface IListService {

    /**
     * 更新商品incrHotScore
     *
     * @param skuId
     * @return
     */
    @GetMapping("inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable("skuId") Long skuId);
}
