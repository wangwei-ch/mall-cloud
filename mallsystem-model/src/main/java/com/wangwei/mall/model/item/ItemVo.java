package com.wangwei.mall.model.item;


import com.wangwei.mall.model.product.BaseCategoryView;
import com.wangwei.mall.model.product.SkuInfo;
import com.wangwei.mall.model.product.SpuPoster;
import com.wangwei.mall.model.product.SpuSaleAttr;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ItemVo {

    @ApiModelProperty(value = "sku信息")
    private SkuInfo skuInfo;
    @ApiModelProperty(value = "分类信息")
    private BaseCategoryView categoryView;
    @ApiModelProperty(value = "spu销售属性")
    private List<SpuSaleAttr> spuSaleAttrList;
    @ApiModelProperty(value = "spu海报数据")
    private List<SpuPoster> spuPosterList;
    @ApiModelProperty(value = "sku平台属性")
    private List<Map<String, String>> skuAttrList;
    @ApiModelProperty(value = "切换数据")
    private String valuesSkuJson;
    @ApiModelProperty(value = "最新价格")
    private BigDecimal skuPrice;

}
