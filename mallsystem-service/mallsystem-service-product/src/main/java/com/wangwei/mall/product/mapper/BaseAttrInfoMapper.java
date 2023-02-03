package com.wangwei.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangwei.mall.model.product.BaseAttrInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    /**
     *
     * @param skuId
     */
    List<BaseAttrInfo> selectBaseAttrInfoListBySkuId(@Param("skuId")Long skuId);

    List<BaseAttrInfo> selectBaseAttrInfoList(@Param("category1Id")Long category1Id, @Param("category2Id")Long category2Id, @Param("category3Id")Long category3Id);
}
