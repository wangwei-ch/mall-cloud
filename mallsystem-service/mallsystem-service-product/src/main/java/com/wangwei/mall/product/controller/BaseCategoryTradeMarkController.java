package com.wangwei.mall.product.controller;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.model.product.BaseTrademark;
import com.wangwei.mall.model.product.CategoryTrademarkVo;
import com.wangwei.mall.product.service.BaseCategoryTrademarkService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("分类品牌管理")
@RestController
@RequestMapping("admin/product/baseCategoryTrademark")
public class BaseCategoryTradeMarkController {

    @Autowired
    private BaseCategoryTrademarkService categoryTrademarkService;


    @GetMapping("/findTrademarkList/{category3Id}")
    public Result findTrademarkList(@PathVariable Long category3Id){
        List<BaseTrademark> list = categoryTrademarkService.findTrademarkList(category3Id);
        //  返回
        return Result.ok(list);
    }


    @GetMapping("/findCurrentTrademarkList/{category3Id}")
    public Result findCurrentTrademarkList(@PathVariable Long category3Id){
        List<BaseTrademark> list = categoryTrademarkService.findCurrentTrademarkList(category3Id);
        return Result.ok(list);
    }

    @PostMapping("save")
    public Result save(@RequestBody CategoryTrademarkVo categoryTrademarkVo){
        //  保存方法
        categoryTrademarkService.save(categoryTrademarkVo);
        return Result.ok();
    }

    @DeleteMapping("remove/{category3Id}/{trademarkId}")
    public Result remove(@PathVariable Long category3Id, @PathVariable Long trademarkId){
        //  调用服务层方法
        categoryTrademarkService.remove(category3Id, trademarkId);
        return Result.ok();
    }

}
