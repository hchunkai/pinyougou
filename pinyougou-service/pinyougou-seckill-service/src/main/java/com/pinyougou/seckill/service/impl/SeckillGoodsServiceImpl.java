package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.pojo.SeckillGoods;
import com.pinyougou.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.SeckillGoodsService")
@Transactional
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 添加方法
     *
     * @param seckillGoods
     */
    @Override
    public void save(SeckillGoods seckillGoods) {

    }

    /**
     * 修改方法
     *
     * @param seckillGoods
     */
    @Override
    public void update(SeckillGoods seckillGoods) {

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
    public SeckillGoods findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<SeckillGoods> findAll() {
        // 定义秒杀商品数据
        List<SeckillGoods> seckillGoodsList = null;
        try{
            // 从Redis中获取秒杀商品数据
            seckillGoodsList = redisTemplate.boundHashOps("seckillGoodsList").values();
            if (seckillGoodsList != null && seckillGoodsList.size() > 0){
                System.out.println("Redis缓存中数据：" + seckillGoodsList);
                return seckillGoodsList;
            }
        }catch(Exception e){
        e.printStackTrace();
        }
        try{
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("status", "1");
            criteria.andGreaterThan("stockCount", 0);
            //秒杀开始时间小于现在的
            criteria.andLessThanOrEqualTo("startTime", new Date());
            //秒杀结束时间大于现在的
            criteria.andGreaterThanOrEqualTo("endTime", new Date());
             seckillGoodsList = seckillGoodsMapper.selectByExample(example);
            //存进Redis
            for (SeckillGoods seckillGood : seckillGoodsList) {
                redisTemplate.boundHashOps("seckillGoodsList").put(seckillGood.getId(), seckillGood);
            }
        }catch(Exception e){
        e.printStackTrace();
        }
        return seckillGoodsList;
    }

    /**
     * 多条件分页查询
     *
     * @param seckillGoods
     * @param page
     * @param rows
     */
    @Override
    public List<SeckillGoods> findByPage(SeckillGoods seckillGoods, int page, int rows) {
        return null;
    }

    /**
     * 根据秒杀商品id查询商品
     *
     * @param id
     */
    @Override
    public SeckillGoods findOneFromRedis(Long id) {
        try{
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("seckillGoodsList").get(id);
            return seckillGoods;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
