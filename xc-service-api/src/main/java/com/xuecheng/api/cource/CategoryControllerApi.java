package com.xuecheng.api.cource;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="课程分类接口",description="后台课程分类管理接口，提供课程分类的查询、")
public interface CategoryControllerApi {

    @ApiOperation("查询课程分类信息")
    CategoryNode findCategoryNode();
}
