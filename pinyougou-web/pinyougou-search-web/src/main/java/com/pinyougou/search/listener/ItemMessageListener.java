package com.pinyougou.search.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.Item;
import com.pinyougou.service.GoodsService;
import com.pinyougou.service.ItemSearchService;
import com.pinyougou.solr.SolrItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.jms.listener.SessionAwareMessageListener;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemMessageListener implements SessionAwareMessageListener<ObjectMessage> {

    @Reference(timeout = 10000)
    private GoodsService goodsService;

    @Reference(timeout = 10000)
    private ItemSearchService itemSearchService;


    /** 增加上架商品索引 */
    @Override
    public void onMessage(ObjectMessage objectMessage, Session session) throws JMSException {
        System.out.println("#####增加上架商品索引ItemMessageListener##########");
        Long[] goodsIds = (Long[]) objectMessage.getObject();
        // 查找对应id的SKU
        List<Item> itemList = goodsService.findItemByGoodsId(goodsIds);
        List<SolrItem> solrItems = new ArrayList<>();
        if (itemList != null && itemList.size() > 0) {
            for (Item item : itemList) {
                SolrItem solrItem = new SolrItem();
                solrItem.setTitle(item.getTitle());
                solrItem.setSpecMap(JSON.parseObject(item.getSpec(), Map.class));
                solrItem.setUpdateTime(item.getUpdateTime());
                solrItem.setSeller(item.getSeller());
                solrItem.setPrice(item.getPrice());
                solrItem.setImage(item.getImage());
                solrItem.setGoodsId(item.getGoodsId());
                solrItem.setCategory(item.getCategory());
                solrItem.setBrand(item.getBrand());
                solrItem.setId(item.getId());
                solrItems.add(solrItem);
            }

            itemSearchService.saveOrUpdate(solrItems);
        }


    }
}
