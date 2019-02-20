package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: M
 * @Date: 2019/2/20 10:41
 * @Version 1.0
 */
@RestController
@RequestMapping("/password")
public class PasswordController {
    @Reference(timeout = 10000)
    private SellerService sellerService;

    // 修改密码
    @PostMapping("/updatePassword")
    public Map<String,String> updatePassword(@RequestBody Map<String,String> pass){
        try {
            Map<String,String> data = new HashMap<>();
            //得到前端数据
            String oldPassword = pass.get("oldPassword");
            String newPassword = pass.get("newPassword1");
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();


            //用户id查询到旧密码
            Seller seller = sellerService.findOne(sellerId);
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (StringUtils.isNoneBlank(newPassword)){
                if (passwordEncoder.matches(oldPassword, seller.getPassword())){
                seller.setPassword(passwordEncoder.encode(newPassword));
                sellerService.update(seller);
                data.put("falg", "1");
                    } else {
                    data.put("falg","0");
                }
            }else{
                data.put("falg", "2");
            }

            return data;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
