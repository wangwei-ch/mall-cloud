package com.wangwei.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangwei.mall.model.product.BaseTrademark;
import com.wangwei.mall.product.mapper.BaseTrademarkMapper;
import com.wangwei.mall.product.service.BaseTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseTrademarkServiceImpl extends ServiceImpl<BaseTrademarkMapper, BaseTrademark> implements BaseTrademarkService {

    @Autowired
    private BaseTrademarkMapper trademarkMapper;

    @Override
    public IPage<BaseTrademark> selectPage(Page<BaseTrademark> baseTrademarkPage) {

        QueryWrapper<BaseTrademark> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        IPage<BaseTrademark> page = trademarkMapper.selectPage(baseTrademarkPage, wrapper);
        return page;
    }
}
