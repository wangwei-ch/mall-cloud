package com.wangwei.mall.item.service;

import java.util.Map;

public interface ItemService {


    //获取SKU详细信息
    Map<String, Object> getBySkuId(Long skuId);

}
