package com.changgou.goods.pojo;

import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "")
public class BrandCategory implements Serializable{
    private Integer brandId;
    private Integer categoryId;

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
}
