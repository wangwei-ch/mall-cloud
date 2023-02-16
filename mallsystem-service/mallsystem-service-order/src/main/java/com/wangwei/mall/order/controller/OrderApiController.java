package com.wangwei.mall.order.controller;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.common.util.AuthContextHolder;
import com.wangwei.mall.model.cart.CartInfo;
import com.wangwei.mall.model.order.OrderDetail;
import com.wangwei.mall.model.order.OrderInfo;
import com.wangwei.mall.model.user.UserAddress;
import com.wangwei.mall.order.inner.service.ICartService;
import com.wangwei.mall.order.inner.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/order")
@Slf4j
public class OrderApiController {


    @Autowired
    private IUserService userService;

    @Autowired
    private ICartService cartService;


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

        Map<String, Object> result = new HashMap<>();
        result.put("userAddressList", addressList);
        result.put("detailArrayList", detailArrayList);
        // 保存总金额
        result.put("totalNum", detailArrayList.size());
        result.put("totalAmount", orderInfo.getTotalAmount());

        return Result.ok(result);
    }

}
