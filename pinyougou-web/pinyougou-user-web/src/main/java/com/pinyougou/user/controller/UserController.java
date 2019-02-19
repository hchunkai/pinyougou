package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.User;
import com.pinyougou.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    UserService userService;

    /** 保存注册用户 */
    @PostMapping("/save")
    public boolean save(@RequestBody User user,String code) {
        try {
            String phone = user.getPhone();
            boolean success = userService.checkSmsCode(phone, code);
            if (success) {
            userService.save(user);
            return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 发送注册短信
     */
    @GetMapping("/sendCode")
    public Map<String, Object> sendCode(String phone) {
      try{
          if (StringUtils.isNoneBlank(phone)) {
              Map<String, Object> map = userService.sendCode(phone);
              return map;
          }
      }catch(Exception e){
      e.printStackTrace();
      }
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        return map;
    }

    /**
     * 获取登录用户名
     */
    @GetMapping("showName")
    public Map<String, String> showName() {
        Map<String, String> map = new HashMap<>();
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName", name);
        return map;
    }

}
