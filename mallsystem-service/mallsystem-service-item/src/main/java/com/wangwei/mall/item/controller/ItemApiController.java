package com.wangwei.mall.item.controller;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/item")
public class ItemApiController {


    @Autowired
    private ItemService itemService;



    @GetMapping("{skuId}")
    public Result getItem(@PathVariable Long skuId){
        Map<String,Object> result = itemService.getBySkuId(skuId);
        return Result.ok(result);
    }

}
