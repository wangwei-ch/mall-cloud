package com.wangwei.mall.order.inner.service;

import com.wangwei.mall.model.user.UserAddress;
import com.wangwei.mall.order.inner.fallback.IUserDegradeService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "mallsystem-service-user",fallback = IUserDegradeService.class)
public interface IUserService {

    @GetMapping("/api/user/inner/findUserAddressListByUserId/{userId}")
    List<UserAddress> findUserAddressListByUserId(@PathVariable(value = "userId") String userId);
}
