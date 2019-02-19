package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClientUtils;
import com.pinyougou.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;


@Service(interfaceName = "com.pinyougou.service.WeixinPayService")
@Transactional
public class WeixinPayServiceImpl implements WeixinPayService {

    // 微信公众号
    @Value("${appid}")
    private String appid;
    // 商户账号
    @Value("${partner}")
    private String partner;
    // 商户密钥
    @Value("${partnerkey}")
    private String partnerkey;
    // 统一下单请求地址
    @Value("${unifiedorder}")
    private String unifiedorder;
    @Value("${orderquery}")
    private String orderquery;
    /** 关闭订单请求地址 */
    @Value("${closeorder}")
    private String closeorder;


    /**
     * 生成微信支付二维码
     *
     * @param outTradeNo 订单交易号
     * @param totalFee   金额(分)
     * @return Map集合
     */
    @Override
    public Map<String, String> genPayCode(String outTradeNo, String totalFee) {
        //创建Map封装数据
        Map<String, String> param = new HashMap<>();
        /** 公众号 */
        param.put("appid", appid);
        /** 商户号 */
        param.put("mch_id", partner);
        /** 随机字符串 */
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        /** 商品描述 */
        param.put("body", "品优购");
        /** 商户订单交易号 */
        param.put("out_trade_no", outTradeNo);
        /** 总金额（分） */
        param.put("total_fee",totalFee);
        /** IP地址 */
        param.put("spbill_create_ip", "127.0.0.1");
        /** 回调地址(随意写) */
        param.put("notify_url", "http://test.itcast.cn");
        /** 交易类型 */
        param.put("trade_type", "NATIVE");
        try{
                /** 根据商户密钥签名生成XML格式请求参数 */
                String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
                System.out.println("请求参数：" + xmlParam);
                /** 创建HttpClientUtils对象发送请求 */
                HttpClientUtils client = new HttpClientUtils(true);
                /** 发送请求，得到响应数据 */
                String result = client.sendPost(unifiedorder, xmlParam);
                System.out.println("响应数据：" + result);
                /** 将响应数据XML格式转化成Map集合 */
                Map<String,String> resultMap = WXPayUtil.xmlToMap(result);
                /** 创建Map集合封装返回数据 */
                Map<String,String> data = new HashMap<>();
                /** 支付地址(二维码中的URL) */
                data.put("codeUrl", resultMap.get("code_url"));
                /** 总金额 */
                data.put("totalFee", totalFee);
                /** 订单交易号 */
                data.put("outTradeNo", outTradeNo);
                return data;
            }catch(Exception e){
                throw new RuntimeException(e);
        }

    }

    /**
     * 关闭超时未支付订单
     *
     * @param outTradeNo
     */
    @Override
    public Map<String, String> closePayTimeout(String outTradeNo) {
        /** 创建Map集合封装请求参数 */
        Map<String, String> params = new HashMap<>();
        /** 公众账号 */
        params.put("appid", appid);
        /** 商户账号 */
        params.put("mch_id", partner);
        /** 订单交易号 */
        params.put("out_trade_no", outTradeNo);
        /** 随机字符串 */
        params.put("nonce_str", WXPayUtil.generateNonceStr());
       try{
           String xmlParam  = WXPayUtil.generateSignedXml(params, partnerkey);
           HttpClientUtils client = new HttpClientUtils(true);
           String result = client.sendPost(closeorder, xmlParam);
           Map<String, String> map = WXPayUtil.xmlToMap(result);
           return map;
       }catch(Exception e){
           throw new RuntimeException(e);
       }

    }

    /**
     * 查询支付状态
     * @param outTradeNo
     * @return
     */
    @Override
    public Map<String, String> queryPayStatus(String outTradeNo) {
        try{
            //封装查询微信支付订单的数据
            Map<String, String> param = new HashMap<>();
            param.put("appid", appid);
            param.put("mch_id", partner);
            param.put("out_trade_no", outTradeNo);
            param.put("nonce_str", WXPayUtil.generateNonceStr());
            String signature = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClientUtils client = new HttpClientUtils(true);
            // 得到相应数据  --> xml格式
            String response = client.sendPost(orderquery, signature);
            Map<String, String> result = WXPayUtil.xmlToMap(response);
            return result;
        }catch(Exception e){
            throw new RuntimeException(e);
        }

    }
}
