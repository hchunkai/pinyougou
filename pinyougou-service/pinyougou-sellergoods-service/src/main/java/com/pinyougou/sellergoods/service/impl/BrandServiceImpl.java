package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.BrandMapper;
import com.pinyougou.pojo.Brand;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BrandServiceImpl 服务接口实现类
 *
 * @version 1.0
 * @date 2019-01-09 16:01:51
 */
@Service(interfaceName = "com.pinyougou.service.BrandService")
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    /**
     * 添加方法
     */
    public void save(Brand brand) {
        try {
            brandMapper.insertSelective(brand);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 修改方法
     */
    public void update(Brand brand) {
        try {
            brandMapper.updateByPrimaryKeySelective(brand);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 根据主键id删除
     */
    public void delete(Serializable id) {

    }

    /**
     * 批量删除
     */
    public void deleteAll(Serializable[] ids) {


        try {
            for (Serializable id : ids) {
                brandMapper.deleteByPrimaryKey(id);
            }
		/*	// 创建示范对象
			Example example = new Example(Brand.class);
			// 创建条件对象
			Example.Criteria criteria = example.createCriteria();
			// 创建In条件
			criteria.andIn("id", Arrays.asList(ids));
			// 根据示范对象删除
			brandMapper.deleteByExample(example);*/
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 根据主键id查询
     */
    public Brand findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    public List<Brand> findAll() {
        return brandMapper.selectAll();

    }

    /**
     * 多条件分页查询
     */
    public PageResult findByPage(Brand brand, int page, int rows) {

        PageInfo<Brand> pageInfo = PageHelper.startPage(page, rows).doSelectPageInfo(() -> brandMapper.findAll(brand)
        );
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());


    }

    @Override
    public List<Map<String, Object>> brandIDandName() {
        return brandMapper.brandIDandName();
    }

}