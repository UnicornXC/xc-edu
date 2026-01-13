package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


@Api(value = "cms页面管理接口",description ="cms页面管理接口，提供页面的CRUD" )
public interface CmsPageControllerApi {

    @ApiOperation("分页查询页面列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page",value = "页码",required = true),
            @ApiImplicitParam(name = "size", value = "每页记录数", required = true)
            })
    QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    @ApiOperation("添加页面")
    CmsPageResult add(CmsPage cmsPage);

    //根据页面的id查询页面信息
    @ApiOperation("根据页面的id查询页面信息")
    CmsPageResult findById(String id);

    //根据页面id更新页面的信息
    @ApiOperation("根据id更新页面")
    CmsPageResult update(String id, CmsPage cmsPage);

    @ApiOperation("删除页面")
    ResponseResult delete(String id);

    @ApiOperation("发布页面")
    ResponseResult post(String pageId);

    @ApiOperation("更新或添加页面")
    CmsPageResult save(CmsPage cmsPage);

    @ApiOperation("一键发布页面")
    CmsPostPageResult postPageQuick(CmsPage cmsPage);

}
