package com.wangwei.mall.list.controller;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.model.list.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/list")
public class ListApiController {


    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @GetMapping("inner/createIndex")
    public Result createIndex(){
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        return Result.ok();
    }



}
