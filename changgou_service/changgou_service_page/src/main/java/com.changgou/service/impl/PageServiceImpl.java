package com.changgou.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.service.PageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {

    @Value("${pagepath}")
    private String pagePath;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 创建静态页面
     * @param spuId
     */
    @Override
    public void createPage(String spuId) {


        //1. 获取context对象
        Context context = new Context();
            //封装数据
        Map<String,Object> itemData = this.getItemData(spuId);
        context.setVariables(itemData);

        //2. 获取文件夹所在位置，并判断是否存在

        File dir = new File(pagePath);
        if (!dir.exists()){
            //不存在就创建目录
            dir.mkdirs();
        }
        //3. 定义输出流，完成静态界面的生成

        File file = new File(pagePath+"/"+spuId+".html");
        Writer out = null;
        try {

            out = new PrintWriter(file);

            /* 1：文件名称（默认在template目录下）
             * 2：内容
             * 3：输出流（将静态文件输出到哪里）
             */
            templateEngine.process("item", context,out);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    //准备数据
    private Map<String, Object> getItemData(String spuId) {
        HashMap<String, Object> itemData = new HashMap<>();

        //查询spu信息
        Spu spu = (Spu) spuFeign.findById(spuId).getData();
        itemData.put("spu",spu );

        //封装图片信息
        String images = spu.getImages();
        if (StringUtils.isNotEmpty(images)){
            itemData.put("imageList",spu.getImages().split(","));

        }

        //封装种类名称
        Category category1 = (Category) categoryFeign.findById(spu.getCategory1Id()).getData();
        itemData.put("category1", category1);

        Category category2 = (Category) categoryFeign.findById(spu.getCategory2Id()).getData();
        itemData.put("category2", category2);

        Category category3 = (Category) categoryFeign.findById(spu.getCategory3Id()).getData();
        itemData.put("category3", category3);



        //查询sku信息
        List<Sku> skuList = skuFeign.findSkuBySpuId(spuId);
        itemData.put("skuList", skuList);


        //封装商品规格信息
        String specItems = spu.getSpecItems();
        itemData.put("specificationList", JSON.parseObject(specItems,Map.class));

        return itemData;
    }



}
