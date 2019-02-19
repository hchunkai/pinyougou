package com.pinyougou.mapper;

import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.Seller;

import java.util.List;

/**
 * SellerMapper 数据访问接口
 * @date 2019-01-09 15:45:18
 * @version 1.0
 */
public interface SellerMapper extends Mapper<Seller>{


    List<Seller> findByPage(Seller seller);
}