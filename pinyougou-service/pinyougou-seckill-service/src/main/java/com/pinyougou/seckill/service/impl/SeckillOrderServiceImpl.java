package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.mapper.SeckillOrderMapper;
import com.pinyougou.pojo.SeckillGoods;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.SeckillOrderService")
@Transactional
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private IdWorker idWorker;
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加方法
     *
     * @param seckillOrder
     */
    @Override
    public void save(SeckillOrder seckillOrder) {

    }

    /**
     * 修改方法
     *
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder) {

    }

    /**
     * 根据主键id删除
     *
     * @param id
     */
    @Override
    public void delete(Serializable id) {

    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Override
    public void deleteAll(Serializable[] ids) {

    }

    /**
     * 根据主键id查询
     *
     * @param id
     */
    @Override
    public SeckillOrder findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<SeckillOrder> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *
     * @param seckillOrder
     * @param page
     * @param rows
     */
    @Override
    public List<SeckillOrder> findByPage(SeckillOrder seckillOrder, int page, int rows) {
        return null;
    }

    /**
     * 根据用户名查询秒杀订单
     *
     * @param userId 用户名
     */
    @Override
    public SeckillOrder findOrderFromRedis(String userId) {
        try {
            // 从Redis中查询用户秒杀订单
            return (SeckillOrder)redisTemplate.boundHashOps("seckillOrderList").get(userId);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }



    /**
     * 提交订单到Redis
     *
     * @param id     秒杀商品id
     * @param userId 用户id
     */
    @Override
    public void submitOrderToRedis(Long id, String userId) {
        try {
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("seckillGoodsList").get(id);
            if (seckillGoods != null && seckillGoods.getStockCount() > 0) {
                // 减库存(redis)
                seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
                if (seckillGoods.getStockCount() == 0) {//商品被秒杀完了
                    seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                    // 删除redis中对应的商品
                    redisTemplate.boundHashOps("seckillGoodsList").delete(id);
                } else { //如果还有库存
                    //更新redis
                    redisTemplate.boundHashOps("seckillGoodsList").put(id, seckillGoods);
                }
                //创建秒杀订单对象
                SeckillOrder seckillOrder = new SeckillOrder();
                // 设置订单id
                seckillOrder.setId(idWorker.nextId());
                // 设置秒杀商品id
                seckillOrder.setSeckillId(id);
                // 设置秒杀价格
                seckillOrder.setMoney(seckillGoods.getCostPrice());
                // 设置用户id
                seckillOrder.setUserId(userId);
                // 设置商家id
                seckillOrder.setSellerId(seckillGoods.getSellerId());
                // 设置创建时间
                seckillOrder.setCreateTime(new Date());
                // 设置状态码(未付款)
                seckillOrder.setStatus("0");
                // 保存订单到Redis
                redisTemplate.boundHashOps("seckillOrderList").put(userId, seckillOrder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 从Redis中删除超时未支付订单
     *
     * @param seckillOrder
     */
    @Override
    public void deleteOrderFromRedis(SeckillOrder seckillOrder) {
       try{
           // 删除超时的订单
           redisTemplate.boundHashOps("seckillOrderList").delete(seckillOrder.getUserId());
           //更新库存
           SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("seckillGoodsList").
                   get(seckillOrder.getSeckillId());
           if (seckillGoods != null) { //还有库存
               //增加库存
               seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
           } else {  //被秒杀完了
               //从数据库中获取
               seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
               seckillGoods.setStockCount(1);
           }
           //存进Redis
           redisTemplate.boundHashOps("seckillGoodsList").put(seckillOrder.getSeckillId(),seckillGoods);
       }catch(Exception e){
           throw new RuntimeException(e);
       }
    }

    /**
     * 查询超时未支付订单(
     */
    @Override
    public List<SeckillOrder> findOrderByTimeout() {

       try{
           // 定义List集合封装超时5分钟未支付的订单
           List<SeckillOrder> seckillOrders = new ArrayList<>();

           //查询出所有未支付的订单
           List<SeckillOrder> seckillOrderList = redisTemplate.boundHashOps("seckillOrderList").values();
           // 判断是否存在未支付的订单
           if (seckillOrderList != null && seckillOrderList.size() > 0) {
               for (SeckillOrder seckillOrder : seckillOrderList) {
                   //得到五分钟钱的时间毫秒值
                   long time = System.currentTimeMillis() - (5 * 60 * 1000);
                   //小于time的都是超过五分钟没有付款的
                   if (seckillOrder.getCreateTime().getTime() < time) {
                       //存进集合
                       seckillOrders.add(seckillOrder);
                   }
               }
           }
           return seckillOrders;
       }catch(Exception e){
           throw new RuntimeException(e);
       }

    }

    /**
     * 支付成功保存订单
     * @param userId 用户名
     * @param transactionId 微信交易流水号
     */
    public void saveOrder(String userId, String transactionId){
        try{
            /** 根据用户ID从redis中查询秒杀订单 */
            SeckillOrder seckillOrder = (SeckillOrder)redisTemplate
                    .boundHashOps("seckillOrderList").get(userId);
            /** 判断秒杀订单 */
            if(seckillOrder != null){
                /** 微信交易流水号 */
                seckillOrder.setTransactionId(transactionId);
                /** 支付时间 */
                seckillOrder.setPayTime(new Date());
                /** 状态码(已付款) */
                seckillOrder.setStatus("1");
                /** 保存到数据库 */
                seckillOrderMapper.insertSelective(seckillOrder);
                /** 删除Redis中的订单 */
                redisTemplate.boundHashOps("seckillOrderList").delete(userId);
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
