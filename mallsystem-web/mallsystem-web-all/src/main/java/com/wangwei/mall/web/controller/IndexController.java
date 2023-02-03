package com.wangwei.mall.web.controller;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.web.inner.service.IProductService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexController {


    @Resource
    private IProductService iProductService;

    @GetMapping({"/","index.html"})
    public String index(HttpServletRequest request){
        // 获取首页分类数据
        Result result = iProductService.getBaseCategoryList();
        request.setAttribute("list",result.getData());
        return "index/index";
    }

}
