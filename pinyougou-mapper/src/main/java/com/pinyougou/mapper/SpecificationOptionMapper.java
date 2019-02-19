package com.pinyougou.mapper;

import com.pinyougou.pojo.Specification;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.SpecificationOption;

/**
 * SpecificationOptionMapper 数据访问接口
 * @date 2019-01-09 15:45:18
 * @version 1.0
 */
public interface SpecificationOptionMapper extends Mapper<SpecificationOption>{


    public void save(Specification specification);
}