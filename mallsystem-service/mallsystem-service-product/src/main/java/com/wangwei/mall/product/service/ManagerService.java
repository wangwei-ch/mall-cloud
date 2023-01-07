package com.wangwei.mall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangwei.mall.model.product.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ManagerService {

    /**
     * 查询所有一级分类信息
     */
    List<BaseCategory1> getCategory1();

    /**
     * 根据一级分类id查询二级分类数据
     */
    List<BaseCategory2> getCategory2(Long category1Id);

    /**
     * 根据二级分类Id 查询三级分类数据
     * @param category2Id
     * @return
     */
    List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * 根据分类id获取平台属性数据
     * 接口说明:
     *      1.平台属性可以挂在一级分类、二级分类和三级分类
     *      2.查询一级分类下面的平台属性，传：category1Id，0，0；   取出该分类的平台属性
     *      3.查询二级分类下面的平台属性，传：category1Id，category2Id，0
     *          取出对应一级分类下面的平台属性与二级分类对应的平台属性
     *      4.查询三级分类下面的平台属性，传：category1Id，category2Id，category3Id；
     *  *       取出对应一级分类、二级分类与三级分类对应的平台属性
     *
     */
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    /**
     * 保存属性和属性值
     */
    void saveAttrInfo (BaseAttrInfo baseAttrInfo);

    BaseAttrInfo getAttrInfo(Long attrId);

    IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo);

    /**
     * 查询所有的销售属性数据
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();


    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuId 查询spuImageList
     * @param spuId
     * @return
     */
    List<SpuImage> getSpuImageList(Long spuId);


    /**
     * 根据spuId 查询销售属性集合
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    /**
     * 保存数据
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);


    /**
     * SKU分页列表
     * @param pageParam
     * @return
     */
    IPage<SkuInfo> getPage(Page<SkuInfo> pageParam);
    /**
     * 商品上架
     * @param skuId
     */
    void onSale(Long skuId);

    /**
     * 商品下架
     * @param skuId
     */
    void cancelSale(Long skuId);


    /**
     * 根据skuId 查询skuInfo
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(Long skuId);


    /**
     * 通过三级分类id查询分类信息
     * @param category3Id
     * @return
     */
    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);

    /**
     * 获取sku价格
     * @param skuId
     * @return
     */
    BigDecimal getSkuPrice(Long skuId);


    /**
     * 根据spuId，skuId 查询销售属性集合
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);


    /**
     * 根据spuId 查询map 集合属性
     * @param spuId
     * @return
     */
    Map getSkuValueIdsMap(Long spuId);

    /**
     * 根据spuid获取商品海报
     * @param spuId
     * @return
     */
    List<SpuPoster> findSpuPosterBySpuId(Long spuId);


    /**
     * 通过skuId 集合来查询数据
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> getAttrList(Long skuId);


    /**
     * 获取全部分类信息
     * @return
     */
    List<JSONObject> getBaseCategoryList();


    /**
     * 通过品牌Id 来查询数据
     * @param tmId
     * @return
     */
    BaseTrademark getTrademarkByTmId(Long tmId);

}
