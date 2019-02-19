package com.pinyougou.mapper;

import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.Specification;

import java.util.List;
import java.util.Map;

/**
 * SpecificationMapper 数据访问接口
 * @date 2019-01-09 15:45:18
 * @version 1.0
 */
public interface SpecificationMapper extends Mapper<Specification>{

    List<Specification> findAll(Specification specification);


    List<Map<String, Object>> specIDandName();
}