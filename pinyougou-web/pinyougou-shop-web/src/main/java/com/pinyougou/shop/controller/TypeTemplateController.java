package com.pinyougou.shop.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TypeTemplate;
import com.pinyougou.service.TypeTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {

    @Reference(timeout = 10000)
    TypeTemplateService templateService;


    @GetMapping("/findOne")
    public TypeTemplate findOne(Long id) {
        return templateService.findOne(id);
    }

    /** 根据模版id查询所有的规格与规格选项 */
    @GetMapping("/findSpec")
    public List<Map> findSpecByTemplateId(Long id){
        return templateService.findSpec(id);
    }

}
