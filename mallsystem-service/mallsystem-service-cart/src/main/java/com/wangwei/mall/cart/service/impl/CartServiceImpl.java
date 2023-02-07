package com.wangwei.mall.cart.service.impl;

import com.wangwei.mall.cart.inner.service.IProductService;
import com.wangwei.mall.cart.service.CartService;
import com.wangwei.mall.common.constant.RedisConst;
import com.wangwei.mall.common.util.DateUtil;
import com.wangwei.mall.model.cart.CartInfo;
import com.wangwei.mall.model.product.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@SuppressWarnings("all")
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IProductService productService;

    @Override
    public void addToCart(Long skuId, String userId, Integer skuNum) {

        //  获取缓存key
        String cartKey = getCartKey(userId);

        BoundHashOperations<String, String, CartInfo> boundHashOps
                = redisTemplate.boundHashOps(cartKey);

        CartInfo cartInfo = null;
        //判断缓存中有没有添加过该商品
        if(boundHashOps.hasKey(skuId.toString())) {
            //如果添加过该商品 那么只修改数量和实时价格和时间
            cartInfo = boundHashOps.get(skuId.toString());
            cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
            cartInfo.setIsChecked(1);
            cartInfo.setSkuPrice(productService.getSkuPrice(skuId));
            cartInfo.setUpdateTime(new Date());
        } else {
            //如果第一次添加该商品
            cartInfo = new CartInfo();
            //  给cartInfo 赋值！
            SkuInfo skuInfo = productService.getSkuInfo(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
            cartInfo.setSkuPrice(skuInfo.getPrice());
        }
        //添加到缓存中
        boundHashOps.put(skuId.toString(), cartInfo);
    }

    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {

        //创建返回结果集
        List<CartInfo> result = null;

        if (!StringUtils.isEmpty(userId)){
            BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(getCartKey(userId));
            result = boundHashOperations.values();
        }

        if (!StringUtils.isEmpty(userTempId)){
            BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(getCartKey(userTempId));
            result = boundHashOperations.values();
        }

        if (!CollectionUtils.isEmpty(result)){
            result.sort((o1,o2) -> {
                //使用时间进行比较
                return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
            });
        }

        return null;
    }

    /**
     * 获取购物车的key
     * @param userId
     * @return
     */
    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }


}
