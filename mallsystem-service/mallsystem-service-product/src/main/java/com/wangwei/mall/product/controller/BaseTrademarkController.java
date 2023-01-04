package com.wangwei.mall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.model.product.BaseTrademark;
import com.wangwei.mall.product.service.BaseTrademarkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("品牌管理")
@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {


    @Autowired
    private BaseTrademarkService trademarkService;

    /**
     * 品牌列表 - 分页
     * @param page
     * @param limit
     * @return
     */
    @ApiOperation("品牌列表-分页")
    @GetMapping("{page}/{limit}")
    public Result getTrademarkList(@PathVariable Long page,
                                                        @PathVariable Long limit){

        Page<BaseTrademark> baseTrademarkPage = new Page<>(page, limit);
        IPage<BaseTrademark> pageModel = trademarkService.selectPage(baseTrademarkPage);

        return Result.ok(pageModel);
    }


    @ApiOperation(value = "获取BaseTrademark")
    @GetMapping("get/{id}")
    public Result get(@PathVariable String id) {
        BaseTrademark baseTrademark = trademarkService.getById(id);
        return Result.ok(baseTrademark);
    }


    @ApiOperation(value = "新增BaseTrademark")
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark banner) {
        trademarkService.save(banner);
        return Result.ok();
    }

    @ApiOperation(value = "修改BaseTrademark")
    @PutMapping("update")
    public Result updateById(@RequestBody BaseTrademark banner) {
        trademarkService.updateById(banner);
        return Result.ok();
    }

    @ApiOperation(value = "删除BaseTrademark")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        trademarkService.removeById(id);
        return Result.ok();
    }
}
