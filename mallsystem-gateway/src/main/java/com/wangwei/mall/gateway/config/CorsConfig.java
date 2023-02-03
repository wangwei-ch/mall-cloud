package com.wangwei.mall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){

        //cors跨域配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");  //设置允许访问的网络
        corsConfiguration.setAllowCredentials(true); //设置是否从服务器获取cookie
        corsConfiguration.addAllowedHeader("*"); //设置请求头信息
        corsConfiguration.addAllowedMethod("*");//设置允许的请求方法

        //配置源对象
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        //cors过滤器对象
        return new CorsWebFilter(corsConfigurationSource);
    }

}
