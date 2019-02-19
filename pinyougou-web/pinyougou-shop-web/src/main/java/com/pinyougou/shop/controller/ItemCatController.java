package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.ItemCat;
import com.pinyougou.service.ItemCatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class ItemCatController {

    @Reference(timeout = 10000)
    ItemCatService itemCatService;

    @GetMapping("/findByParentId")
    public List<ItemCat> findByPage(@RequestParam(value="parentId",
            defaultValue="0")Long parentId) {
        return itemCatService.findByPage(parentId);
    }


    @GetMapping("/findCatItemList")
    public List<ItemCat> findCatItemList() {
        List<ItemCat> all = itemCatService.findAll();
        return all;
    }

}
