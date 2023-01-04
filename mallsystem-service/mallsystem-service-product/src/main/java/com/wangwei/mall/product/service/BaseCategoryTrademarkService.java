package com.wangwei.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangwei.mall.model.product.BaseCategoryTrademark;
import com.wangwei.mall.model.product.BaseTrademark;
import com.wangwei.mall.model.product.CategoryTrademarkVo;

import java.util.List;

public interface BaseCategoryTrademarkService extends IService<BaseCategoryTrademark> {
    List<BaseTrademark> findTrademarkList(Long category3Id);

    List<BaseTrademark> findCurrentTrademarkList(Long category3Id);

    /**
     * 保存分类与品牌关联
     * @param categoryTrademarkVo
     */
    void save(CategoryTrademarkVo categoryTrademarkVo);

    /**
     * 删除关联
     * @param category3Id
     * @param trademarkId
     */
    void remove(Long category3Id, Long trademarkId);
}
