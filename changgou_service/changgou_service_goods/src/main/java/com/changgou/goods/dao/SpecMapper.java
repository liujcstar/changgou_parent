package com.changgou.goods.dao;

import com.changgou.goods.pojo.Spec;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

@Repository
public interface SpecMapper extends Mapper<Spec> {

    @Select("select * from `tb_spec` where template_id in (select distinct template_id from `tb_category` where name = #{categoryName})")
    List<Map> findSpecListByCategoryName(@Param("categoryName") String categoryName);
}
