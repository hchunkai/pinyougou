package com.pinyougou.manager.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.pojo.Specification;
import com.pinyougou.pojo.SpecificationOption;
import com.pinyougou.service.SpecificationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference(timeout = 10000)
    SpecificationService specificationService;


    @GetMapping("/delete")
    public boolean delete(Long[] ids) {
        try {
            specificationService.deleteAll(ids);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    public boolean update(@RequestBody Specification specification) {
        try {
            specificationService.update(specification);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 修改按钮显示规格选项数据
     */
    @GetMapping("/showSo")
    public List<SpecificationOption> showSo(Long id) {

        return specificationService.findSo(id);
    }


    @GetMapping("/findByPage")
    public PageResult findByPage(Specification specification, Integer page, Integer rows) {
        if (specification.getSpecName() != null && StringUtils.isNoneBlank(specification.getSpecName())) {
            try {
                specification.setSpecName(new String(specification
                        .getSpecName().getBytes("ISO8859-1"), "UTF-8"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return specificationService.findByPage(specification, page, rows);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public boolean save(@RequestBody Specification specification) {
        try {
            specificationService.save(specification);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/specIDandName")
    public List<Map<String, Object>> specIDandName() {
        return specificationService.specIDandName();
    }

}
