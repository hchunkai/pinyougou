package com.pinyougou.solr.util;


import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Item;
import com.pinyougou.solr.SolrItem;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtils {

    @Autowired
    ItemMapper itemMapper;
    @Autowired
    SolrTemplate solrTemplate;

    //批量导入solr索引库
    public void importItemData() {
        Item item = new Item();
        item.setStatus("1");
        List<Item> items = itemMapper.select(item);
        System.out.println("============ 商品列表 ==========");
        List<SolrItem> solrItems = new ArrayList<>();
        for (Item item1 : items) {
            SolrItem solrItem = new SolrItem();
            solrItem.setId(item1.getId());
            solrItem.setBrand(item1.getBrand());
            solrItem.setCategory(item1.getCategory());
            solrItem.setGoodsId(item1.getGoodsId());
            solrItem.setImage(item1.getImage());
            solrItem.setPrice(item1.getPrice());
            solrItem.setSeller(item1.getSeller());
            solrItem.setTitle(item1.getTitle());
            solrItem.setUpdateTime(item1.getUpdateTime());
            /** 将spec字段的json字符串转换成map */
            Map specMap = JSON.parseObject(item1.getSpec(), Map.class);
            /** 设置动态域 */
            solrItem.setSpecMap(specMap);

            solrItems.add(solrItem);
        }
        UpdateResponse updateResponse = solrTemplate.saveBeans(solrItems);
        //这里的 status 可以拿到操作执行的状态 ,0表示 成功
        if (updateResponse.getStatus() == 0) {
            solrTemplate.commit();
        } else {
            solrTemplate.rollback();
        }
        System.out.println("===批量导入Item到solr成功===");
    }


    /**
     * 批量删除方法
     */
    public void deleteAll() {
        Query query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
        System.out.println("删除全部solr成功");
    }


    /**
     * 方法入口
     * */
    public static void main(String[] args) {
        //创建spring容器
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        //获取对象
        SolrUtils solrUtils = applicationContext.getBean(SolrUtils.class);
        //调用方法
        solrUtils.importItemData();


        //删除所有方法
       // solrUtils.deleteAll();

    }

}
