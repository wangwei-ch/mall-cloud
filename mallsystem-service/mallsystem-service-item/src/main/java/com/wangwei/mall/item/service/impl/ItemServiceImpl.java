package com.wangwei.mall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.wangwei.mall.common.constant.RedisConst;
import com.wangwei.mall.item.inner.service.IListService;
import com.wangwei.mall.item.inner.service.IProductService;
import com.wangwei.mall.item.service.ItemService;
import com.wangwei.mall.model.product.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {



    @Resource
    private IProductService productService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private IListService listService;



    @Override
    public Map<String, Object> getBySkuId(Long skuId) {

        Map<String, Object> result = new HashMap<>();

        CompletableFuture<SkuInfo> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {

            SkuInfo skuInfo = productService.getSkuInfo(skuId);
            result.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);


        CompletableFuture<Void> spuSaleAttrCompletableFuture  = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productService.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            result.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        }, threadPoolExecutor);

        CompletableFuture<Void> skuValueIdsMapCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            //  获取销售属性+销售属性值
            //  查询销售属性值Id 与skuId 组合的map
            Map skuValueIdsMap = productService.getSkuValueIdsMap(skuInfo.getSpuId());
            //  将这个map 转换为页面需要的Json 对象
            String valueJson = JSON.toJSONString(skuValueIdsMap);
            result.put("valuesSkuJson",valueJson);
        },threadPoolExecutor);

        CompletableFuture<Void> skuPriceCompletableFuture = CompletableFuture.runAsync(()->{
            //  获取价格
            BigDecimal skuPrice = productService.getSkuPrice(skuId);
            //  map 中 key 对应的谁? Thymeleaf 获取数据的时候 ${skuInfo.skuName}
            result.put("price",skuPrice);
        },threadPoolExecutor);

        CompletableFuture<Void> categoryViewCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            //  获取分类数据
            BaseCategoryView categoryView = productService.getCategoryView(skuInfo.getCategory3Id());
            result.put("categoryView",categoryView);
        },threadPoolExecutor);

        CompletableFuture<Void> spuPosterListCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            //  spu海报数据
            List<SpuPoster> spuPosterList =  productService.getSpuPosterBySpuId(skuInfo.getSpuId());
            result.put("spuPosterList", spuPosterList);

        },threadPoolExecutor);

        CompletableFuture<Void> skuAttrListCompletableFuture = CompletableFuture.runAsync(() -> {
            List<BaseAttrInfo> attrList = productService.getAttrList(skuId);
            //  使用拉姆达表示
            List<Map<String, String>> skuAttrList = attrList.stream().map((baseAttrInfo) -> {
                Map<String, String> attrMap = new HashMap<>();
                attrMap.put("attrName", baseAttrInfo.getAttrName());
                attrMap.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                return attrMap;
            }).collect(Collectors.toList());
            result.put("skuAttrList", skuAttrList);
        },threadPoolExecutor);

        CompletableFuture<Void> incrHotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
            listService.incrHotScore(skuId);
        }, threadPoolExecutor);


        CompletableFuture.allOf(skuAttrListCompletableFuture,
                spuSaleAttrCompletableFuture,
                skuValueIdsMapCompletableFuture,
                skuPriceCompletableFuture,
                categoryViewCompletableFuture,
                spuPosterListCompletableFuture,
                spuPosterListCompletableFuture,
                skuAttrListCompletableFuture,
                incrHotScoreCompletableFuture).join();

        return result;
    }


}
