package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.pojo.TypeTemplate;
import com.pinyougou.service.TypeTemplateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {

    @Reference(timeout = 10000)
    private TypeTemplateService templateService;

    @GetMapping("/findByPage")
    public PageResult findByPage(TypeTemplate typeTemplate, Integer page, Integer rows) {
        if (typeTemplate != null && StringUtils.isNoneBlank(typeTemplate.getName())) {
            try {
                typeTemplate.setName(new String(typeTemplate.getName().getBytes("ISO8859-1"), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return templateService.findByPage(typeTemplate, page, rows);
    }

    @PostMapping("/save")
    public boolean save(@RequestBody TypeTemplate typeTemplate) {
        try {
            templateService.save(typeTemplate);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/update")
    public boolean update(@RequestBody TypeTemplate typeTemplate) {
        try {
            templateService.update(typeTemplate);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/delete")
    public boolean delete(Long[] ids) {
        try {
            templateService.deleteAll(ids);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/findTypeTemplateList")
    public List<Map<String, Object>> findTypeTemplateList() {
      return templateService.findTypeTemplateList();
    }


}
