package com.wangwei.mall.web.controller;


import com.wangwei.mall.model.product.SkuInfo;
import com.wangwei.mall.web.inner.service.ICartService;
import com.wangwei.mall.web.inner.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CartController {

    @Autowired
    private ICartService cartService;

    @Autowired
    private IProductService productService;

    /**
     * 查看购物车
     * @return
     */
    @RequestMapping("cart.html")
    public String index(){
        return "cart/index";
    }

    /**
     * 添加购物车
     * @param skuId
     * @param skuNum
     * @param request
     * @return
     */
    @RequestMapping("addCart.html")
    public String addCart(@RequestParam(name = "skuId") Long skuId,
                          @RequestParam(name = "skuNum") Integer skuNum,
                          HttpServletRequest request){
        SkuInfo skuInfo = productService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "cart/addCart";
    }

}
