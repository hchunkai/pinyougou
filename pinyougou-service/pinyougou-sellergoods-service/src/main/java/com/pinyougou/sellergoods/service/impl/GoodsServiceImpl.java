package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.Goods;
import com.pinyougou.pojo.GoodsDesc;
import com.pinyougou.pojo.Item;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

@Service(interfaceName = "com.pinyougou.service.GoodsService")
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsDescMapper goodsDescMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ItemCatMapper itemCatMapper;
    @Autowired
    private SellerMapper sellerMapper;
    @Autowired
    private BrandMapper brandMapper;


    /**
     * 添加方法
     *
     * @param goods
     */
    @Override
    public void save(Goods goods) {
        try {
            // 设置商品审核状态(未审核)
            goods.setAuditStatus("0");
            goodsMapper.insertSelective(goods);
            // 设置商品描述id
            Long id = goods.getId();
            goods.getGoodsDesc().setGoodsId(id);
            //存储goodDesc
            goodsDescMapper.insertSelective(goods.getGoodsDesc());

            //存储Item
            // 判断是否启用规格
            if ("1".equals(goods.getIsEnableSpec())) {
                List<Item> items = goods.getItems();
                for (Item item : items) {
                    StringBuilder title = new StringBuilder();
                    title.append(" " + goods.getGoodsName());
                    String specs = item.getSpec();
                    //{"网络":"联通4G","机身内存":"64G"}
                    Map<String, Object> spec = JSON.parseObject(specs);
                    Collection<Object> values = spec.values();
                    for (Object value : values) {
                        title.append(" " + value);
                    }
                    //储存SKU商品的标题
                    item.setTitle(title.toString());

                    /** 设置SKU商品其它属性 */
                    setItemInfo(item, goods);
                    itemMapper.insertSelective(item);
                }
            }else { // 没有启用规格
                    // SPU就是SKU 只需要往tb_item表插入一条数据
                //  {spec : {}, price : 0, num : 9999, status : '0', isDefault : '0' }
                /** 创建SKU具体商品对象 */
                Item item = new Item();
                /** 设置SKU商品的标题 */
                item.setTitle(goods.getGoodsName());
                /** 设置SKU商品的价格 */
                item.setPrice(goods.getPrice());
                /** 设置SKU商品库存数据 */
                item.setNum(9999);
                /** 设置SKU商品启用状态 */
                item.setStatus("1");
                /** 设置是否默认*/
                item.setIsDefault("1");
                /** 设置规格选项 */
                item.setSpec("{}");
                    setItemInfo(item, goods);
                    itemMapper.insertSelective(item);
                }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 定义方法设置Item的值
     */
    /**
     * 设置SKU商品信息
     */
    private void setItemInfo(Item item, Goods goods) {
        /** 设置SKU商品图片地址 */
        List<Map> imageList = JSON.parseArray(
                goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList != null && imageList.size() > 0) {
            /** 取第一张图片 */
            item.setImage((String) imageList.get(0).get("url"));
        }
        /** 设置SKU商品的分类(三级分类) */
        item.setCategoryid(goods.getCategory3Id());
        /** 设置SKU商品的创建时间 */
        item.setCreateTime(new Date());
        /** 设置SKU商品的修改时间 */
        item.setUpdateTime(item.getCreateTime());
        /** 设置SPU商品的编号 */
        item.setGoodsId(goods.getId());
        /** 设置商家编号 */
        item.setSellerId(goods.getSellerId());
        /** 设置分类名称 */
        item.setCategory(itemCatMapper
                .selectByPrimaryKey(goods.getCategory3Id()).getName());
        /** 设置品牌名称 */
        item.setBrand(brandMapper
                .selectByPrimaryKey(goods.getBrandId()).getName());
        /** 设置商家店铺名称 */
        item.setSeller(sellerMapper.selectByPrimaryKey(
                goods.getSellerId()).getNickName());
    }


    /**
     * 更新上下架状态
     *
     * @param isMarketable
     * @param ids
     */
    @Override
    public void updateMarketStatus(String isMarketable, Long[] ids) {

        goodsMapper.updateMarketStatus(isMarketable, ids);
    }

    /**
     * 更新审核状态
     *
     * @param auditStatus
     * @param ids
     */
    @Override
    public void updateStatus(String auditStatus, Long[] ids) {
        goodsMapper.updateStatus(auditStatus, ids);

    }

    /**
     * 修改方法
     *
     * @param goods
     */
    @Override
    public void update(Goods goods) {

    }

    /**
     * 根据主键id删除
     *
     * @param id
     */
    @Override
    public void delete(Serializable id) {

    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Override
    public void deleteAll(Serializable[] ids) {
        goodsMapper.updateDeleteStatus(ids, "1");
    }

    /**
     * 根据主键id查询
     *
     * @param id
     */
    @Override
    public Goods findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<Goods> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *
     * @param goods
     * @param page
     * @param rows
     */
    @Override
    public PageResult findByPage(Goods goods, int page, int rows) {

        PageHelper.startPage(page, rows);
        List<Map<String, Object>> page1 = goodsMapper.findPage(goods);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(page1);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }


    /**
     * 更新索引库，根据goodsid和状态查找Item
     *
     * @param goodsIds
     * @param status
     * @return
     */
    public List<Item> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
        Example example = new Example(Item.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", "1");
        criteria.andIn("goodsId", Arrays.asList(goodsIds));
        return itemMapper.selectByExample(example);

    }

    /**
     * 商品详情页  >>>>>>>>  获取商品信息
     *
     * @param goodsId
     * @return
     */
    @Override
    public Map<String, Object> getGoods(Long goodsId) {
        // 定义数据模型
        Map<String, Object> dataModel = new HashMap<>();

        //###########加载SPU信息###############
        Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
        dataModel.put("goods", goods);
        // 如果不为空,那么1,2分类都不为空
        if (goods != null && goods.getCategory3Id() != null) {
            dataModel.put("itemCat1", itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName());
            dataModel.put("itemCat2", itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName());
            dataModel.put("itemCat3", itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName());
        }
        //############查询商品描述信息##############
        GoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
        dataModel.put("goodsDesc", goodsDesc);

        //############查询SKU数据################
        Example example = new Example(Item.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", "1");
        criteria.andEqualTo("goodsId", goodsId);
        example.orderBy("isDefault").desc();
        List<Item> itemList = itemMapper.selectByExample(example);
        dataModel.put("itemList", JSON.toJSONString(itemList));

        return dataModel;
    }

    //根据goodsId查找SKU
    @Override
    public List<Item> findItemByGoodsId(Long[] goodsIds) {
        Example example = new Example(Item.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("goodsId",Arrays.asList(goodsIds));
        List<Item> itemList = itemMapper.selectByExample(example);
        return itemList;
    }
}
