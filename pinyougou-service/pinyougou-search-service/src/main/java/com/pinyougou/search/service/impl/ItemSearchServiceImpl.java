package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.Item;
import com.pinyougou.service.ItemSearchService;
import com.pinyougou.solr.SolrItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(interfaceName = "com.pinyougou.service.ItemSearchService")
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 搜索方法
     */
    @Override
    public Map<String, Object> search(Map<String, Object> params) {

        Map<String, Object> data = new HashMap<>();
        //1.查询列表,条件过滤,分页查询
        data.putAll(searchList(params));
        //2.查询分类
        List categoryList = searchCategoryList(params);  // [手机,电视,连衣裙...]
        data.put("categoryList", categoryList);
        //3.根据分类Id查询品牌和规格列表
        if (categoryList.size() > 0) {
            Map brandAndSpecList = searchBrandAndSpecList((String) categoryList.get(0));
            data.putAll(brandAndSpecList); //默认查找第一个
        }
        return data;
    }



    /**
     * 根据关键字搜索列表
     *
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap) {
        //封装数据返回
        Map<String, Object> data = new HashMap<>();
        /**
         * 分页
         */
        //获得当前页
        Integer page = (Integer) searchMap.get("page");
        if (page == null) {
            page = 1;
        }
        //获得当前页大小
        Integer rows = (Integer) searchMap.get("rows");
        if (rows == null) {
            rows = 20;
        }

        //获得搜索关键字(如果是null是不能使用toString方法,并且没有重写,会是地址值")
        String keywords = (String) searchMap.get("keywords");
        //判断是否是空串
        if (StringUtils.isNoneBlank(keywords)) {

            //创建高亮查询对象
            HighlightQuery query = new SimpleHighlightQuery();
            //创建高亮选项对象
            HighlightOptions highlightOptions = new HighlightOptions();
            //设置高亮域
            highlightOptions.addField("title");
            //设置高亮前缀
            highlightOptions.setSimplePrefix("<font color='red'>");
            //设置高亮后缀
            highlightOptions.setSimplePostfix("</font>");
            //设置高亮选项
            query.setHighlightOptions(highlightOptions);
            //创建查询条件对象，搜索keywords域
            Criteria criteria = new Criteria("keywords");
            criteria.is(keywords);
            //添加查询对象
            query.addCriteria(criteria);
            //添加分页条件
            query.setOffset((page - 1) * rows);
            query.setRows(rows);

            //##############  1按商品分类过滤 ################
            //1.1分类过滤
            if (!"".equals((String) searchMap.get("category"))) {
                Criteria criteria1 = new Criteria("category");
                criteria1.is(searchMap.get("category"));
                query.addFilterQuery(new SimpleFilterQuery(criteria1));
            }
            //1.2品牌过滤
            if (!"".equals((String) searchMap.get("brand"))) {
                Criteria criteria1 = new Criteria("brand").is(searchMap.get("brand"));
                query.addFilterQuery(new SimpleFilterQuery(criteria1));
            }
            //1.3规格过滤
            if (searchMap.get("spec") != null) {
                Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
                for (String key : specMap.keySet()) {
                    Criteria criteria1 = new Criteria("spec_" + key).is(specMap.get(key));
                    query.addFilterQuery(new SimpleFilterQuery(criteria1));
                }
            }
            //1.4价格过滤
            if (!"".equals(searchMap.get("price"))) { //0-500 , 500-1000...
                String[] price = searchMap.get("price").toString().split("-");
                if (!"0".equals(price[0])) {
                    Criteria criteria1 = new Criteria("price").greaterThanEqual(price[0]);
                    query.addFilterQuery(new SimpleFilterQuery(criteria1));
                }
                if (!"*".equals(price[1])) {
                    Criteria criteria1 = new Criteria("price").lessThanEqual(price[1]);
                    query.addFilterQuery(new SimpleFilterQuery(criteria1));
                }
            }

            //################ 升序降序  ########################
            String sortValue  = (String) searchMap.get("sort");  // ASC  DESC
            String sortField = (String) searchMap.get("sortField");  //获得要排序的域
            if (StringUtils.isNoneBlank(sortValue) && StringUtils.isNoneBlank(sortField)) {
                Sort sort = new Sort("ASC".equals(sortValue) ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
                query.addSort(sort);
            }


            // ##############查询得到高亮分页查询对象###################

            HighlightPage<SolrItem> highlightPage = solrTemplate.queryForHighlightPage(query, SolrItem.class);
            //获得高亮选项集合
            List<HighlightEntry<SolrItem>> highlightEntryList = highlightPage.getHighlighted();
            for (HighlightEntry<SolrItem> highlightEntry : highlightEntryList) {
                //获得原实体
                SolrItem solrItem = highlightEntry.getEntity();
                //高亮集合
                List<HighlightEntry.Highlight> highlights = highlightEntry.getHighlights();
                //集合中Field的高亮内容(第一个Field)
                if (highlights.size() > 0) {
                    List<String> snipplets = highlights.get(0).getSnipplets();
                    if (highlights.size() > 0 && snipplets.size() > 0) {
                        //设置高亮结果(第一个高亮的内容)
                        solrItem.setTitle(snipplets.get(0));
                    }
                }
            }
            List<SolrItem> content = highlightPage.getContent();
            data.put("totalPage", highlightPage.getTotalPages());
            data.put("total", highlightPage.getTotalElements());
            data.put("rows", highlightPage.getContent());
        } else {
            //简单查询
            SimpleQuery simpleQuery = new SimpleQuery("*:*");
            //设置分页
            simpleQuery.setOffset((page - 1) * rows);
            simpleQuery.setRows(rows);
            //获得高分集合
            ScoredPage scoredPage = solrTemplate.queryForPage(simpleQuery, SolrItem.class);
            data.put("rows", scoredPage.getContent());
            data.put("totalPage", scoredPage.getTotalPages());
            data.put("total", scoredPage.getTotalElements());
        }
        return data;
    }


    /**
     * 查询分类内容---分组查询
     *
     * @param searchMap
     * @return
     */
    private List searchCategoryList(Map searchMap) {

        List<String> list = new ArrayList();
        String keywords = (String) searchMap.get("keywords");

        if (StringUtils.isNoneBlank(keywords)) {
            Query query = new SimpleQuery();
            Criteria criteria = new Criteria("keywords");
            criteria.is(keywords);
            //设置查询条件
            query.addCriteria(criteria);
            //创建分组选项对象
            GroupOptions groupOptions = new GroupOptions();
            //设置分组域
            groupOptions.addGroupByField("category");
            //设置分组选项
            query.setGroupOptions(groupOptions);

            //###############获得分组查询集合页################

            GroupPage<SolrItem> page = solrTemplate.queryForGroupPage(query, SolrItem.class);
            //根据列得到分组结果集
            GroupResult<SolrItem> groupResult = page.getGroupResult("category");
            //得到分组结果入口页
            Page<GroupEntry<SolrItem>> groupEntries = groupResult.getGroupEntries();
            //得到分组入口集合
            List<GroupEntry<SolrItem>> content = groupEntries.getContent();
            for (GroupEntry<SolrItem> entry : content) {
                list.add(entry.getGroupValue());  //将分组结果的名称封装到返回值中
            }
        }
        return list;
    }

    /**
     * 查询品牌和规格列表
     */
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        //获取模板ID
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId != null) {
            //根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            //返回品牌列表
            map.put("brandList", brandList);
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            //返回规格列表
            map.put("specList", specList);
        }
        return map;
    }



    /**
     * 导入solr数据
     *
     * @param list
     */
    @Override
    public void importList(List list) {
        List<Item> itemList = list;
        List<SolrItem> solrItemList = new ArrayList<>();
        for (Item item : itemList) {
            SolrItem solrItem = new SolrItem();
            solrItem.setId(item.getId());
            solrItem.setBrand(item.getBrand());
            solrItem.setCategory(item.getCategory());
            solrItem.setGoodsId(item.getGoodsId());
            solrItem.setImage(item.getImage());
            solrItem.setPrice(item.getPrice());
            solrItem.setSeller(item.getSeller());
            solrItem.setTitle(item.getTitle());
            solrItem.setUpdateTime(item.getUpdateTime());
            /** 将spec字段的json字符串转换成map */
            Map specMap = JSON.parseObject(item.getSpec(), Map.class);
            /** 设置动态域 */
            solrItem.setSpecMap(specMap);
            solrItemList.add(solrItem);
        }
        UpdateResponse updateResponse = solrTemplate.saveBeans(solrItemList);
        //这里的 status 可以拿到操作执行的状态 ,0表示 成功
        if (updateResponse.getStatus() == 0) {
            solrTemplate.commit();
        } else {
            solrTemplate.rollback();
        }
        System.out.println("===更新solr库成功===");
    }

    /**
     * 添加或修改商品索引
     *
     * @param solrItems
     */
    @Override
    public void saveOrUpdate(List<SolrItem> solrItems) {
        UpdateResponse updateResponse = solrTemplate.saveBeans(solrItems);
        if (updateResponse.getStatus() == 0) {
            solrTemplate.commit();
        } else {
            solrTemplate.rollback();
        }
    }

    /** 删除商品索引 */
    @Override
    public void delete(List<Long> goodsIds) {
        Query query = new SimpleQuery();
        Criteria criteria1 = new Criteria("goodsId").in(goodsIds);
        query.addCriteria(criteria1);
        UpdateResponse updateResponse = solrTemplate.delete(query);
        if (updateResponse.getStatus() == 0) {
            solrTemplate.commit();
        } else {
            solrTemplate.rollback();
        }
    }
}
