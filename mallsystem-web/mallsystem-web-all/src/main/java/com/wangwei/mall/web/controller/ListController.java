package com.wangwei.mall.web.controller;

import com.wangwei.mall.common.result.Result;
import com.wangwei.mall.model.list.SearchParam;
import com.wangwei.mall.web.inner.service.IListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ListController {


    @Autowired
    private IListService listService;
    /**
     * 列表搜索
     * @param searchParam
     * @return
     */
    @GetMapping("list.html")
    public String search(SearchParam searchParam, Model model) {
        Result<Map> result = listService.list(searchParam);
        model.addAllAttributes(result.getData());

        // 记录拼接url；
        String urlParam = makeUrlParam(searchParam);
        //处理品牌条件回显
        String trademarkParam = this.makeTrademark(searchParam.getTrademark());
        //处理平台属性条件回显
        List<Map<String, String>> propsParamList = this.makeProps(searchParam.getProps());
        //处理排序
        Map<String,Object> orderMap = this.dealOrder(searchParam.getOrder());

        model.addAttribute("searchParam",searchParam);
        model.addAttribute("urlParam",urlParam);
        model.addAttribute("trademarkParam",trademarkParam);
        model.addAttribute("propsParamList",propsParamList);
        model.addAttribute("orderMap",orderMap);

        return "list/index";
    }

    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder urlParam = new StringBuilder();
        // 判断关键字
        if (searchParam.getKeyword()!=null){
            urlParam.append("keyword=").append(searchParam.getKeyword());
        }
        // 判断一级分类
        if (searchParam.getCategory1Id()!=null){
            urlParam.append("category1Id=").append(searchParam.getCategory1Id());
        }
        // 判断二级分类
        if (searchParam.getCategory2Id()!=null){
            urlParam.append("category2Id=").append(searchParam.getCategory2Id());
        }
        // 判断三级分类
        if (searchParam.getCategory3Id()!=null){
            urlParam.append("category3Id=").append(searchParam.getCategory3Id());
        }
        // 处理品牌
        if(searchParam.getTrademark()!=null){
            if (urlParam.length()>0){
                urlParam.append("&trademark=").append(searchParam.getTrademark());
            }
        }
        // 判断平台属性值
        if (null != searchParam.getProps()){
            for (String prop : searchParam.getProps()) {
                if (urlParam.length() > 0){
                    urlParam.append("&props=").append(prop);
                }
            }
        }
        return "list.html?" + urlParam.toString();
    }

    /**
     * 处理品牌条件回显
     * @param trademark
     * @return
     */
    private String makeTrademark(String trademark) {
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = StringUtils.split(trademark, ":");
            if (split != null && split.length == 2) {
                return "品牌：" + split[1];
            }
        }
        return "";
    }

    /**
     * 处理平台属性条件回显
     * @param props
     * @return
     */
// 处理平台属性
    private List<Map<String, String>> makeProps(String[] props) {
        List<Map<String, String>> list = new ArrayList<>();
        // 2:v:n
        if (props!=null && props.length!=0){
            for (String prop : props) {
                String[] split = StringUtils.split(prop, ":");
                if (split!=null && split.length==3){
                    // 声明一个map
                    HashMap<String, String> map = new HashMap<String,String>();
                    map.put("attrId",split[0]);
                    map.put("attrValue",split[1]);
                    map.put("attrName",split[2]);
                    list.add(map);
                }
            }
        }
        return list;
    }
    /**
     * 处理排序
     * @param order
     * @return
     */
    private Map<String, Object> dealOrder(String order) {
        Map<String,Object> orderMap = new HashMap<>();
        if(!StringUtils.isEmpty(order)) {
            String[] split = StringUtils.split(order, ":");
            if (split != null && split.length == 2) {
                // 传递的哪个字段
                orderMap.put("type", split[0]);
                // 升序降序
                orderMap.put("sort", split[1]);
            }
        }else {
            orderMap.put("type", "1");
            orderMap.put("sort", "asc");
        }
        return orderMap;
    }
}
