package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.ItemCatMapper;
import com.pinyougou.pojo.ItemCat;
import com.pinyougou.service.ItemCatService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.ItemCatService")
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private ItemCatMapper itemCatMapper;
    //导入RedisTemplate
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 添加方法
     *
     * @param itemCat
     */
    @Override
    public void save(ItemCat itemCat) {
        itemCatMapper.insertSelective(itemCat);

    }

    /**
     * 修改方法
     *
     * @param itemCat
     */
    @Override
    public void update(ItemCat itemCat) {
        itemCatMapper.updateByPrimaryKeySelective(itemCat);
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


    }

    /**
     * 根据主键id查询
     *
     * @param id
     */
    @Override
    public ItemCat findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<ItemCat> findAll() {
        return itemCatMapper.selectAll();
    }

    /**
     * 多条件分页查询
     */
    @Override
    public List<ItemCat> findByPage(Long id) {


        //每次执行查询的时候，一次性读取缓存进行Redis存储 (因为每次增删改都要执行此方法)
        List<ItemCat> ItemList = findAll();
        for (ItemCat itemCat : ItemList) {
            if (StringUtils.isNoneBlank(itemCat.getName())) {
            redisTemplate.boundHashOps("itemCat").put(itemCat.getName(), itemCat.getTypeId());
            }
        }
        System.out.println("更新Redis缓存:商品分类表");


        return itemCatMapper.findParent(id);

    }

    //循环删除
    @Override
    public void deleByid(Long[] ids) {
        try {
            List<Long> idList = new ArrayList<>();
            for (Long id : ids) {
                idList.add(id);
                //循环找出
                findleafNode(id, idList);
            }

            //删除Redis
            redisTemplate.boundHashOps("itemCat").delete();
            itemCatMapper.deleteById(idList);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    //循环找出
    private void findleafNode(Long id, List<Long> idList) {
        List<ItemCat> itemCatLists = findByPage(id);
        if (itemCatLists != null && itemCatLists.size() > 0) {
            for (ItemCat itemCatList : itemCatLists) {
                idList.add(itemCatList.getId());
                findleafNode(itemCatList.getId(), idList);
            }
        }

    }
}

