package com.wangwei.mall.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangwei.mall.model.user.UserInfo;
import com.wangwei.mall.user.mapper.UserInfoMapper;
import com.wangwei.mall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public UserInfo login(UserInfo userInfo) {

        //登录查询数据库
        String passwd = userInfo.getPasswd();

        //加密
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());

        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("login_name", userInfo.getLoginName());
        wrapper.eq("passwd", newPasswd);
        UserInfo info = userInfoMapper.selectOne(wrapper);

        if (info != null) return info;


        return null;
    }
}
