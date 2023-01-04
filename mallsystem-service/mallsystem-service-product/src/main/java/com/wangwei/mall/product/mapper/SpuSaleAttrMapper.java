package com.wangwei.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangwei.mall.model.product.SpuSaleAttr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    List<SpuSaleAttr> selectSpuSaleAttrList(Long spuId);


    // 根据spuId，skuId 查询销售属性集合
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("skuId") Long skuId, @Param("spuId")Long spuId);
}
