package com.wangwei.mall.user.service;

import com.wangwei.mall.model.user.UserAddress;

import java.util.List;

public interface UserAddressService {
    List<UserAddress> findUserAddressListByUserId(String userId);
}
