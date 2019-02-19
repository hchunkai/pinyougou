package com.pinyougou.manager.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Brand;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.service.BrandService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@ResponseBody
@RequestMapping("/brand")
public class BrandController {

    @Reference(timeout = 10000)
    BrandService brandService;

    @GetMapping("findAll")
    public List<Brand> findAll() {
        return brandService.findAll();
    }

    @PostMapping("/save")
    public boolean save(@RequestBody Brand brand) {
        try {
            brandService.save(brand);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @PostMapping("/update")
    public boolean update(@RequestBody Brand brand) {
        try {
            brandService.update(brand);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/delete")
    public boolean delete(Long[] ids) {
        try {
            brandService.deleteAll(ids);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @PostMapping("/search")
    public List<Brand> search(@RequestBody Brand brand) {

        return null;
    }

    //    分页查询
    @GetMapping("/findByPage")
    public PageResult findByPage(Brand brand, Integer page, Integer rows) {
        if (brand.getName() != null && brand.getName() != "") {
            try {
                brand.setName(new String(brand
                        .getName().getBytes("ISO8859-1"), "UTF-8"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return brandService.findByPage(brand, page, rows);
    }

    @GetMapping("/brandIDandName")
    public List<Map<String, Object>> brandIDandName() {
        List<Map<String, Object>> maps = brandService.brandIDandName();
        return maps;
    }

}
