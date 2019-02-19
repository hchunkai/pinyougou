package com.pinyougou.shop.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Goods;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.service.GoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference(timeout = 10000)
    private GoodsService goodsService;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination solrQueue;
    @Autowired
    private Destination solrDeleteQueue;
    @Autowired
    private Destination createStatciItemTopic;
    @Autowired
    private Destination deleteStaticItemTopic;


    //添加商品
    @PostMapping("/save")
    public boolean save(@RequestBody Goods goods) {
        try {
            String sellerName = SecurityContextHolder.getContext().getAuthentication().getName();
            goods.setSellerId(sellerName);
            goodsService.save(goods);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    //多条件分页查询商品
    @GetMapping("/findByPage")
    public PageResult findByPage(Goods goods, Integer page, Integer rows) {

        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(sellerId);
        try {
            if (goods != null && StringUtils.isNoneBlank(goods.getGoodsName())) {
                String s = new String(goods.getGoodsName().getBytes("ISO8859-1"), "UTF-8");
                goods.setGoodsName(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return goodsService.findByPage(goods, page, rows);
    }


    //删除
    @GetMapping("/delete")
    public boolean delete(Long[] ids) {
        try {
            if (ids.length > 0) {
                goodsService.deleteAll(ids);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //修改上下架状态(修改可销售状态)
    @GetMapping("/updateStatus")
    public boolean updateStatus(String isMarketable, Long[] ids) {
        try {
            goodsService.updateMarketStatus(isMarketable, ids);
            //如果是上架状态,生成商品索引
            if ("1".equals(isMarketable)) {
                /** 发送消息到消息服务器(创建上架商品的索引到索引库) */
                jmsTemplate.send(solrQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        // ids: tb_goods 表中的多个id
                        return session.createObjectMessage(ids);
                    }
                });
                /** 发送消息到消息服务器(生成上架商品的静态html页面) */
                for (Long goodsId : ids) {
                    jmsTemplate.send(createStatciItemTopic, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(goodsId.toString());
                        }

                    });
                }
            } else { //下架
                /** 发送消息到消息服务器(删除下架商品的索引) */
                jmsTemplate.send(solrDeleteQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createObjectMessage(ids);
                    }
                });

                /** 发送消息到消息服务器(删除下架商品的静态html页面) */
                jmsTemplate.send(deleteStaticItemTopic, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createObjectMessage(ids);
                    }
                });


            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
