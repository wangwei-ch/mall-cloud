package com.wangwei.mall.order.inner.fallback;

import com.wangwei.mall.model.user.UserAddress;
import com.wangwei.mall.order.inner.service.IUserService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IUserDegradeService implements IUserService {
    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {
        return null;
    }
}
