package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.CmsTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cms/template")
public class CmsTemplateController {

    @Autowired
    private CmsTemplateService templateService;

    @GetMapping("list/")
    public QueryResponseResult<CmsTemplate> findAll() {
        return templateService.findAll();
    }

    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult<CmsTemplate> findPage(
            @PathVariable int page,
            @PathVariable int size,
            QueryTemplateRequest queryTemplateRequest
    ) {
        return templateService.findPage(page,size,queryTemplateRequest);
    }

    @GetMapping("/get/{templateId}")
    public CmsTemplateResult findById(@PathVariable String templateId) {
        return templateService.findById(templateId);
    }

    @DeleteMapping("/del/{templateId}")
    public ResponseResult deleteById(@PathVariable String templateId) {
        return templateService.deleteById(templateId);
    }


    @PostMapping("/add")
    public CmsTemplateResult add(@RequestBody CmsTemplate cmsTemplate) {
        return templateService.addTemplate(cmsTemplate);
    }

    @PutMapping("/edit/{templateId}")
    public CmsTemplateResult edit(@PathVariable String templateId, @RequestBody CmsTemplate cmsTemplate){
        return templateService.updateTemplate(templateId, cmsTemplate);
    }

}
