package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Cart;
import com.pinyougou.pojo.Item;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.CartService")
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 合并购物车
     *
     * @param redisCarts  Redis购物车
     * @param cookieCarts Cookie购物车
     * @return 合并后的购物车
     */
    @Override
    public List<Cart> mergeCart(List<Cart> redisCarts, List<Cart> cookieCarts) {
        for (Cart cookieCart : cookieCarts) {
            List<OrderItem> orderItems = cookieCart.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                redisCarts = addItemToCart(redisCarts, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return redisCarts;
    }

    /**
     * 从Redis中查询购物车
     *
     * @param userName 用户名
     * @return 购物车
     */
    @Override
    public List<Cart> findCartRedis(String userName) {
        List<Cart> carts = (List<Cart>) redisTemplate.boundValueOps("cart_" + userName).get();
        if (carts == null) {
            carts = new ArrayList<>(0);
        }
        return carts;
    }

    /**
     * 将购物车保存到Redis
     *
     * @param userName
     * @param carts    购物车
     */
    @Override
    public void saveCartRedis(String userName, List<Cart> carts) {

        redisTemplate.boundValueOps("cart_" + userName).set(carts);
    }

    /**
     * 添加SKU商品到购物车
     *
     * @param carts  购物车(一个Cart对应一个商家)
     * @param itemId SKU商品id
     * @param num    购买数据
     * @return 修改后的购物车
     */
    @Override
    public List<Cart> addItemToCart(List<Cart> carts, Long itemId, Integer num) {
        try {
            //根据SKUid查找出对应信息
            Item item = itemMapper.selectByPrimaryKey(itemId);
            //获得商家的ID
            String sellerId = item.getSellerId();
            //判断购物车里面是否有此商家
            Cart cart = searchCartBySellerId(carts, sellerId);
            if (cart == null) {  //如果没有此商家
                //创建一个新的商家购物车对象
                cart = new Cart();
                cart.setSellerId(sellerId);
                cart.setSellerName(item.getSeller());
                // 创建订单明细(购物中一个商品)
                OrderItem orderItem = createOrderItem(item, num);
                List<OrderItem> orderItems = new ArrayList<>();
                orderItems.add(orderItem);
                cart.setOrderItems(orderItems);
                carts.add(cart);
            } else {  //如果有此商家
                //判断是否有此商品
                OrderItem orderItem = searchOrderItemByItemId(cart.getOrderItems(), itemId);
                if (orderItem == null) { //没有此商品
                    OrderItem orderItem1 = createOrderItem(item, num);
                    cart.getOrderItems().add(orderItem1);
                } else {  //有此商品
                    orderItem.setNum(orderItem.getNum() + num);
                    orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));
                    //如果订单数小于1，则此商品删除
                    if (orderItem.getNum() < 1) {
                        // 删除购物车中的订单明细(商品)
                        cart.getOrderItems().remove(orderItem);
                    }
                    // 如果cart的orderItems订单明细为0，则删除cart
                    if (cart.getOrderItems().size() == 0) {
                        carts.remove(cart);
                    }
                }

            }
            return carts;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 判断购物车中是否有此商家
     */
    private Cart searchCartBySellerId(List<Cart> carts, String sellerId) {
        for (Cart cart : carts) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 创建购物车订单明细(购物车中的一个商品)
     */
    private OrderItem createOrderItem(Item item, Integer num) {
        // 创建订单明细
        OrderItem orderItem = new OrderItem();
        orderItem.setSellerId(item.getSellerId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setTitle(item.getTitle());
        // 小计
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;
    }

    /**
     * 判断商家购物车中是否有此商品
     */
    private OrderItem searchOrderItemByItemId(List<OrderItem> orderItems, Long itemId) {
        for (OrderItem orderItem : orderItems) {
            if (itemId.equals(orderItem.getItemId())) {
                return orderItem;
            }
        }
        return null;
    }


    /**
     * 填写数字更改购物车数量
     */
    @Override
    public List<Cart> addItemToCart1(List<Cart> carts, Long itemId, Integer num) {
        //根据SKUid查找出对应信息
        Item item = itemMapper.selectByPrimaryKey(itemId);
        //获得商家的ID
        String sellerId = item.getSellerId();
        Cart cart = searchCartBySellerId(carts, sellerId);
        //得到商品
        OrderItem orderItem = searchOrderItemByItemId(cart.getOrderItems(), itemId);
            orderItem.setNum(num);
            orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));
            //如果订单数小于1，则此商品删除
            if (orderItem.getNum() < 1) {
                // 删除购物车中的订单明细(商品)
                cart.getOrderItems().remove(orderItem);
            }
            // 如果cart的orderItems订单明细为0，则删除cart
            if (cart.getOrderItems().size() == 0) {
                carts.remove(cart);
            }
        return carts;
    }



}
