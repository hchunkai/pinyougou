package com.pinyougou.service;

import com.pinyougou.pojo.Cart;

import java.util.List;

public interface CartService {


    /** 添加购物车*/
    List<Cart> addItemToCart(List<Cart> carts, Long itemId, Integer num);

    /**
     * 填写数字更改购物车数量
     */
    List<Cart> addItemToCart1(List<Cart> carts, Long itemId, Integer num);

    /**
     * 从Redis中查询购物车
     * @param userName 用户名
     * @return 购物车
     */
    List<Cart> findCartRedis(String userName);

    /**
     * 将购物车保存到Redis
     * @param userName 用户名
     * @param carts 购物车
     */
    void saveCartRedis(String userName, List<Cart> carts);
    /**
     * 合并购物车
     * @param cookieCarts Cookie购物车
     * @param redisCarts Redis购物车
     * @return 合并后的购物车
     */
    List<Cart> mergeCart(List<Cart> redisCarts, List<Cart> cookieCarts);
}
