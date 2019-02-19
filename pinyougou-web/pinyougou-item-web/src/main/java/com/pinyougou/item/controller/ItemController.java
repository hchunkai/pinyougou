package com.pinyougou.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.GoodsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class ItemController {

    @Reference(timeout = 10000)
    GoodsService goodsService;

    /**
     * 根据id查询商品信息
     */
    @GetMapping("/{goodsId}")
    public String getGoods(@PathVariable("goodsId") Long goodsId , Model model) {
        //定义Map集合封装模板文件数据
        Map<String, Object> data =  goodsService.getGoods(goodsId);

        //也可以用Map.put封装模板数据
        model.addAllAttributes(data);
        return "item";
    }


}
