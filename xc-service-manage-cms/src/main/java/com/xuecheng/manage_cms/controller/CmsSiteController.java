package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.manage_cms.service.CmsSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cms/site")
public class CmsSiteController {

    @Autowired
    private CmsSiteService siteService;


    @GetMapping("list")
    public QueryResponseResult<CmsSite> getAll(){
        return siteService.findList();
    }

}
