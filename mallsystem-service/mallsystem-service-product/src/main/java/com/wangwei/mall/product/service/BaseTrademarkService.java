package com.wangwei.mall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wangwei.mall.model.product.BaseTrademark;

public interface BaseTrademarkService extends IService<BaseTrademark> {

    IPage<BaseTrademark> selectPage(Page<BaseTrademark> baseTrademarkPage);

}
