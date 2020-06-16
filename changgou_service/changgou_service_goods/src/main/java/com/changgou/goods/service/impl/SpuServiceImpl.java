package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.changgou.goods.dao.*;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.changgou.order.pojo.OrderItem;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpuServiceImpl implements SpuService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SkuMapper skuMapper;

    /**
     * 查询全部列表
     *
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id) {
        return spuMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     *
     * @param spu
     */
    @Override
    public void add(Goods goods) {

        //首先存储Spu,商品、类型
        Spu spu = goods.getSpu();
        spu.setId(String.valueOf(idWorker.nextId()));

        //该商品是否已经删除
        spu.setIsDelete("0");

        //该商品是否上架
        spu.setIsMarketable("0");

        //该商品审核状态
        spu.setStatus("0");

        spuMapper.insertSelective(spu);

        //添加sku商品详情
        addSku(goods);
    }

    //添加Sku
    private void addSku(Goods goods) {

        List<Sku> skuList = goods.getSkuList();

        //获取品牌名称
        Spu spu = goods.getSpu();
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());

        //设置品牌与分类的关联
        Integer brandId = spu.getBrandId();
        Integer category3Id = spu.getCategory3Id();


        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setBrandId(brandId);
        categoryBrand.setCategoryId(category3Id);

        //查询是否存在该关联关系
        CategoryBrand sqlCategoryBrand = categoryBrandMapper.selectOne(categoryBrand);
        if (sqlCategoryBrand == null) {
            //建立关联关系
            categoryBrandMapper.insert(categoryBrand);
        }


        //获取种类名称
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());


        //判断是否有sku商品信息
        if (skuList != null) {

            for (Sku sku : skuList) {

                //设置id
                sku.setId(String.valueOf(idWorker.nextId()));

                //设置spuid
                sku.setSpuId(spu.getId());

                if (brand!=null) {

                    //设置商品名
                    sku.setBrandName(brand.getName());
                }

                if (category!=null) {
                    //设置种类名
                    sku.setCategoryName(category.getName());

                    //商品分类id
                    sku.setCategoryId(category.getId());
                }

                //设置名称
                String spec = sku.getSpec();
                if (StringUtils.isBlank(spec)) {
                    //避免数据库中存入null值，避免空指针异常
                    spec = "{}";
                }

                //sku存入的名称为spu的名称加上sku的spec规格
                Map<String, String> map = JSON.parseObject(spec, Map.class);

                if (map != null && map.size() > 0) {
                    String name = spu.getName();
                    for (String value : map.values()) {
                        name += value + " ";
                    }
                    sku.setName(name);
                }



                //设置添加更新时间
                sku.setCreateTime(new Date());
                sku.setUpdateTime(new Date());


                skuMapper.insertSelective(sku);
            }

        }

    }


    /**
     * 修改
     *
     * @param spu
     */
    @Override
    public void update(Goods goods) {

        //直接修改Spu信息
        Spu spu = goods.getSpu();
        spuMapper.updateByPrimaryKey(spu);

        //修改sku数据信息（直接删除原来的数据信息，然后再添加sku集合，不进行修改）
        //删除原来的信息
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",spu.getId() );
        skuMapper.deleteByExample(example);

        //添加新的信息
        addSku(goods);


    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(String id) {
        //保证商品信息没有删除

        Spu spu = spuMapper.selectByPrimaryKey(id);

        if (spu==null){
            throw new  RuntimeException("商品不存在");
        }

        if (!"0".equals(spu.getIsMarketable())){
            throw new  RuntimeException("商品处于上架状态");
        }

        //修改删除状态
        spu.setIsDelete("1");

        spuMapper.updateByPrimaryKeySelective(spu);

    }


    /**
     * 条件查询
     *
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return (Page<Spu>) spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     *
     * @param searchMap 查询条件
     * @param page      页码
     * @param size      页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        return (Page<Spu>) spuMapper.selectByExample(example);
    }

    /**
     * 根据spuId查询Goods组合商品
     *
     * @param id
     */
    @Override
    public Goods findGoods(String id) {

        Goods goods = new Goods();
        //根据传入的SpuID查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu != null) {
            goods.setSpu(spu);

            //根据外键关系查询Sku集合
            Example example = new Example(Sku.class);

            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("spuId", spu.getId());

            List<Sku> skus = skuMapper.selectByExample(example);

            goods.setSkuList(skus);
        }

        return goods;

    }

    /**
     * 修改商品上架信息
     * @param id
     */
    @Override
    public void putaway(String id) {

        //保证商品信息没有删除

        Spu spu = spuMapper.selectByPrimaryKey(id);

        if (spu==null){
            throw new  RuntimeException("商品不存在");
        }

        if (!"0".equals(spu.getIsDelete())){
            throw new  RuntimeException("商品已删除");
        }

        //保证商品信息通过审核
        if (!"1".equals(spu.getStatus())){
            throw new  RuntimeException("商品未通过审核");
        }

        //修改上架状态
        spu.setIsMarketable("1");

        spuMapper.updateByPrimaryKeySelective(spu);

    }

    /**
     * 下架商品
     * @param id
     */
    @Override
    public void pullaway(String id) {

        //保证商品信息没有删除

        Spu spu = spuMapper.selectByPrimaryKey(id);

        if (spu==null){
            throw new  RuntimeException("商品不存在");
        }

        if (!"0".equals(spu.getIsDelete())){
            throw new  RuntimeException("商品已删除");
        }


        //修改上架状态
        spu.setIsMarketable("0");

        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 审核商品
     * @param id
     */
    @Override
    public void audit(String id) {

        Spu spu = spuMapper.selectByPrimaryKey(id);

        if (spu==null){
            throw new  RuntimeException("商品不存在");
        }

        if (!"0".equals(spu.getIsDelete())){
            throw new  RuntimeException("商品已删除");
        }

        //修改审核信息
        spu.setStatus("1");

        spuMapper.updateByPrimaryKeySelective(spu);
    }


    /**
     * 商品审核不成功
     * @param id
     */
    @Override
    public void unAudit(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);

        if (spu==null){
            throw new  RuntimeException("商品不存在");
        }

        if (!"0".equals(spu.getIsDelete())){
            throw new  RuntimeException("商品已删除");
        }

        if (!"0".equals(spu.getIsMarketable())){
            throw new  RuntimeException("商品处于上架状态");
        }

        //修改审核信息
        spu.setStatus("0");


        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 还原商品
     * @param id
     */
    @Override
    public void restore(String id) {

        Spu spu = spuMapper.selectByPrimaryKey(id);

        if (spu==null){
            throw new  RuntimeException("商品不存在");
        }

        //还原商品
        spu.setIsDelete("0");

        spuMapper.updateByPrimaryKeySelective(spu);


    }

    /**
     * 物理删除商品
     * @param id
     */
    @Override
    public void relDelete(String id) {
        spuMapper.deleteByPrimaryKey(id);
    }





    /**
     * 构建查询对象
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 主键
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }
            // 货号
            if (searchMap.get("sn") != null && !"".equals(searchMap.get("sn"))) {
                criteria.andEqualTo("sn", searchMap.get("sn"));
            }
            // SPU名
            if (searchMap.get("name") != null && !"".equals(searchMap.get("name"))) {
                criteria.andLike("name", "%" + searchMap.get("name") + "%");
            }
            // 副标题
            if (searchMap.get("caption") != null && !"".equals(searchMap.get("caption"))) {
                criteria.andLike("caption", "%" + searchMap.get("caption") + "%");
            }
            // 图片
            if (searchMap.get("image") != null && !"".equals(searchMap.get("image"))) {
                criteria.andLike("image", "%" + searchMap.get("image") + "%");
            }
            // 图片列表
            if (searchMap.get("images") != null && !"".equals(searchMap.get("images"))) {
                criteria.andLike("images", "%" + searchMap.get("images") + "%");
            }
            // 售后服务
            if (searchMap.get("saleService") != null && !"".equals(searchMap.get("saleService"))) {
                criteria.andLike("saleService", "%" + searchMap.get("saleService") + "%");
            }
            // 介绍
            if (searchMap.get("introduction") != null && !"".equals(searchMap.get("introduction"))) {
                criteria.andLike("introduction", "%" + searchMap.get("introduction") + "%");
            }
            // 规格列表
            if (searchMap.get("specItems") != null && !"".equals(searchMap.get("specItems"))) {
                criteria.andLike("specItems", "%" + searchMap.get("specItems") + "%");
            }
            // 参数列表
            if (searchMap.get("paraItems") != null && !"".equals(searchMap.get("paraItems"))) {
                criteria.andLike("paraItems", "%" + searchMap.get("paraItems") + "%");
            }
            // 是否上架
            if (searchMap.get("isMarketable") != null && !"".equals(searchMap.get("isMarketable"))) {
                criteria.andEqualTo("isMarketable", searchMap.get("isMarketable"));
            }
            // 是否启用规格
            if (searchMap.get("isEnableSpec") != null && !"".equals(searchMap.get("isEnableSpec"))) {
                criteria.andEqualTo("isEnableSpec", searchMap.get("isEnableSpec"));
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andEqualTo("isDelete", searchMap.get("isDelete"));
            }
            // 审核状态
            if (searchMap.get("status") != null && !"".equals(searchMap.get("status"))) {
                criteria.andEqualTo("status", searchMap.get("status"));
            }

            // 品牌ID
            if (searchMap.get("brandId") != null) {
                criteria.andEqualTo("brandId", searchMap.get("brandId"));
            }
            // 一级分类
            if (searchMap.get("category1Id") != null) {
                criteria.andEqualTo("category1Id", searchMap.get("category1Id"));
            }
            // 二级分类
            if (searchMap.get("category2Id") != null) {
                criteria.andEqualTo("category2Id", searchMap.get("category2Id"));
            }
            // 三级分类
            if (searchMap.get("category3Id") != null) {
                criteria.andEqualTo("category3Id", searchMap.get("category3Id"));
            }
            // 模板ID
            if (searchMap.get("templateId") != null) {
                criteria.andEqualTo("templateId", searchMap.get("templateId"));
            }
            // 运费模板id
            if (searchMap.get("freightId") != null) {
                criteria.andEqualTo("freightId", searchMap.get("freightId"));
            }
            // 销量
            if (searchMap.get("saleNum") != null) {
                criteria.andEqualTo("saleNum", searchMap.get("saleNum"));
            }
            // 评论数
            if (searchMap.get("commentNum") != null) {
                criteria.andEqualTo("commentNum", searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
