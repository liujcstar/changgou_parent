package com.changgou.service.impl;

import com.changgou.dao.EsMapper;
import com.changgou.pojo.SkuInfo;
import com.changgou.service.MappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

@Service
public class MappingServiceImpl implements MappingService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private EsMapper esMapper;

    //创建索引
    @Override
    public void createMapping() {

        elasticsearchTemplate.createIndex(SkuInfo.class);


    }


    //预热索引库
    @Override
    public void updateAll() {

    }
}
