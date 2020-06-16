package com.changgou.service;

import com.changgou.goods.pojo.Sku;

import java.util.List;
import java.util.Map;

public interface SkuSearchService {

    //创建索引

    void createMapping();

    //更新全部数据
    void importAll();


    void importBySku(List<Sku> skuList);

    void delBySkuList(List<Sku> skuBySpuId);

    Map<String,Object> search(Map<String, String> map);
}
