package com.wangwei.mall.list.service;

import com.wangwei.mall.model.list.SearchParam;
import com.wangwei.mall.model.list.SearchResponseVo;

import java.io.IOException;

public interface SearchService {

    /**
     * 上架商品列表
     */
    void upperGoods(Long skuId);


    /**
     * 下架商品列表
     */
    void lowerGoods(Long skuId);

    /**
     * 更新商品热点
     * @param skuId
     */
    void incrHotScore(Long skuId);

    /**
     * 商品搜索
     * @param searchParam
     * @return
     */
    SearchResponseVo search(SearchParam searchParam) throws IOException;
}
