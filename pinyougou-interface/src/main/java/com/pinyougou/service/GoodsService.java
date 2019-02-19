package com.pinyougou.service;

import com.pinyougou.pojo.Goods;
import com.pinyougou.pojo.Item;
import com.pinyougou.pojo.PageResult;

import java.util.List;
import java.io.Serializable;
import java.util.Map;

/**
 * GoodsService 服务接口
 * @date 2019-01-09 15:50:19
 * @version 1.0
 */
public interface GoodsService {
	/** 更新索引库，根据goodsid和状态查找Item */
	public List<Item> findItemListByGoodsIdandStatus(Long[] goodsIds, String status );

	/** 添加方法 */
	void save(Goods goods);

	/** 修改方法 */
	void update(Goods goods);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	Goods findOne(Serializable id);

	/** 查询全部 */
	List<Goods> findAll();

	/** 多条件分页查询 */
	PageResult  findByPage(Goods goods, int page, int rows);

	void updateStatus(String auditStatus, Long[] ids);

	void updateMarketStatus(String isMarketable, Long[] ids);

	/**
	 * 获取商品信息
	 * @param goodsId
	 * @return
	 */
    Map<String, Object> getGoods(Long goodsId);

	List<Item> findItemByGoodsId(Long[] goodsId);
}