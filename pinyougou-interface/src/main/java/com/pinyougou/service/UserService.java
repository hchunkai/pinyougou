package com.pinyougou.service;

import com.pinyougou.pojo.User;
import java.util.List;
import java.io.Serializable;
import java.util.Map;

/**
 * UserService 服务接口
 * @date 2019-01-09 15:50:19
 * @version 1.0
 */
public interface UserService {

	/** 添加方法 */
	void save(User user);

	/** 修改方法 */
	void update(User user);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	User findOne(Serializable id);

	/** 查询全部 */
	List<User> findAll();

	/** 多条件分页查询 */
	List<User> findByPage(User user, int page, int rows);

	/** 发送短信 */
	Map<String, Object> sendCode(String phone);

	/** 检查短信验证码是否正确 */
	boolean checkSmsCode(String phone, String code);

	/** 修改密码*/
	void updatePassword(User user);
}