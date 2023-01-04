package com.wangwei.mall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.model.product.*;
import com.wangwei.mall.product.service.ManagerService;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("商品基础属性接口")
@RestController
@RequestMapping("admin/product")
public class BaseManagerController {

    @Autowired
    private ManagerService managerService;


    /**
     * 查询所有的一级分类信息
     * @return
     */
    @GetMapping("getCategory1")
    public Result<List<BaseCategory1>> getCategory1() {
        List<BaseCategory1> baseCategory1List = managerService.getCategory1();
        return Result.ok(baseCategory1List);
    }

    /**
     * 根据一级分类Id 查询二级分类数据
     * @param category1Id
     * @return
     */
    @GetMapping("getCategory2/{category1Id}")
    public Result<List<BaseCategory2>> getCategory2(@PathVariable("category1Id") Long category1Id) {
        List<BaseCategory2> baseCategory2List = managerService.getCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }

    /**
     * 根据二级分类Id 查询三级分类数据
     * @param category2Id
     * @return
     */
    @GetMapping("getCategory3/{category2Id}")
    public Result<List<BaseCategory3>> getCategory3(@PathVariable("category2Id") Long category2Id) {
        List<BaseCategory3> baseCategory3List = managerService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }

    /**
     * 根据分类Id 获取平台属性数据
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result<List<BaseAttrInfo>> attrInfoList(@PathVariable("category1Id") Long category1Id,
                                                   @PathVariable("category2Id") Long category2Id,
                                                   @PathVariable("category3Id") Long category3Id) {
        List<BaseAttrInfo> baseAttrInfoList = managerService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(baseAttrInfoList);
    }

    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){

        if (ObjectUtils.isEmpty(baseAttrInfo)){
            return Result.fail().message("平台属性保存失败");
        }
        managerService.saveAttrInfo(baseAttrInfo);
        return Result.ok().message("平台属性保存成功");
    }


    /**
     * 数据回显
     * @param attrId
     * @return
     */
    @GetMapping("getAttrValueList/{attrId}")
    public Result<List<BaseAttrValue>> getAttrValueList(@PathVariable("attrId") Long attrId){
        BaseAttrInfo attrInfo = managerService.getAttrInfo(attrId);
        return Result.ok(attrInfo.getAttrValueList());
    }



    /**
     * SKU分页列表
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/list/{page}/{limit}")
    public Result index(
            @PathVariable Long page,
            @PathVariable Long limit) {

        Page<SkuInfo> pageParam = new Page<>(page, limit);
        IPage<SkuInfo> pageModel = managerService.getPage(pageParam);
        return Result.ok(pageModel);
    }

    /**
     * 商品上架
     * @param skuId
     * @return
     */
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId) {
        managerService.onSale(skuId);
        return Result.ok();
    }

    /**
     * 商品下架
     * @param skuId
     * @return
     */
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId) {
        managerService.cancelSale(skuId);
        return Result.ok();
    }

}
