package com.wangwei.mall.order.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.wangwei.mall.common.constant.RedisConst;
import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.common.util.AuthContextHolder;
import com.wangwei.mall.model.cart.CartInfo;
import com.wangwei.mall.model.order.OrderDetail;
import com.wangwei.mall.model.order.OrderInfo;
import com.wangwei.mall.model.user.UserAddress;
import com.wangwei.mall.order.inner.service.ICartService;
import com.wangwei.mall.order.inner.service.IProductService;
import com.wangwei.mall.order.inner.service.IUserService;
import com.wangwei.mall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("api/order")
@Slf4j
public class OrderApiController {


    @Autowired
    private IUserService userService;

    @Autowired
    private ICartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private IProductService productService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 确认订单
     * @param request
     * @return
     */
    @GetMapping("auth/trade")
    public Result<Map<String, Object>> trade(HttpServletRequest request) {

        // 获取到用户Id
        String userId = AuthContextHolder.getUserId(request);
        log.info("用户id为-{}",userId);

        List<UserAddress> addressList = userService.findUserAddressListByUserId(userId);

        List<CartInfo> cartInfoList = cartService.getCartCheckedList(userId);

        ArrayList<OrderDetail> detailArrayList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());

            // 添加到集合
            detailArrayList.add(orderDetail);
        }

        // 计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();

        // 获取流水号
        String tradeNo = orderService.getTradeNo(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("userAddressList", addressList);
        result.put("detailArrayList", detailArrayList);
        // 保存总金额
        result.put("totalNum", detailArrayList.size());
        result.put("totalAmount", orderInfo.getTotalAmount());

        result.put("tradeNo", tradeNo);
        return Result.ok(result);
    }

    /**
     * 提交订单
     * @param orderInfo
     * @param request
     * @return
     */
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request){
        // 获取到用户Id
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));


        // 获取前台页面的流水号
        String tradeNo = request.getParameter("tradeNo");

        // 调用服务层的比较方法
        boolean flag = orderService.checkTradeCode(userId, tradeNo);
        if (!flag) {
            // 比较失败！
            return Result.fail().message("不能重复提交订单！");
        }
        //  删除流水号
        orderService.deleteTradeNo(userId);

        /**
         * 使用异步编排形式减少请求时间
         */

        List<String> errorList = new ArrayList<>();
        List<CompletableFuture> futureList = new ArrayList<>();



        //验库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {

            CompletableFuture<Void> checkStockCompletableFuture =
                    CompletableFuture.runAsync(() ->{
                boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                if (!result) {
//                    return Result.fail().message(orderDetail.getSkuName() + "库存不足！");
                    errorList.add(orderDetail.getSkuName() + "库存不足!");
                }
            },threadPoolExecutor);
            futureList.add(checkStockCompletableFuture);


            CompletableFuture<Void> checkPriceCompletableFuture =
                    CompletableFuture.runAsync(() -> {
                // 验证价格：
                BigDecimal skuPrice = productService.getSkuPrice(orderDetail.getSkuId());
                if (orderDetail.getOrderPrice().compareTo(skuPrice) != 0) {
                    List<CartInfo> cartInfoList = cartService.getCartCheckedList(userId);
                    cartInfoList.forEach(cartInfo -> {
                        redisTemplate.opsForHash().put(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX, cartInfo.getSkuId().toString(), cartInfo);
                    });
                    errorList.add(orderDetail.getSkuName() + "价格有变动！");
//                    return Result.fail().message(orderDetail.getSkuName() + "价格有变动！");
                }
            },threadPoolExecutor);
            futureList.add(checkPriceCompletableFuture);

        }

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();

        if(errorList.size() > 0) {
            return Result.fail().message(StringUtils.join(errorList, ","));
        }

        // 验证通过，保存订单！
        Long orderId = orderService.saveOrderInfo(orderInfo);
        return Result.ok(orderId);
    }

}
