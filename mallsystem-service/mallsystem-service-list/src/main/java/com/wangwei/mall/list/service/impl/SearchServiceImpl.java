package com.wangwei.mall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.wangwei.mall.list.inner.service.IProductService;
import com.wangwei.mall.list.repository.GoodsRepository;
import com.wangwei.mall.list.service.SearchService;
import com.wangwei.mall.model.list.*;
import com.wangwei.mall.model.product.*;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class SearchServiceImpl implements SearchService {

    //注入操作es的客户端

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private IProductService productService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;



    @Override
    public void upperGoods(Long skuId) {

        Goods goods = new Goods();
        List<BaseAttrInfo> baseAttrInfoList = productService.getAttrList(skuId);
        if (!CollectionUtils.isEmpty(baseAttrInfoList)){
            List<SearchAttr> searchAttrList = baseAttrInfoList.stream().map(baseAttrInfo -> {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getAttrValueList();
                searchAttr.setAttrValue(baseAttrValueList.get(0).getValueName());
                return searchAttr;
            }).collect(Collectors.toList());
            goods.setAttrs(searchAttrList);
        }

        //查询sku信息
        SkuInfo skuInfo = productService.getSkuInfo(skuId);

        //查询品牌
        BaseTrademark baseTrademark = productService.getTrademark(skuInfo.getTmId());
        if (null != baseTrademark){
            goods.setTmId(skuInfo.getTmId());
            goods.setTmName(baseTrademark.getTmName());
            goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        }

        //查询分类
        BaseCategoryView baseCategoryView = productService.getCategoryView(skuInfo.getCategory3Id());
        if (null != baseCategoryView){
            goods.setCategory1Id(baseCategoryView.getCategory1Id());
            goods.setCategory1Name(baseCategoryView.getCategory1Name());
            goods.setCategory2Id(baseCategoryView.getCategory2Id());
            goods.setCategory2Name(baseCategoryView.getCategory2Name());
            goods.setCategory3Id(baseCategoryView.getCategory3Id());
            goods.setCategory3Name(baseCategoryView.getCategory3Name());
        }

        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setId(skuInfo.getId());
        goods.setTitle(skuInfo.getSkuName());
        goods.setCreateTime(new Date());

        this.goodsRepository.save(goods);

    }

    @Override
    public void lowerGoods(Long skuId) {
        this.goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {

        //定义key
        String hotKey = "hotScore";

        //保存数据
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);

        if (hotScore % 10 == 0){
            //更新es
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(Math.round(hotScore));
            goodsRepository.save(goods);
        }

    }

    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {

        //构建dsl语句
        SearchRequest searchRequest = buildQueryDsl(searchParam);

        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchResponseVo responseVo = parseSearchResult(response);
        responseVo.setPageSize(searchParam.getPageSize());
        responseVo.setPageNo(searchParam.getPageNo());
        responseVo.setTotalPages((responseVo.getTotal() + searchParam.getPageSize() -1) / searchParam.getPageSize());
        return responseVo;
    }

    private SearchResponseVo parseSearchResult(SearchResponse response) {

        SearchHits hits = response.getHits();
        //声明对象
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        //获取品牌的集合
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
        //ParsedLongTerms ?
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo trademark = new SearchResponseTmVo();
            //获取品牌Id
            trademark.setTmId((Long.parseLong(((Terms.Bucket) bucket).getKeyAsString())));
            //trademark.setTmId(Long.parseLong(bucket.getKeyAsString()));
            //获取品牌名称
            Map<String, Aggregation> tmIdSubMap = ((Terms.Bucket) bucket).getAggregations().asMap();
            ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmIdSubMap.get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();

            trademark.setTmName(tmName);
            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) tmIdSubMap.get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            trademark.setTmLogoUrl(tmLogoUrl);

            return trademark;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(trademarkList);

        //赋值商品列表
        SearchHit[] subHits = hits.getHits();
        List<Goods> goodsList = new ArrayList<>();
        if (subHits!=null && subHits.length>0){
            //循环遍历
            for (SearchHit subHit : subHits) {
                // 将subHit 转换为对象
                Goods goods = JSONObject.parseObject(subHit.getSourceAsString(), Goods.class);

                //获取高亮
                if (subHit.getHighlightFields().get("title")!=null){
                    Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                    goods.setTitle(title.toString());
                }
                goodsList.add(goods);
            }
        }
        searchResponseVo.setGoodsList(goodsList);

        //获取平台属性数据
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> buckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)){
            List<SearchResponseAttrVo> searchResponseAttrVOS = buckets.stream().map(bucket -> {
                //声明平台属性对象
                SearchResponseAttrVo responseAttrVO = new SearchResponseAttrVo();
                //设置平台属性值Id
                responseAttrVO.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
                List<? extends Terms.Bucket> nameBuckets = attrNameAgg.getBuckets();
                responseAttrVO.setAttrName(nameBuckets.get(0).getKeyAsString());
                //设置规格参数列表
                ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
                List<? extends Terms.Bucket> valueBuckets = attrValueAgg.getBuckets();

                List<String> values = valueBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                responseAttrVO.setAttrValueList(values);

                return responseAttrVO;

            }).collect(Collectors.toList());
            searchResponseVo.setAttrsList(searchResponseAttrVOS);
        }
        // 获取总记录数
        searchResponseVo.setTotal(hits.getTotalHits().value);

        return searchResponseVo;
    }

    private SearchRequest buildQueryDsl(SearchParam searchParam) {

        //构建查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //构建boolquerybuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //判断查询条件是否为空 关键字
        if (!StringUtils.isBlank(searchParam.getKeyword())){
            MatchQueryBuilder title = QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND);
            boolQueryBuilder.must(title);
        }

        //构建品牌查询
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isBlank(trademark)){
            String[] split = org.springframework.util.StringUtils.split(trademark, ":");
            if (null != split && split.length == 2 ){
                //根据品牌id过滤
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }

        // 构建分类过滤 用户在点击的时候，只能点击一个值，所以此处使用term
        if(null!=searchParam.getCategory1Id()){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id",searchParam.getCategory1Id()));
        }
        // 构建分类过滤
        if(null!=searchParam.getCategory2Id()){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id",searchParam.getCategory2Id()));
        }
        // 构建分类过滤
        if(null!=searchParam.getCategory3Id()){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id",searchParam.getCategory3Id()));
        }


        //构建平台属性查询
        String[] props = searchParam.getProps();
        if (null != props && props.length > 0){
            for (String prop : props) {
                String[] split = org.springframework.util.StringUtils.split(prop, ":");
                if (null != split && split.length == 3){
                    //构建嵌套查询
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    //嵌套查询子查询
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
                    //构建子查询中的过滤条件
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));
                    boolQuery.must(QueryBuilders.nestedQuery("attrs", subBoolQuery, ScoreMode.None));
                    boolQueryBuilder.filter(boolQuery);
                }
            }
        }

        //执行查询方法
        searchSourceBuilder.query(boolQueryBuilder);

        //构建分页
        int from = (searchParam.getPageNo()-1)*searchParam.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());

        // 排序
        String order = searchParam.getOrder();
        if (!StringUtils.isBlank(order)){
            // 判断排序规则
            String[] split = org.springframework.util.StringUtils.split(order, ":");
            if (split!=null && split.length==2){
                // 排序的字段
                String field = null;
                // 数组中的第一个参数
                switch (split[0]){
                    case "1":
                        field="hotScore";
                        break;
                    case "2":
                        field="price";
                        break;
                }
                searchSourceBuilder.sort(field,"asc".equals(split[1])? SortOrder.ASC:SortOrder.DESC);
            }else {
                // 没有传值的时候给默认值
                searchSourceBuilder.sort("hotScore",SortOrder.DESC);
            }
        }

        // 构建高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.postTags("</span>");
        highlightBuilder.preTags("<span style=color:red>");

        searchSourceBuilder.highlighter(highlightBuilder);

        //  设置品牌聚合
        TermsAggregationBuilder termsAggregationBuilder =        AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));

        searchSourceBuilder.aggregation(termsAggregationBuilder);


        //  设置平台属性聚合
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));


        // 结果集过滤
        searchSourceBuilder.fetchSource(new String[]{"id","defaultImg","title","price"},null);

        SearchRequest searchRequest = new SearchRequest("goods");
        //searchRequest.types("_doc");
        searchRequest.source(searchSourceBuilder);
        System.out.println("dsl:"+searchSourceBuilder.toString());
        return searchRequest;
    }
}
