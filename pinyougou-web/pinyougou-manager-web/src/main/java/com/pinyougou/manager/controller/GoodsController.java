package com.pinyougou.manager.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Goods;
import com.pinyougou.pojo.Item;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.service.GoodsService;
import com.pinyougou.service.ItemSearchService;
import com.pinyougou.solr.SolrItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference(timeout = 10000)
    private GoodsService goodsService;
    @Reference(timeout = 10000)
    private ItemSearchService itemSearchService;

    @PostMapping("/save")
    public boolean save(@RequestBody Goods goods) {
        try {
            String sellerName = SecurityContextHolder.getContext().getAuthentication().getName();
            goods.setSellerId(sellerName);
            goodsService.save(goods);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @GetMapping("/findByPage")
    public PageResult findByPage(Goods goods, Integer page, Integer rows) {
        try {
            goods.setAuditStatus("0");
            if (goods != null && StringUtils.isNoneBlank(goods.getGoodsName())) {
                String goodsName = new String(goods.getGoodsName().getBytes("ISO8859-1"), "UTF-8");
                goods.setGoodsName(goodsName);
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

    //修改审核状态
    @GetMapping("/updateStatus")
    public boolean updateStatus(String auditStatus,Long[] ids) {
        try {

            goodsService.updateStatus(auditStatus, ids);

            //到导入solr索引库中
            if ("1".equals(auditStatus)) {//如果是审核通过的
                //得到需要的SKU列表
                List<Item> itemList = goodsService.findItemListByGoodsIdandStatus(ids, auditStatus);
                //导入到solr
                itemSearchService.importList(itemList);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
