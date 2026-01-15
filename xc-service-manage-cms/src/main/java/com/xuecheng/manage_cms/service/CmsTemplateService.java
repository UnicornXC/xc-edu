package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CmsTemplateService {

    @Autowired
    private CmsTemplateRepository templateRepository;

    public QueryResponseResult<CmsTemplate> findAll() {
        List<CmsTemplate> list = templateRepository.findAll();
        QueryResult<CmsTemplate> result = new QueryResult<>();
        result.setList(list);
        result.setTotal(list.size());
        return new QueryResponseResult<>(CommonCode.SUCCESS, result);
    }
}
