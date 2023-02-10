package com.wangwei.mall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.common.result.ResultCodeEnum;
import com.wangwei.mall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@SuppressWarnings("all")
public class AuthGlobalFilter implements GlobalFilter {

    @Value("${authUrls.url}")
    private String authUrls;

    AntPathMatcher antPathMatcher=new AntPathMatcher();

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 认证过滤
     *  思路：
     *   1.内部接口直接拒绝  inner  提示
     *   2.根据token，获取userId
     *   3.判断token是否被盗用 提示
     *   4.判断api是否认证 提示
     *   5.判断域名 order.gmall.com 跳转
     *   6.添加userId到header
     *   7.方向
     *   8.没有登录去登录页面 originUrl
     *
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        //获取url http://list.gmall.com/list.html?category3Id=61
        URI uri = request.getURI();
        ///list.html  api/product/inner
        String path = request.getURI().getPath();
        //判断是否内部接口 api/*/inner/**
        if(antPathMatcher.match("/**/inner/**",path)){

            //提示权限
            ServerHttpResponse response = exchange.getResponse();
            //字节数组
            return out(response, ResultCodeEnum.PERMISSION);



        }
        //获取userId
        String userId=this.getUserId(request);
        //获取临时userTemId
        String userTempId=this.getUserTempId(request);

        //判读是否被盗用
        if("-1".equals(userId)){
            ServerHttpResponse response = exchange.getResponse();
            return out(response,ResultCodeEnum.ILLEGAL_REQUEST);
        }


        //判断api接口的访问
        ///api/**/auth/**
        if(antPathMatcher.match("/api/**/auth/**",path)){
            //判断是否登录
            if(StringUtils.isEmpty(userId)){
                //未登录

                //提示权限
                ServerHttpResponse response = exchange.getResponse();
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }

        }

        //判读是否需要认证
        //白名单
        //trade.html,myOrder.html,list.html,#addCart.html
        for (String authUrl : authUrls.split(",")) {
            //1.未登录的条件
            //2.白名单中包含当前path list.gmall.com/list.html path=list.html
            if(path.indexOf(authUrl)!=-1&&StringUtils.isEmpty(userId)){
                //未登录-重定向
                ServerHttpResponse response = exchange.getResponse();
                //设置状态码
                response.setStatusCode(HttpStatus.SEE_OTHER);
                //location
                response.getHeaders().set(HttpHeaders.LOCATION,"http://www.gmall.com/login.html?originUrl="+request.getURI());

                //重定向
                return response.setComplete();

            }
        }

        //可以访问指定的资源
        //需要，在后台服务处理业务过程中有可能需要用到userId
        if(!StringUtils.isEmpty(userId)||!StringUtils.isEmpty(userTempId)){

            //用户id
            if(!StringUtils.isEmpty(userId)){
                request.mutate().header("userId",userId).build();
            }
            //用户的临时id
            if(!StringUtils.isEmpty(userTempId)){
                request.mutate().header("userTempId",userTempId).build();
            }



            //放行
            return chain.filter(exchange.mutate().request(request).build());

        }

        return chain.filter(exchange);
    }

    /**
     * 获取临时id
     * @param request
     * @return
     */
    private String getUserTempId(ServerHttpRequest request) {

        //定义变量，接收token
        String userTempId ="";
        //从请求头中获取token
        List<String> userTempIdList = request.getHeaders().get("userTempId");
        //判断
        if(!CollectionUtils.isEmpty(userTempIdList)){
            userTempId=userTempIdList.get(0);

        }else{
            //从cookie获取
            MultiValueMap<String, HttpCookie> cookieMultiValueMap = request.getCookies();
            //获取token
            HttpCookie cookieUserTempId = cookieMultiValueMap.getFirst("userTempId");
            //判断
            if(null !=cookieUserTempId){
                userTempId= URLDecoder.decode(cookieUserTempId.getValue());
            }

        }

        return userTempId;
    }

    /**
     * 输出信息
     * @param response
     * @param loginAuth
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum loginAuth) {
        //字节数组
        Result<Object> result = Result.build(null, loginAuth);
        //转换
        byte[] bytes = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        //DataBuffer
        DataBuffer wrap = response.bufferFactory().wrap(bytes);
        //乱码问题
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        return response.writeWith(Mono.just(wrap));
    }

    /**
     * "":表示没有token
     * -1;表示有token，但是ip不一致
     * userId:成功认证
     * 获取user
     *
     * token：request
     *  1.header
     *  2.cookie
     *
     * 页面：login.html
     *   编码： function encode(s) {
     * URLDecoder.decode:解码
     *
     * 作用："=" "+",页面处理特殊字符时候，处理掉
     *
     * @param request
     * @return
     */
    private String getUserId(ServerHttpRequest request) {

        //定义变量，接收token
        String token ="";
        //从请求头中获取token
        List<String> tokeList = request.getHeaders().get("token");
        //判断
        if(!CollectionUtils.isEmpty(tokeList)){
            token=tokeList.get(0);

        }else{
            //从cookie获取
            MultiValueMap<String, HttpCookie> cookieMultiValueMap = request.getCookies();
            //获取token
            HttpCookie cookieToken = cookieMultiValueMap.getFirst("token");
            //判断
            if(null !=cookieToken){
                token= URLDecoder.decode(cookieToken.getValue());
            }

        }

        //判断是否获取到token
        if(!StringUtils.isEmpty(token)){

            //获取数据redis
            String userJon = (String) redisTemplate.opsForValue().get("user:login:" + token);
            //转化成JSONObject
            if(!StringUtils.isEmpty(userJon)){


                JSONObject jsonObject = JSONObject.parseObject(userJon);
                //获取redis存储的ip
                String ip = (String) jsonObject.get("ip");
                //获取当前请求的ip
                String currentIp = IpUtil.getGatwayIpAddress(request);


                //判断是否为同一
                if(ip.equals(currentIp)){

                    return (String) jsonObject.get("userId");
                }else{

                    return "-1";
                }
            }
        }




        //没有token
        return "";
    }
}
