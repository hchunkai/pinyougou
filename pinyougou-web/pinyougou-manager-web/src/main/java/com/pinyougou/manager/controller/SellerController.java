package com.pinyougou.manager.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seller")
public class SellerController {


    @Reference
    SellerService sellerService;

    @RequestMapping("/findByPage")
    public PageResult findByPage(Seller seller, Integer page, Integer rows) {
        try {
            if (seller != null && StringUtils.isNoneBlank(seller.getName())) {
                seller.setName(new String(seller.getName()
                        .getBytes("iso8859-1"), "UTF-8"));
            }
            if (seller != null && StringUtils.isNoneBlank(seller.getNickName())) {
                seller.setNickName(new String(seller.getNickName()
                        .getBytes("ISO8859-1"), "UTF-8"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sellerService.findByPage(seller, page, rows);
    }

    @PostMapping("/update")
    public boolean update(String sellerId, String status) {
        try{
            Seller seller = new Seller();
            seller.setSellerId(sellerId);
            seller.setStatus(status);
            sellerService.update(seller);
            return true;
        }catch(Exception e){
        e.printStackTrace();
            return false;
        }

    }

}
