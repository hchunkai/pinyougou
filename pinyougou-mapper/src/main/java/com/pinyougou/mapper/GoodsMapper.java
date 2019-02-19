package com.pinyougou.mapper;

import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.Goods;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * GoodsMapper 数据访问接口
 * @date 2019-01-09 15:45:18
 * @version 1.0
 */
public interface GoodsMapper extends Mapper<Goods>{


    List<Map<String,Object>> findPage(Goods goods);

    void updateDeleteStatus(@Param("ids") Serializable[] ids,@Param("isDelete") String s);

    void updateStatus(String auditStatus,@Param("ids") Long[] ids);

    void updateMarketStatus(String isMarketable,@Param("ids") Long[] ids);
}