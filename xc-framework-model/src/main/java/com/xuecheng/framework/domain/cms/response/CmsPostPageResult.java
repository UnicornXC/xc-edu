package com.xuecheng.framework.domain.cms.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于发布成功，cms 返回页面，可以访问的页面url
 *
 *    url = CmsSite.siteDomain + CmsSite.siteWebPath
 *          + CmsPage.pageWebPath + CmsPage.pageName
 */

@Data
@NoArgsConstructor
public class CmsPostPageResult extends ResponseResult {

    private String pageUrl;

    public CmsPostPageResult(ResultCode code,String pageUrl){
        super(code);
        this.pageUrl = pageUrl;

    }
}
