package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface BrandMapper extends Mapper<Brand> {

    @Select("select * from `tb_brand` where id in (select brand_id from `tb_category_brand` where category_id in (select id from `tb_category` where name =#{categoryName}))")
    List<Brand> findBrandListByCategoryName(String categoryName);
}
