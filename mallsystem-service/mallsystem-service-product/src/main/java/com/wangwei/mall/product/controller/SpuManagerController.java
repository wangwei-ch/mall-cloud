package com.wangwei.mall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.model.product.BaseSaleAttr;
import com.wangwei.mall.model.product.SpuInfo;
import com.wangwei.mall.model.product.SpuSaleAttr;
import com.wangwei.mall.product.service.ManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api("SPU管理")
@RestController
@RequestMapping("admin/product")
public class SpuManagerController {

    @Autowired
    private ManagerService manageService;

    /**
     * 查询spu商品属性 - 分页
     * @param page
     * @param size
     * @param spuInfo
     * @return
     */
    @ApiOperation("查询商品属性SPU-列表展示")
    @GetMapping("{page}/{size}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long size,
                                 SpuInfo spuInfo){

        //创建一个page对象
        Page<SpuInfo> spuInfoPage = new Page<>(page, size);
        //获取数据
        IPage<SpuInfo> spuInfoPageList = manageService.getSpuInfoPage(spuInfoPage,spuInfo);


        return Result.ok(spuInfoPageList);
    }

    @ApiOperation("新增SPU销售属性")
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){

        log.info("================进入新增SPU销售属性=====================");
        log.info("新增的spu为-----" + spuInfo.toString());
        if (null == spuInfo){
            log.error("spu信息为空,请重新上传");
            return Result.fail();
        }
        manageService.saveSpuInfo(spuInfo);
        return Result.ok();

    }

    @ApiOperation("获取销售属性集合")
    @GetMapping("baseSaleAttrList")
    public Result getBaseSaleAttrList(){
        List<BaseSaleAttr> list = manageService.getBaseSaleAttrList();
        return Result.ok(list);
    }


    /**
     * 根据spuId 查询销售属性集合
     * @param spuId
     * @return
     */
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result<List<SpuSaleAttr>> getSpuSaleAttrList(@PathVariable("spuId") Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }
}
