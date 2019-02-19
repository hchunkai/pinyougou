package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationMapper;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.pojo.Specification;
import com.pinyougou.pojo.SpecificationOption;
import com.pinyougou.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service(interfaceName ="com.pinyougou.service.SpecificationService")
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    SpecificationMapper specificationMapper;
    @Autowired
    SpecificationOptionMapper specificationOptionMapper;

    /**
     * 添加方法
     *
     * @param specification
     */
    @Override
    public void save(Specification specification) {
        specificationMapper.insertSelective(specification);
        specificationOptionMapper.save(specification);
    }

    /**
     * 修改方法
     *
     * @param specification
     */
    @Override
    public void update(Specification specification) {
        specificationMapper.updateByPrimaryKeySelective(specification);
        Long id = specification.getId();
        SpecificationOption specificationOption = new SpecificationOption();
        specificationOption.setSpecId(id);
        specificationOptionMapper.delete(specificationOption);
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
        Example example = new Example(Specification.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        specificationMapper.deleteByExample(example);
        for (Serializable id : ids) {
            SpecificationOption so = new SpecificationOption();
            so.setSpecId((Long) id);
            specificationOptionMapper.delete(so);
        }
    }

    /**
     * 根据主键id查询
     *
     * @param id
     */
    @Override
    public Specification findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<Specification> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *
     * @param specification
     * @param page
     * @param rows
     */
    @Override
    public PageResult findByPage(Specification specification, int page, int rows) {

        PageInfo<Specification> pageInfo = PageHelper.startPage(page, rows).doSelectPageInfo(() -> specificationMapper.findAll(specification));
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 查询规格选项
     *
     */
    @Override
    public List<SpecificationOption> findSo(Long id) {
        SpecificationOption specificationOption = new SpecificationOption();
        specificationOption.setSpecId(id);
        return specificationOptionMapper.select(specificationOption);
    }

    @Override
    public List<Map<String, Object>> specIDandName() {
        return specificationMapper.specIDandName();
    }
}
