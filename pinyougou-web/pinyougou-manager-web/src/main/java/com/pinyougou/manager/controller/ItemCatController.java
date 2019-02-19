package com.pinyougou.manager.controller;

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

    @PostMapping("/save")
    public boolean save(@RequestBody ItemCat itemCat) {
        try {
            itemCatService.save(itemCat);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @PostMapping("/update")
    public boolean update(@RequestBody ItemCat itemCat) {
        try {
            itemCatService.update(itemCat);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/delete")
    public boolean delete(Long[] ids) {
        try {
            itemCatService.deleByid(ids);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/findCatItemList")
    public List<ItemCat> findCatItemList() {
        List<ItemCat> all = itemCatService.findAll();
        return all;
    }
}
