package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.mapper.TypeTemplateMapper;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.pojo.SpecificationOption;
import com.pinyougou.pojo.TypeTemplate;
import com.pinyougou.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service(interfaceName = "com.pinyougou.service.TypeTemplateService")
public class TypeTemplateServiceImpl implements TypeTemplateService {


    @Autowired
    private TypeTemplateMapper typeTemplateMapper;
    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 添加方法
     *
     * @param typeTemplate
     */
    @Override
    public void save(TypeTemplate typeTemplate) {
        typeTemplateMapper.insertSelective(typeTemplate);
    }

    /**
     * 修改方法
     *
     * @param typeTemplate
     */
    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateMapper.updateByPrimaryKeySelective(typeTemplate);

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
        Example example = new Example(TypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id",Arrays.asList(ids));
        typeTemplateMapper.deleteByExample(example);

        //删除Redis
        redisTemplate.boundHashOps("itemCat").delete();
    }

    /**
     * 根据主键id查询
     *
     * @param id
     */
    @Override
    public TypeTemplate findOne(Serializable id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询全部
     */
    @Override
    public List<TypeTemplate> findAll() {
        return typeTemplateMapper.selectAll();
    }

    /**
     * 多条件分页查询
     *
     * @param typeTemplate
     * @param page
     * @param rows
     */
    @Override
    public PageResult findByPage(TypeTemplate typeTemplate, int page, int rows) {

        //将数据存进Redis缓存
        saveToRedis();

        PageHelper.startPage(page, rows);
        List<TypeTemplate> page1 = typeTemplateMapper.findPage(typeTemplate);
        PageInfo<TypeTemplate> pageInfo = new PageInfo<>(page1);
        return new PageResult(pageInfo.getTotal(),pageInfo.getList());
    }


    @Override
    public List<Map<String, Object>> findTypeTemplateList() {
        return typeTemplateMapper.findTypeTemplateList();
    }


    //新增商品之规格
    @Override
    public List<Map> findSpec(Long id) {
        TypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);

        List<Map> specList = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
        for (Map map : specList) {
            SpecificationOption so = new SpecificationOption();
            //根据specId查询规格选项列表
            so.setSpecId(Long.valueOf(map.get("id").toString()));
            List<SpecificationOption> optionNames = specificationOptionMapper.select(so);
            map.put("options", optionNames);
        }
        return specList;
    }

    //缓存Redis方法
    private void saveToRedis() {
        //获取模板数据
        List<TypeTemplate> typeTemplateList = findAll();
        for (TypeTemplate typeTemplate : typeTemplateList) {
            // 获得品牌列表
            List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
            redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), brandList);

            List<Map> specList = findSpec(typeTemplate.getId());
            redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
        }

    }

}
