package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CmsTemplateService {

    @Autowired
    private CmsTemplateRepository templateRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    public QueryResponseResult<CmsTemplate> findAll() {
        List<CmsTemplate> list = templateRepository.findAll();
        QueryResult<CmsTemplate> result = new QueryResult<>();
        result.setList(list);
        result.setTotal(list.size());
        return new QueryResponseResult<>(CommonCode.SUCCESS, result);
    }

    public QueryResponseResult<CmsTemplate> findPage(int page, int size, QueryTemplateRequest queryTemplateRequest) {

        if (queryTemplateRequest == null) {
            queryTemplateRequest = new QueryTemplateRequest();
        }
        //自定义条件查询
        //自定义条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher
                .matching()
                .withMatcher(
                        "templateName",
                        ExampleMatcher.GenericPropertyMatchers.contains()
                );
        //定义值对象
        CmsTemplate cmsTemplate = new CmsTemplate();
        //设置条件值
        if (!StringUtils.isEmpty(queryTemplateRequest.getSiteId())){
            cmsTemplate.setSiteId(queryTemplateRequest.getSiteId());
        }
        if (!StringUtils.isEmpty(queryTemplateRequest.getTemplateId())){
            cmsTemplate.setTemplateId(queryTemplateRequest.getTemplateId());
        }
        //定义条件对象
        Example<CmsTemplate> example = Example.of(cmsTemplate,exampleMatcher);

        if (page <= 0) {
            page = 1;
        }
        page = page - 1;
        if (size <= 0) {
            size = 10;
        }
        //分页对象
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsTemplate> list = templateRepository.findAll(example,pageable);//条件查询
        QueryResult<CmsTemplate> queryResult = new QueryResult<>();
        queryResult.setList(list.getContent());
        queryResult.setTotal(list.getTotalElements());
        return new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
    }

    public CmsTemplateResult findById(String templateId) {
        Optional<CmsTemplate> ops = templateRepository.findById(templateId);
        return ops.map(cmsTemplate -> new CmsTemplateResult(CommonCode.SUCCESS, cmsTemplate))
                .orElseGet(() -> new CmsTemplateResult(CommonCode.FAIL, null));
    }


    public ResponseResult deleteById(String templateId) {
        CmsTemplateResult result = findById(templateId);
        if (!result.isSuccess()) {
            return ResponseResult.FAIL();
        }
        CmsTemplate template = result.getCmsTemplate();

        String fileId = template.getTemplateFileId();
        if (fileId != null) {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(fileId)));
        }
        templateRepository.deleteById(templateId);
        return ResponseResult.SUCCESS();
    }

    public CmsTemplateResult addTemplate(CmsTemplate cmsTemplate) {
        CmsTemplate template = templateRepository.findBySiteIdAndTemplateName(
                cmsTemplate.getSiteId(), cmsTemplate.getTemplateName()
        );
        if (template == null) {
            cmsTemplate.setTemplateId(null);
            templateRepository.save(cmsTemplate);
        }
        return new CmsTemplateResult(CommonCode.INVALID_PARAM, null);
    }

    public CmsTemplateResult updateTemplate(String templateId, CmsTemplate cmsTemplate) {
        CmsTemplateResult result = findById(templateId);
        if (!result.isSuccess()){
            return new CmsTemplateResult(CommonCode.FAIL, null);
        }
        CmsTemplate cms = result.getCmsTemplate();
        cms.setTemplateName(cmsTemplate.getTemplateName());
        cms.setTemplateParameter(cmsTemplate.getTemplateParameter());
        cms.setTemplateFileId(cmsTemplate.getTemplateFileId());
        cms.setSiteId(cmsTemplate.getSiteId());
        templateRepository.save(cms);
        return new CmsTemplateResult(CommonCode.SUCCESS, cms);
    }
}
