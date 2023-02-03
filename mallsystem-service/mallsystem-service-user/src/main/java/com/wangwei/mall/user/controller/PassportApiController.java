package com.wangwei.mall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.wangwei.mall.common.constant.RedisConst;
import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.common.util.IpUtil;
import com.wangwei.mall.model.user.UserInfo;
import com.wangwei.mall.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/passport")
@Slf4j
public class PassportApiController {


    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登录
     */
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request, HttpServletResponse response) {

        log.info("进入控制器");
        UserInfo info = userService.login(userInfo);


        //可以从数据库中查到登录用户的信息
        if (null != info) {
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            HashMap<String, Object> map = new HashMap<>();

            //返回给页面的数据
            map.put("nickName", info.getNickName());
            map.put("token", token);

            JSONObject userJson = new JSONObject();
            userJson.put("userId", info.getId().toString());
            userJson.put("ip", IpUtil.getIpAddress(request));
            redisTemplate.opsForValue().set(
                    RedisConst.USER_LOGIN_KEY_PREFIX + token,
                    userJson.toJSONString(),
                    RedisConst.USERKEY_TIMEOUT,
                    TimeUnit.SECONDS);
            return Result.ok(map);
        }else {
            return Result.fail().message("用户名或密码错误");
        }


    }


    /**
     * 退出登录
     * @param request
     * @return
     */
    @GetMapping("logout")
    public Result logout(HttpServletRequest request){
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX + request.getHeader("token"));
        return Result.ok();
    }

}
