package com.pinyougou.service;

import com.pinyougou.pojo.Address;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.PayLog;

import java.util.List;
import java.io.Serializable;
/**
 * OrderService 服务接口
 * @date 2019-01-09 15:50:19
 * @version 1.0
 */
public interface OrderService {

	/** 添加方法 */
	void save(Order order);

	/** 修改方法 */
	void update(Order order);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	Order findOne(Serializable id);

	/** 查询全部 */
	List<Order> findAll();

	/** 多条件分页查询 */
	List<Order> findByPage(Order order, int page, int rows);

	/** 根据用户查询支付日志 */
	PayLog findPayLogFromRedis(String userId);


	/**
	 * 修改订单状态
	 * @param outTradeNo 订单交易号
	 * @param transactionId 微信交易流水号
	 */
	void updateOrderStatus(String outTradeNo, String transactionId);

}