package com.wangwei.mall.product.service.impl;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangwei.mall.model.product.BaseCategoryTrademark;
import com.wangwei.mall.model.product.BaseTrademark;
import com.wangwei.mall.model.product.CategoryTrademarkVo;
import com.wangwei.mall.product.mapper.BaseCategoryTrademarkMapper;
import com.wangwei.mall.product.mapper.BaseTrademarkMapper;
import com.wangwei.mall.product.service.BaseCategoryTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BaseCategoryTrademarkServiceImpl extends ServiceImpl<BaseCategoryTrademarkMapper, BaseCategoryTrademark> implements BaseCategoryTrademarkService {

    @Autowired
    private BaseCategoryTrademarkMapper categoryTrademarkMapper;

    @Autowired
    private BaseTrademarkMapper trademarkMapper;


    @Override
    public List<BaseTrademark> findTrademarkList(Long category3Id) {
        QueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkQueryWrapper = new QueryWrapper<>();
        baseCategoryTrademarkQueryWrapper.eq("category3_id", category3Id);
        List<BaseCategoryTrademark> list = categoryTrademarkMapper.selectList(baseCategoryTrademarkQueryWrapper);

        //  判断baseCategoryTrademarkList 这个集合
        if(!CollectionUtils.isEmpty(list)){
            //  需要获取到这个集合中的品牌Id 集合数据
            List<Long> tradeMarkIdList = list.stream().map(baseCategoryTrademark -> {
                return baseCategoryTrademark.getTrademarkId();
            }).collect(Collectors.toList());
            //  正常查询数据的话... 需要根据品牌Id 来获取集合数据！
            return trademarkMapper.selectBatchIds(tradeMarkIdList);
        }
        //  如果集合为空，则默认返回空
        return null;
    }

    @Override
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {
        //  哪些是关联的品牌Id
        QueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkQueryWrapper = new QueryWrapper<>();
        baseCategoryTrademarkQueryWrapper.eq("category3_id",category3Id);
        List<BaseCategoryTrademark> list = categoryTrademarkMapper.selectList(baseCategoryTrademarkQueryWrapper);

        if (!CollectionUtils.isEmpty(list)){
            //  找到关联的品牌Id 集合数据 {1,3}
            List<Long> idList = list.stream().map(BaseCategoryTrademark::getTrademarkId).collect(Collectors.toList());
            List<BaseTrademark> baseTrademarkList = trademarkMapper.selectList(null).stream().filter(baseTrademark -> {
                return !idList.contains(baseTrademark.getId());
            }).collect(Collectors.toList());
            //  返回数据
            return baseTrademarkList;
        }
        //  如果说这个三级分类Id 下 没有任何品牌！ 则获取到所有的品牌数据！
        return trademarkMapper.selectList(null);

    }

    @Override
    public void save(CategoryTrademarkVo categoryTrademarkVo) {
        //  获取到品牌Id 集合数据
        List<Long> trademarkIdList = categoryTrademarkVo.getTrademarkIdList();

        //  判断
        if (!CollectionUtils.isEmpty(trademarkIdList)){
            List<BaseCategoryTrademark> collect = trademarkIdList.stream().map((trademarkId) -> {
                BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
                baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());
                baseCategoryTrademark.setTrademarkId(trademarkId);
                return baseCategoryTrademark;
            }).collect(Collectors.toList());

            this.saveBatch(collect);
        }


    }

    @Override
    public void remove(Long category3Id, Long trademarkId) {
        //  更新： update base_category_trademark set is_deleted = 1 where category3_id=? and trademark_id=?;
        QueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkQueryWrapper = new QueryWrapper<>();
        baseCategoryTrademarkQueryWrapper.eq("category3_id",category3Id);
        baseCategoryTrademarkQueryWrapper.eq("trademark_id",trademarkId);
        categoryTrademarkMapper.delete(baseCategoryTrademarkQueryWrapper);
    }
}
