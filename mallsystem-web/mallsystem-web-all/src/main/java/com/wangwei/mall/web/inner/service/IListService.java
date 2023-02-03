package com.wangwei.mall.web.inner.service;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.model.list.SearchParam;
import com.wangwei.mall.web.inner.fallback.IListDegradeService;
import com.wangwei.mall.web.inner.fallback.IProductDegradeService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "mallsystem-service-list",fallback = IListDegradeService.class)
public interface IListService {
    /**
     * 搜索商品
     * @param listParam
     * @return
     */
    @PostMapping("/api/list")
    Result list(@RequestBody SearchParam listParam);

    /**
     * 上架商品
     * @param skuId
     * @return
     */
    @GetMapping("/api/list/inner/upperGoods/{skuId}")
    Result upperGoods(@PathVariable("skuId") Long skuId);

    /**
     * 下架商品
     * @param skuId
     * @return
     */
    @GetMapping("/api/list/inner/lowerGoods/{skuId}")
    Result lowerGoods(@PathVariable("skuId") Long skuId);
}
