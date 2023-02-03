package com.wangwei.mall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangwei.mall.common.cache.MallCache;
import com.wangwei.mall.common.constant.RedisConst;
import com.wangwei.mall.model.product.*;
import com.wangwei.mall.product.mapper.*;
import com.wangwei.mall.product.service.ManagerService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ManagerServiceImpl implements ManagerService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrInfoMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuPosterMapper spuPosterMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;


    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        return baseCategory2Mapper.selectList(new QueryWrapper<BaseCategory2>().eq("category1_id", category1Id));
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        return baseCategory3Mapper.selectList(new QueryWrapper<BaseCategory3>().eq("category2_id", category2Id));
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id, category2Id, category3Id);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() != null) {
            //修改
            baseAttrInfoMapper.updateById(baseAttrInfo);
        } else {
            baseAttrInfoMapper.insert(baseAttrInfo);
        }
        // baseAttrValue 平台属性值
        // 修改：通过先删除{baseAttrValue}，在新增的方式！
        // 删除条件：baseAttrValue.attrId = baseAttrInfo.id
        QueryWrapper queryWrapper = new QueryWrapper<BaseAttrValue>();
        queryWrapper.eq("attr_id", baseAttrInfo.getId());
        baseAttrValueMapper.delete(queryWrapper);

        // 获取页面传递过来的所有平台属性值数据
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (attrValueList != null && attrValueList.size() > 0) {
            // 循环遍历
            attrValueList.forEach(baseAttrValue -> {
                // 获取平台属性Id 给attrId
                baseAttrValue.setAttrId(baseAttrInfo.getId()); // ?
                baseAttrValueMapper.insert(baseAttrValue);
            });
        }

    }

    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        BaseAttrInfo attrInfo = baseAttrInfoMapper.selectById(attrId);
        attrInfo.setAttrValueList(getAttrValueList(attrId));
        return attrInfo;
    }

    @Override
    public IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo) {
        QueryWrapper<SpuInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id", spuInfo.getCategory3Id());
        wrapper.orderByDesc("id");
        return spuInfoMapper.selectPage(spuInfoPage, wrapper);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {

        //1. 保存spu
        spuInfoMapper.insert(spuInfo);
        //2. 保存图片
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (!CollectionUtils.isEmpty(spuImageList) || spuImageList.size() > 0) {
            spuImageList.forEach(image -> {
                image.setSpuId(spuInfo.getId());
                spuImageMapper.insert(image);
            });
        }
        //3. 保存海报
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        if (!CollectionUtils.isEmpty(spuPosterList) || spuPosterList.size() > 0) {
            spuPosterList.forEach(poster -> {
                poster.setSpuId(spuInfo.getId());
                spuPosterMapper.insert(poster);
            });
        }

        //4. 保存销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (!CollectionUtils.isEmpty(spuSaleAttrList) || spuSaleAttrList.size() > 0) {
            spuSaleAttrList.forEach(spuSaleAttr -> {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);

                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList) || spuSaleAttrValueList.size() > 0) {
                    spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    });
                }
            });
        }


    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {

        QueryWrapper<SpuImage> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        return spuImageMapper.selectList(wrapper);
    }


    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
    /*
        skuInfo 库存单元表 --- spuInfo！
        skuImage 库存单元图片表 --- spuImage!
        skuSaleAttrValue sku销售属性值表{sku与销售属性值的中间表} --- skuInfo ，spuSaleAttrValue
        skuAttrValue sku与平台属性值的中间表 --- skuInfo ，baseAttrValue
     */
        skuInfoMapper.insert(skuInfo);
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size() > 0) {
            // 循环遍历
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            }
        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        // 调用判断集合方法
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }
    }


    @Override
    public IPage<SkuInfo> getPage(Page<SkuInfo> pageParam) {
        QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");

        return skuInfoMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    @Transactional
    public void onSale(Long skuId) {
        // 更改销售状态
        SkuInfo skuInfoUp = new SkuInfo();
        skuInfoUp.setId(skuId);
        skuInfoUp.setIsSale(1);
        skuInfoMapper.updateById(skuInfoUp);
    }

    @Override
    @Transactional
    public void cancelSale(Long skuId) {
        // 更改销售状态
        SkuInfo skuInfoUp = new SkuInfo();
        skuInfoUp.setId(skuId);
        skuInfoUp.setIsSale(0);
        skuInfoMapper.updateById(skuInfoUp);
    }

    @MallCache(prefix = RedisConst.SKUKEY_PREFIX)
    @Override
    public SkuInfo getSkuInfo(Long skuId) {

        return getSkuInfoRedisson(skuId);
    }


    private List<BaseAttrValue> getAttrValueList(Long attrId) {
        QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_id", attrId);
        return baseAttrValueMapper.selectList(wrapper);
    }

    @MallCache(prefix = "categoryView")
    @Override
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }


    /**
     * 获取sku价格
     *
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        //  不需要将数据放入缓存！
        RLock lock = redissonClient.getLock(skuId + ":lock");
        //  上锁
        lock.lock();
        SkuInfo skuInfo = null;
        BigDecimal price = new BigDecimal(0);
        try {
            QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
            skuInfoQueryWrapper.eq("id",skuId);
            skuInfoQueryWrapper.select("price");
            skuInfo = skuInfoMapper.selectOne(skuInfoQueryWrapper);
            if (skuInfo!=null){
                price = skuInfo.getPrice();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //  解锁！
            lock.unlock();
        }
        //  返回价格
        return price;
    }


    @MallCache(prefix = "spuSaleAttrListCheckBySku")
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId, spuId);
    }


    @MallCache(prefix = "skuValueIdsMap")
    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        Map<Object, Object> map = new HashMap<>();
        // key = 125|123 ,value = 37
        List<Map> mapList = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
        if (mapList != null && mapList.size() > 0) {
            // 循环遍历
            for (Map skuMap : mapList) {
                // key = 125|123 ,value = 37
                map.put(skuMap.get("value_ids"), skuMap.get("sku_id"));
            }
        }
        return map;
    }

    @MallCache(prefix = "spuPosterBySpuId")
    @Override
    public List<SpuPoster> findSpuPosterBySpuId(Long spuId) {
        QueryWrapper<SpuPoster> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("spu_id", spuId);
        List<SpuPoster> spuPosterList = spuPosterMapper.selectList(spuInfoQueryWrapper);
        return spuPosterList;
    }


    @MallCache(prefix = "attrList")
    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {

        return baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
    }




    /**
     * 使用redis 做分布式锁
     *
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoRedis(Long skuId) {

        SkuInfo skuInfo = null;


        try {

            //缓存存储数据 定义key sku:skuId:info
            String key = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;

            //1. 获取缓存数据
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(key);

            //2. 如果缓存中取出的数据为空 去查询db  查询db 防止出现问题 需上锁

            if (null == skuInfo) {
                /**
                 * 3. 查数据库 上锁
                 *      redis
                 *      redisson
                 */
                // 定义锁的key
                String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;

                //定义锁的值
                String uuid = UUID.randomUUID().toString().replace("-", "");

                //上锁
                Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);

                if (lock) {
                    //上锁了
                    log.info("--------------------获取到分布式锁--------------------");
                    //4. 从数据库中查询数据
                    skuInfo = getSkuInfoDB(skuId);

                    //5. 如果从数据库中获取到的数据也为空 ,为了避免缓存穿透 将空对象也放入缓存中

                    if (null == skuInfo) {
                        redisTemplate.opsForValue().set(key, skuInfo, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                        return skuInfo;
                    }

                    //查询数据库有值 放入缓存
                    redisTemplate.opsForValue().set(key, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);


                    //6. 解锁：使用lua 脚本解锁
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    // 设置lua脚本返回的数据类型
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    // 设置lua脚本返回类型为Long
                    redisScript.setResultType(Long.class);
                    redisScript.setScriptText(script);
                    // 删除key 所对应的 value
                    redisTemplate.execute(redisScript, Arrays.asList(lockKey), uuid);

                    return skuInfo;
                } else {
                    //7. 其他线程等待
                    Thread.sleep(1000);
                    return getSkuInfo(skuId);
                }

            } else {

                //8. 如果缓存中查到了数据 返回
                return skuInfo;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        // 为了防止缓存宕机：从数据库中获取数据
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoDB(Long skuId) {


        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo != null) {
            QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
            skuImageQueryWrapper.eq("sku_id", skuId);
            List<SkuImage> skuImageList = skuImageMapper.selectList(skuImageQueryWrapper);
            skuInfo.setSkuImageList(skuImageList);
        }

        return skuInfo;
    }

    /**
     * 使用redisson 实现分布式锁
     *
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoRedisson(Long skuId) {

        SkuInfo skuInfo = null;
        try {
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;

            //1. 查缓存
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);

            if (null == skuInfo) {
                //2. 缓存中没有数据,查数据库 为防止发生缓存问题加锁
                //定义锁key
                String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(lockKey);

                //3. 尝试加锁
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (res) {
                    try {
                        //4. 获取到锁  查数据库
                        skuInfo = getSkuInfoDB(skuId);

                        if (null == skuInfo) {
                            //数据库查不到也要添加缓存,防止缓存穿透
                            redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return skuInfo;
                        }
                        //数据库中查到数据
                        redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);

                        return skuInfo;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        //5. 释放锁
                        lock.unlock();
                    }

                } else {
                    //其他线程等待
                    Thread.sleep(1000);
                    return getSkuInfo(skuId);
                }

            } else {
                return skuInfo;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        // 为了防止缓存宕机：从数据库中获取数据
        return getSkuInfoDB(skuId);

    }



    @MallCache(prefix = "category")
    @Override
    public List<JSONObject> getBaseCategoryList() {

        //声明json集合
        ArrayList<JSONObject> list  = new ArrayList<>();

        //声明获取所有分类数据集合
        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);

        //循环上面的集合并按一级分类id进行分组
        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        int index = 1;

        //获取一级分类下的所有数据
        for (Map.Entry<Long, List<BaseCategoryView>> entry1 : category1Map.entrySet()) {
            //获取一级分类id
            Long category1Id = entry1.getKey();
            // 获取一级分类下面的所有集合
            List<BaseCategoryView> category2List1  = entry1.getValue();
            JSONObject category1 = new JSONObject();
            category1.put("index", index);
            category1.put("categoryId",category1Id);
            //一级分类名称
            category1.put("categoryName",category2List1.get(0).getCategory1Name());
            //变量迭代
            index++;
            // 循环获取二级分类数据
            Map<Long, List<BaseCategoryView>> category2Map  = category2List1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            // 声明二级分类对象集合
            List<JSONObject> category2Child = new ArrayList<>();
            // 循环遍历
            for (Map.Entry<Long, List<BaseCategoryView>> entry2 : category2Map.entrySet()) {
                // 获取二级分类Id
                Long category2Id  = entry2.getKey();
                // 获取二级分类下的所有集合
                List<BaseCategoryView> category3List  = entry2.getValue();
                // 声明二级分类对象
                JSONObject category2 = new JSONObject();

                category2.put("categoryId",category2Id);
                category2.put("categoryName",category3List.get(0).getCategory2Name());
                // 添加到二级分类集合
                category2Child.add(category2);

                List<JSONObject> category3Child = new ArrayList<>();

                // 循环三级分类数据
                category3List.stream().forEach(category3View -> {
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId",category3View.getCategory3Id());
                    category3.put("categoryName",category3View.getCategory3Name());

                    category3Child.add(category3);
                });

                // 将三级数据放入二级里面
                category2.put("categoryChild",category3Child);

            }
            // 将二级数据放入一级里面
            category1.put("categoryChild",category2Child);
            list.add(category1);

        }


        return list;
    }

    @Override
    public BaseTrademark getTrademarkByTmId(Long tmId) {
        return baseTrademarkMapper.selectById(tmId);
    }


}
