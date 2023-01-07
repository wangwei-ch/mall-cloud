package com.wangwei.mall.web.controller;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.web.inner.service.IItemService;
import com.wangwei.mall.web.inner.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Map;

@Controller
public class ItemController {



    @Resource
    private IItemService itemService;


    /**
     * sku详情页面
     * @param skuId
     * @param model
     * @return
     */
    @RequestMapping("{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model){
        // 通过skuId 查询skuInfo
        Result<Map> result = itemService.getItem(skuId);
        model.addAllAttributes(result.getData());
        return "item/item";
    }


}
