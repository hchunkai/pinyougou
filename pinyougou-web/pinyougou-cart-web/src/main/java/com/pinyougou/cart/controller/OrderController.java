package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.pojo.Address;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.AddressService;
import com.pinyougou.service.OrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference(timeout = 10000)
    private AddressService addressService;
    @Reference(timeout = 10000)
    private OrderService orderService;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/findAddressByUser")
    public List<Address> findAddressByUser(HttpServletRequest request) {
        String userName = request.getRemoteUser();
        return addressService.findAddressByUser(userName);
    }


    /**
     * 创建新地址
     */
    @PostMapping("/saveNewAddress")
    public boolean saveNewAddress(@RequestBody Address address) {
        try {
            String userName = request.getRemoteUser();
            address.setUserId(userName);
            addressService.save(address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新地址
     */
    @PostMapping("/updateNewAddress")
    public boolean updateNewAddress(@RequestBody Address address) {
        try {
            addressService.update(address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除地址
     */
    @GetMapping("/delete")
    public boolean delete(Long id) {
        try {
            addressService.delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 保存提交的订单
     */
    @PostMapping("/saveOrder")
    public boolean saveOrder(@RequestBody Order order) {
        try {
            //设置购买的用户
            order.setUserId(request.getRemoteUser());
            //设置订单来源-->PC
            order.setSourceType("2");
            orderService.save(order);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 获得微信支付二维码
     */
    @GetMapping("/genPayCode")
    public Map<String, String> genPayCode() {
        //获取用户名
        String userId = request.getRemoteUser();
        // 从Redis查询支付日志
        PayLog payLog = orderService.findPayLogFromRedis(userId);
        // 调用生成微信支付二维码服务方法
        Map<String, String> map = weixinPayService.genPayCode(payLog.getOutTradeNo(), String.valueOf(payLog.getTotalFee()));
        return map;
    }

    /**
     * 查询支付状态
     *
     * @param outTradeNo
     * @return
     */
    @GetMapping("/queryPayStatus")
    public Map<String, Integer> queryPayStatus(String outTradeNo) {
        Map<String, Integer> data = new HashMap<>();
        data.put("status", 3);
        try {
            // 调用查询订单接口
            Map<String, String> resMap = weixinPayService.queryPayStatus(outTradeNo);
            if (resMap != null && resMap.size() > 0) {
                // 判断是否支付成功
                if ("SUCCESS".equals(resMap.get("trade_state"))) {
                    data.put("status", 1);
                    //更新状态
                    orderService.updateOrderStatus(outTradeNo, resMap.get("transaction_id"));
                }
                if ("NOTPAY".equals(resMap.get("trade_state"))) {
                    data.put("status", 2);
                }
                if ("PAYERROR".equals(resMap.get("trade_state"))) {//支付失败

                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }

}
