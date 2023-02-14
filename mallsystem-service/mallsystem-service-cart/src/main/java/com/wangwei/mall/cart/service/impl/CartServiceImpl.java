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

        //未登录临时购物车
        List<CartInfo> noLoginCartInfoList = null;


        //情况一：未登录，获取临时购物车
        if (!StringUtils.isEmpty(userTempId)){
            //获取cartKey
            String cartKey = getCartKey(userTempId);
            BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
            //获取商品列表
            noLoginCartInfoList = boundHashOperations.values();
        }

        /**
         * 判断确认未登录,如果未登录,排序后直接返回
         */
        if (StringUtils.isEmpty(userId)){
           //排序
           if (!CollectionUtils.isEmpty(noLoginCartInfoList)){
               noLoginCartInfoList.sort((o1,o2) -> {
                   return DateUtil.truncatedCompareTo(o1.getUpdateTime(), o2.getUpdateTime(), Calendar.SECOND);
               });
           }
           return noLoginCartInfoList;
        }

        //情况2：用户已经登录
        //获取登录后用户的购物车
        BoundHashOperations<String,String,CartInfo> operations = redisTemplate.boundHashOps(getCartKey(userId));
        //判断未登录时购物车里是否有商品
        if (!CollectionUtils.isEmpty(noLoginCartInfoList)){
            //如果临时购物车有商品
            noLoginCartInfoList.stream().forEach(cartInfo -> {
                if (operations.hasKey(cartInfo.getSkuId().toString())){

                    //获取登录后的购物信息
                    CartInfo loginCartInfo = operations.get(cartInfo.getSkuId().toString());
                    //数量相加
                    loginCartInfo.setSkuNum(cartInfo.getSkuNum()+loginCartInfo.getSkuNum());
                    //更新修改时间
                    loginCartInfo.setUpdateTime(new Date());
                    //更新价格
                    loginCartInfo.setSkuPrice(productService.getSkuPrice(loginCartInfo.getSkuId()));
                    //判断选中
                    if (cartInfo.getIsChecked().intValue()==1){
                        loginCartInfo.setIsChecked(1);
                    }
                    //修改redis中登录后购物车的数据
                    operations.put(cartInfo.getSkuId().toString(), loginCartInfo);

                }else {
                    //表示不包含
                    cartInfo.setUserId(userId);
                    cartInfo.setCreateTime(new Date());
                    cartInfo.setUpdateTime(new Date());
                    operations.put(cartInfo.getSkuId().toString(),cartInfo);
                }
            });

            //表示合并完成
            redisTemplate.delete(getCartKey(userTempId));
        }

        //获取登录成功的购物车
        List<CartInfo> loginCartInfoList = operations.values();
        if (!CollectionUtils.isEmpty(loginCartInfoList)){
            return loginCartInfoList;
        }else {
            return new ArrayList<>();
        }

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
