package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.pojo.Cart;
import com.pinyougou.service.CartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 10000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @GetMapping("/addCart")
    @CrossOrigin(origins = "http://item.pinyougou.com")
    public boolean addCart(Long itemId, Integer num) {
        try {
            String userName = request.getRemoteUser();
            //购物车集合
            List<Cart> carts = findCart();
            // 调用服务层添加SKU商品到购物车
            carts = cartService.addItemToCart(carts, itemId, num);

            if (StringUtils.isNoneBlank(userName)) { //已经登录
                /**######## 往Redis存储购物车 #######*/
                cartService.saveCartRedis(userName, carts);
            } else { //未登录
                /**######## 往Cookie存储购物车 #######*/
                // 将购物车重新存入Cookie中
                CookieUtils.setCookie(request, response,
                        CookieUtils.CookieName.PINYOUGOU_CART,
                        JSON.toJSONString(carts),
                        3600 * 24, true);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取购物车集合
     */
    @GetMapping("/findCart")
    public List<Cart> findCart() {
        String userName = request.getRemoteUser();
        // 定义购物车集合
        List<Cart> carts = null;

        if (StringUtils.isNoneBlank(userName)) {  //已经登录
            //从Redis中获取购物车集合json数据
            carts = cartService.findCartRedis(userName);
            // 从Cookie中获取购物车集合json字符串
            String cartStr = CookieUtils.getCookieValue(request, CookieUtils.CookieName.PINYOUGOU_CART, true);
            if (StringUtils.isBlank(cartStr)) {  //cookie中没有数据
                cartStr = "[]";
            } else {  //cookie中有数据
                List<Cart> cookieCarts = JSON.parseArray(cartStr, Cart.class);
                if (cookieCarts != null && cookieCarts.size() > 0) {
                    //合并cookie和redis 中的购物车
                    carts = cartService.mergeCart(carts, cookieCarts);
                }
                //保存到购物车
                cartService.saveCartRedis(userName, carts);
                //删除cookie中的数据
                CookieUtils.deleteCookie(request,response,CookieUtils.CookieName.PINYOUGOU_CART);
            }
        } else {
            // 从Cookie中获取购物车集合json字符串
            String cartStr = CookieUtils.getCookieValue(request, CookieUtils.CookieName.PINYOUGOU_CART, true);
            if (StringUtils.isBlank(cartStr)) {
                cartStr = "[]";
            }
            carts = JSON.parseArray(cartStr, Cart.class);
        }
        return carts;
    }

    /**
     * 填写数字更改购物车数量
     */
    @GetMapping("/addCart1")
    public boolean addCart1(Long itemId, Integer num) {
        try {
            String userName = request.getRemoteUser();
            //购物车集合
            List<Cart> carts = findCart();
            // 调用服务层添加SKU商品到购物车
            carts = cartService.addItemToCart1(carts, itemId, num);

            if (StringUtils.isNoneBlank(userName)) { //已经登录
                /**######## 往Redis存储购物车 #######*/
                cartService.saveCartRedis(userName, carts);
            } else { //未登录
                /**######## 往Cookie存储购物车 #######*/
                // 将购物车重新存入Cookie中
                CookieUtils.setCookie(request, response,
                        CookieUtils.CookieName.PINYOUGOU_CART,
                        JSON.toJSONString(carts),
                        3600 * 24, true);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
