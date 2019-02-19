package com.pinyougou.mapper;

import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.TypeTemplate;

import java.util.List;
import java.util.Map;

/**
 * TypeTemplateMapper 数据访问接口
 * @date 2019-01-09 15:45:18
 * @version 1.0
 */
public interface TypeTemplateMapper extends Mapper<TypeTemplate>{


    List<TypeTemplate> findPage(TypeTemplate typeTemplate);

    @Select("select id,name from tb_type_template")
    List<Map<String, Object>> findTypeTemplateList();
}