package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.common.util.HttpClientUtils;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.User;
import com.pinyougou.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service(interfaceName = "com.pinyougou.service.UserService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${sms.url}")
    private String smsUrl;
    @Value("${sms.signName}")
    private String signName;
    @Value("${sms.templateCode}")
    private String templateCode;


    /**
     * 添加方法
     *
     * @param user
     */
    @Override
    public void save(User user) {
        //设置用户创建时间
        user.setCreated(new Date());
        user.setUpdated(user.getCreated());
        //加密密码
        user.setPassword(DigestUtils.md5Hex(user.getPassword()));
        userMapper.insertSelective(user);

    }

    /**
     * 修改方法
     *
     * @param user
     */
    @Override
    public void update(User user) {

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
    public User findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<User> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *
     * @param user
     * @param page
     * @param rows
     */
    @Override
    public List<User> findByPage(User user, int page, int rows) {
        return null;
    }

    /**
     * 发送短信
     */
    @Override
    public Map<String, Object> sendCode(String phone) {
        try {
            //1.生成随机六位数字
            String code = UUID.randomUUID().toString().replaceAll("-", "").
                    replaceAll("[a-z|A-Z]", "").substring(0, 6);
            //2.调用短信发送接口
            HttpClientUtils httpClientUtils = new HttpClientUtils(false);
            Map<String, String> param = new HashMap<>();
            param.put("phone", phone);
            param.put("signName", signName);
            param.put("templateCode", templateCode);
            param.put("templateParam", "{'number':'" + code + "'}");
            // content: {"success" : true}
            String content = httpClientUtils.sendPost(smsUrl, param);
            Map<String, Object> resMap = JSON.parseObject(content, Map.class);
            boolean success = (boolean) resMap.get("success");
            // 3. 判断短信是否发送成功
            if (success) {
                // 4. 存储验证码到Redis数据库(发送成功) 指定key的有效时间 90秒
                redisTemplate.boundValueOps(phone).set(code, 90, TimeUnit.SECONDS);
            }
            // 5. 返回成功还失败
            Map<String, Object> map = new HashMap<>();
            map.put("success", success);
            map.put("code", code);
            return map;

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * 检查短信验证码是否正确
     *
     * @param phone
     * @param code
     */
    @Override
    public boolean checkSmsCode(String phone, String code) {
        String oldCode = (String) redisTemplate.boundValueOps(phone).get();
        return code.equals(oldCode);

    }

    /**
     * 修改密码
     *
     * @param user
     */
    @Override
    public void updatePassword(User user) {
        try {
            Example example = new Example(User.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("username", user.getUsername());
            userMapper.updateByExampleSelective(user, example);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
