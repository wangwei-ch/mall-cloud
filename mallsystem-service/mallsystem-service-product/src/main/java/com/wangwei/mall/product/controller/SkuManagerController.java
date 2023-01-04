package com.wangwei.mall.product.controller;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.model.product.SkuInfo;
import com.wangwei.mall.model.product.SpuImage;
import com.wangwei.mall.product.service.ManagerService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("SKU")
@Slf4j
@RestController
@RequestMapping("admin/product")
public class SkuManagerController {

    @Autowired
    private ManagerService manageService;

    /**
     * 根据spuId 查询spuImageList
     * @param spuId
     * @return
     */
    @GetMapping("spuImageList/{spuId}")
    public Result<List<SpuImage>> getSpuImageList(@PathVariable("spuId") Long spuId) {
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }

    /**
     * 保存sku
     * @param skuInfo
     * @return
     */
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        // 调用服务层
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }
}
