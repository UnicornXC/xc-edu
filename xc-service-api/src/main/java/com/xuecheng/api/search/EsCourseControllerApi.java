package com.xuecheng.api.search;


import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

/**
 * ES 检索课程的服务接口
 */
@Api(value = "课程搜索接口",description = "课程搜索服务", tags = {"对课程提供综合搜索服务"})
public interface EsCourseControllerApi {

    // 搜索课程的信息
    @ApiOperation("根据条件进行课程综合搜索，并以分页的形式展示出来")
    QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam);

    @ApiOperation("根据Id 查询课程信息")
    Map<String, CoursePub> getAll(String courseId);

    @ApiOperation("根据课程计划查询对应的媒资信息")
    TeachplanMediaPub getMediaByTeachplanId(String teachplanId);

}
