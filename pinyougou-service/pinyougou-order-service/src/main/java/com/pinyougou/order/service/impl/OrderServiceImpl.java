package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.pojo.Cart;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service(interfaceName = "com.pinyougou.service.OrderService")
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private PayLogMapper payLogMapper;

    /**
     * 保存提交的订单
     *
     * @param order
     */
    @Override
    public void save(Order order) {
        //默认未付款
        order.setStatus("1");
        //从redis中 取出提交的订单数据
        List<Cart> carts = (List<Cart>) redisTemplate.boundValueOps("cart_" + order.getUserId()).get();
        //订单集合
        List<Order> orderList = new ArrayList<>();
        //订单明细集合
        List<OrderItem> orderItemList = new ArrayList<>();

        //-------------------paylog-----------------------------s//
        //  支付日志中的存放订单编号列表
        List<String> orderIdList = new ArrayList<>();
        //定义多个订单支付的总金额（元）
        double totalMoney = 0;
        PayLog payLog = new PayLog();
        //-------------------paylog-----------------------------e//


        //迭代购物车
        for (Cart cart : carts) {
            //生成orderId
            long orderId = idWorker.nextId();
            //每一个商家一个订单
            Order order1 = new Order();
            order1.setOrderId(orderId);
            // 设置支付类型
            order1.setPaymentType(order.getPaymentType());
            // 设置支付状态码为“未支付”
            order1.setStatus("1");
            // 设置订单创建时间
            order1.setCreateTime(new Date());
            // 设置订单修改时间
            order1.setUpdateTime(order1.getCreateTime());
            // 设置用户名
            order1.setUserId(order.getUserId());
            // 设置收件人地址
            order1.setReceiverAreaName(order.getReceiverAreaName());
            // 设置收件人手机号码
            order1.setReceiverMobile(order.getReceiverMobile());
            // 设置收件人
            order1.setReceiver(order.getReceiver());
            // 设置订单来源
            order1.setSourceType(order.getSourceType());
            // 设置商家id
            order1.setSellerId(cart.getSellerId());
            // 定义该订单总金额
            double money = 0;
            List<OrderItem> orderItems = cart.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                // 设置主键id
                orderItem.setId(idWorker.nextId());
                // 设置关联的订单id
                orderItem.setOrderId(orderId);
                // 累计总金额
                money += orderItem.getTotalFee().doubleValue();
                // 保存数据到订单明细集合
                /* orderItemList.add(orderItem);*/
                orderItemMapper.insertSelective(orderItem);
            }
            // 设置支付总金额
            order1.setPayment(new BigDecimal(money));
            // 保存数据到订单明合
            /* orderList.add(order1);*/
            orderMapper.insertSelective(order1);


            //-------------------paylog-----------------------------s//
            //集合存放订单id
            orderIdList.add(String.valueOf(orderId));
            //记录多个订单总金额
            totalMoney += money;
            //-------------------paylog-----------------------------e//

        }

        //-------------------paylog-----------------------------s//
        //判断只要不是货到付款
        if (!"2".equals(order.getPaymentType())) {
            //生成订单交易号
            payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
            //创建时间
            payLog.setCreateTime(new Date());
            //总金额(分)
            payLog.setTotalFee((long)(totalMoney * 100));
            //交易状态  默认未支付
            payLog.setTradeState("0");
            //用户id
            payLog.setUserId(order.getUserId());
            //订单编号列表
            String ids = orderIdList.toString().replace("[", "")
                    .replace("]", "").replace(" ", "");
            payLog.setOrderList(ids);
            //支付类型
            payLog.setPayType(order.getPaymentType());
            //插入数据
            payLogMapper.insertSelective(payLog);
            //存进缓存
            redisTemplate.boundValueOps("payLog_" + payLog.getUserId()).set(payLog); //用户下两个订单呢?缓存会被覆盖
        }
        //-------------------paylog-----------------------------e//


        /* //------------- 支付订单过时方案(不完善，下单会刷新所有时间)------------------
        //保存到redis，过期时间30分钟
        redisTemplate.boundHashOps(order.getUserId() + "orderList").put(new Date().toString(), orderList);
        redisTemplate.boundHashOps(order.getUserId() + "orderItemList").put(new Date().toString(), orderItemList);
        //设置过期时间 30分
        redisTemplate.expire(order.getUserId() + "orderList", 30 * 60, TimeUnit.SECONDS);
        redisTemplate.expire(order.getUserId() + "orderItemList", 30 * 60, TimeUnit.SECONDS);*/

        //删除购物车中的数据
        redisTemplate.delete("cart_" + order.getUserId());

    }

    /**
     * 修改方法
     *
     * @param order
     */
    @Override
    public void update(Order order) {

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
    public Order findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<Order> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *
     * @param order
     * @param page
     * @param rows
     */
    @Override
    public List<Order> findByPage(Order order, int page, int rows) {
        return null;
    }

    /**
     * 根据用户查询支付日志
     *
     * @param userId
     */
    @Override
    public PayLog findPayLogFromRedis(String userId) {
        try{
            PayLog payLog = (PayLog) redisTemplate.boundValueOps("payLog_" + userId).get();
            return payLog;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改订单状态
     *
     * @param outTradeNo    订单交易号
     * @param transactionId 微信交易流水号
     */
    @Override
    public void updateOrderStatus(String outTradeNo, String transactionId) {
        try{
            // 修改支付日志状态
            PayLog payLog = payLogMapper.selectByPrimaryKey(outTradeNo);
            payLog.setPayTime(new Date());
            payLog.setTransactionId(transactionId);
            payLog.setTradeState("1"); // 已支付
            payLogMapper.updateByPrimaryKeySelective(payLog);

            //修改订单状态
            String orderIdListStr = payLog.getOrderList();
            String[] orderIdList = orderIdListStr.split(",");
            for (String orderId : orderIdList) {
                Order order = new Order();
                order.setOrderId(Long.valueOf(orderId));
                order.setStatus("2"); //已付款
                order.setPaymentTime(new Date()); // 支付时间
                orderMapper.updateByPrimaryKeySelective(order);
            }

            //清除redis未支付订单缓存
            redisTemplate.delete("payLog_"+payLog.getUserId());

        }catch(Exception e){
        e.printStackTrace();
        }
    }

}
