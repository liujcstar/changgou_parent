package com.changgou.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.dao.EsMapper;
import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.service.SkuSearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SkuSearchServiceImpl implements SkuSearchService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private EsMapper esMapper;

    @Autowired
    private SkuFeign skuFeign;

    //创建索引
    @Override
    public void createMapping() {

        //创建索引
        elasticsearchTemplate.createIndex(SkuInfo.class);

        //创建映射
        elasticsearchTemplate.putMapping(SkuInfo.class);

    }


    //预热索引库
    @Override
    public void importAll() {

        //传递all参数，直接查询所有sku
        List<Sku> skuList = skuFeign.findSkuBySpuId("all");


        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuList), SkuInfo.class);

        for (SkuInfo skuInfo : skuInfoList) {

            Map map = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(map);

        }

        esMapper.saveAll(skuInfoList);

    }

    //上架商品信息
    @Override
    public void importBySku(List<Sku> skuList) {

        if (skuList == null) {
            throw new RuntimeException("对应spu下没有sku商品");
        }


        //更新spec和specMap信息
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuList), SkuInfo.class);
        for (SkuInfo skuInfo : skuInfoList) {

            Map map = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(map);

        }

        esMapper.saveAll(skuInfoList);

    }


    //商品下架
    @Override
    public void delBySkuList(List<Sku> skuList) {

        if (skuList == null) {
            throw new RuntimeException("对应spu下没有sku商品");
        }

        for (Sku sku : skuList) {
            esMapper.deleteById(Long.parseLong(sku.getId()));
        }

    }



    //高级检索
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {

        if (searchMap != null) {

            //nativeSearchQueryBuilder 封装的是查询条件
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

            //boolQuery  构建
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();


            //1. 根据keyword关键字检索
            if (StringUtils.isNotBlank(searchMap.get("keywords"))) {

                boolQuery.must(QueryBuilders.matchQuery("name", searchMap.get("keywords")));
            }

            //2. 根据品牌查询
            if (StringUtils.isNotBlank(searchMap.get("brand"))){
                boolQuery.filter(QueryBuilders.termQuery("brandName",searchMap.get("brand") ));
            }

            //3. 根据价格范围查询
            if (StringUtils.isNotBlank(searchMap.get("price"))){
                String price = searchMap.get("price");
                if (!price.contains("-")){
                    boolQuery.filter(QueryBuilders.rangeQuery("price").gte(price));
                }else {

                    String[] split = price.split("-");

                    boolQuery.filter(QueryBuilders.rangeQuery("price").gte(split[0]).lte(split[1]));
                }
            }

            //4. 根据规格查询。这里有一个查询对象属性的操作，查询对象属性有一套固定的解决方案
            Set<String> keySet = searchMap.keySet();
            for (String keyName : keySet) {
                //判断是否是规格信息
                if (keyName.startsWith("spec_")){

                    //处理后的查询参数
                    String value = searchMap.get(keyName).replace("%2B","+");

                    String propertiesName = "specMap." + keyName.substring(5)+".keyword";
                    boolQuery.filter(QueryBuilders.termQuery(propertiesName, value));

                }

            }

            //5. 分页查询
            String pageNum = searchMap.get("pageNum"); //当前页
            String pageSize = searchMap.get("pageSize"); //每页显示多少条
            if (StringUtils.isEmpty(pageNum)){
                pageNum ="1";
            }
            if (StringUtils.isEmpty(pageSize)){
                pageSize="20";
            }
            //设置分页
            //第一个参数:当前页 是从0开始
            //第二个参数:每页显示多少条
            nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.parseInt(pageNum)-1,Integer.parseInt(pageSize)));


            //6. 根据品牌进行聚合查询(聚合查询的数据必须不可以是对象，text类型可以转换为keyword类型)
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("brandName").field("brandName"));

            //7. 根据规格进行聚合
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("spec").field("spec.keyword"));


            //8.根据价格进行排序
            if (StringUtils.isNotBlank(searchMap.get("sortFile"))&&StringUtils.isNotBlank(searchMap.get("sortRule"))){

                if (searchMap.get("sortRule").equals("ASC")&&searchMap.get("sortFile").equals("price")){
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
                }

                if (searchMap.get("sortRule").equals("DESC")&&searchMap.get("sortFile").equals("price")){
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
                }
            }

            //9. 高亮查询
            HighlightBuilder.Field fields = new HighlightBuilder.Field("name")
                    .preTags("<font style='color:red'>")
                    .postTags("</font>");
            nativeSearchQueryBuilder.withHighlightFields(fields);



            //封装条件
            nativeSearchQueryBuilder.withQuery(boolQuery);


            //查询并返回结果(参数二： 查询的对象)
            AggregatedPage<SkuInfo> resultInfo = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper() {


                //封装返回对象

                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    //处理查询的结果
                    //searchResponse 封装了查询结果的内容
                    //查询结果操作
                    List<T> list = new ArrayList<>();

                    //获取查询命中结果数据
                    SearchHits hits = searchResponse.getHits();
                    if (hits != null) {
                        //有查询结果
                        for (SearchHit hit : hits) {


                            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                            //获取对应的高亮 字段
                            HighlightField name = highlightFields.get("name");

                            Text[] fragments = name.fragments();


                            //SearchHit转换为skuinfo
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);

                            //封装高亮字段
                            skuInfo.setName(fragments[0].toString());

                            list.add((T) skuInfo);
                        }
                    }

                    /*
                     * 1.list：返回的数据
                     * 2.pageable：分页参数
                     * 3.总记录数
                     * 4.分组数据
                     */
                    return new AggregatedPageImpl<T>(list, pageable,
                            hits.getTotalHits(), searchResponse.getAggregations());
                }
            });

            HashMap<String, Object> map = new HashMap<String, Object>();
            //总条数
            map.put("total", resultInfo.getTotalElements());
            //总页数
            map.put("totalPages", resultInfo.getTotalPages());
            //数据
            map.put("rows", resultInfo.getContent());
            //商品名聚合数据
            StringTerms brandTerms = (StringTerms) resultInfo.getAggregation("brandName");

            ArrayList brandList = new ArrayList();

            List<StringTerms.Bucket> buckets = brandTerms.getBuckets();
            for (StringTerms.Bucket bucket : buckets) {
                brandList.add(bucket.getKeyAsString());
            }
            map.put("brandList",brandList );


            //规格聚合数据
            StringTerms specTerms= (StringTerms) resultInfo.getAggregation("spec");
            List<String> specList = specTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());

            //格式化specList规格参数
            Map<String, Set<String>> stringSetMap = this.formartSpec(specList);
            map.put("specList",stringSetMap);


            //每页有多少条数据
            map.put("pageSize",pageSize);

            //当前页
            map.put("pageNum", pageNum);


            return map;
        }
        return null;
    }


    /**
     * 原有数据
     *  [
     *         "{'颜色': '黑色', '尺码': '平光防蓝光-无度数电脑手机护目镜'}",
     *         "{'颜色': '红色', '尺码': '150度'}",
     *         "{'颜色': '黑色', '尺码': '150度'}",
     *         "{'颜色': '黑色'}",
     *         "{'颜色': '红色', '尺码': '100度'}",
     *         "{'颜色': '红色', '尺码': '250度'}",
     *         "{'颜色': '红色', '尺码': '350度'}",
     *         "{'颜色': '黑色', '尺码': '200度'}",
     *         "{'颜色': '黑色', '尺码': '250度'}"
     *     ]
     *
     *    需要的数据格式
     *    {
     *        颜色:[黑色,红色],
     *        尺码:[100度,150度]
     *    }
     */
    public Map<String,Set<String>> formartSpec(List<String> specList){
        Map<String,Set<String>> resultMap = new HashMap<>();
        if (specList!=null && specList.size()>0){
            for (String specJsonString : specList) {
                //将json数据转换为map
                Map<String,String> specMap = JSON.parseObject(specJsonString, Map.class);
                for (String specKey : specMap.keySet()) {
                    Set<String> specSet = resultMap.get(specKey);
                    if (specSet == null){
                        specSet = new HashSet<String>();
                    }
                    //将规格的值放入set中
                    specSet.add(specMap.get(specKey));
                    //将set放入map中
                    resultMap.put(specKey,specSet);
                }
            }
        }
        return resultMap;
    }
}
