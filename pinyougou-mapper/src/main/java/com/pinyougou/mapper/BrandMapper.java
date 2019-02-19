package com.pinyougou.mapper;

import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.Brand;

import java.util.List;
import java.util.Map;

/**
 * BrandMapper 数据访问接口
 * @date 2019-01-09 15:45:18
 * @version 1.0
 */
public interface BrandMapper extends Mapper<Brand>{

    List<Brand> findAll(Brand brand);


    List<Map<String, Object>> brandIDandName();
}