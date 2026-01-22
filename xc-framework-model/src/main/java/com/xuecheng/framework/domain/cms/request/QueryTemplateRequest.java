package com.xuecheng.framework.domain.cms.request;

import lombok.Data;

//接受模板查询的条件
@Data
public class QueryTemplateRequest {
    //站点id
    private String siteId;
    //模板名称
    private String templateName;
    //模板id
    private String templateId;
}
