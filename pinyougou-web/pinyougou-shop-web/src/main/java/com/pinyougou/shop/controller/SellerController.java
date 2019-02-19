package com.pinyougou.shop.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/seller")
public class SellerController {

    @Reference(timeout = 10000)
    SellerService sellerService;

    @PostMapping("/save")
    public boolean save(@RequestBody Seller seller) {
        try{
            seller.setStatus("0");
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String password = passwordEncoder.encode(seller.getPassword());
            seller.setPassword(password);
            sellerService.save(seller);
            return true;
        }catch(Exception e){
        e.printStackTrace();
        }
            return false;
    }


    @GetMapping("/showLoginName")
    public Map<String, String> getLoginName() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, String> map = new HashMap<>();
        map.put("loginName", name);
        return map;
    }

}
