package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SellerMapper;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.SellerService")
public class SellerServiceImpl implements SellerService {

    @Autowired
    SellerMapper sellerMapper;

    /**
     * 添加方法
     *
     * @param seller
     */
    @Override
    public void save(Seller seller) {
        sellerMapper.insertSelective(seller);
    }

    /**
     * 修改方法
     *
     * @param seller
     */
    @Override
    public void update(Seller seller) {
        sellerMapper.updateByPrimaryKeySelective(seller);
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
    public Seller findOne(Serializable id) {

        return sellerMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询全部
     */
    @Override
    public List<Seller> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *
     * @param seller
     * @param page
     * @param rows
     */
    @Override
    public PageResult findByPage(Seller seller, int page, int rows) {
        PageInfo<Seller> pageInfo = PageHelper.startPage(page, rows).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                sellerMapper.findByPage(seller);

            }
        });
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());


    }
}
