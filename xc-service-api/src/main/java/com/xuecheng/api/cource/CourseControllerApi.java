package com.xuecheng.api.cource;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CoursePublishResult;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;

@Api(value="课程管理接口",description="课程管理接口，提供课程的增、删、改、查")
public interface CourseControllerApi {
    @ApiOperation("课程计划查询")
    TeachplanNode findTeachplanList(String courseId);

    @ApiOperation("添加课程计划")
    ResponseResult addTeachplan(Teachplan teachplan);

    @ApiOperation("查询我的课程列表")
    QueryResponseResult<CourseInfo> qureyCourseByPage(
            int page,
            int size,
            CourseListRequest courseListRequest);

    @ApiOperation("添加课程的基本信息")
    AddCourseResult addCourseBase(CourseBase courseBase);

    @ApiOperation("根据课程的id查询课程的基本信息")
    CourseBase getCourseBaseById(String CourseId) throws RuntimeException;

    @ApiOperation("更新课程基本信息")
    ResponseResult updateCourseBase(String courseId,CourseBase courseBase);

    @ApiOperation("根据课程的id信息查询课程的营销信息")
    CourseMarket getCourseMarketById(String courseId);

    @ApiOperation("根据课程的id信息更新课程的营销信息")
    ResponseResult updateCourseMarket(String courseId,CourseMarket courseMarket);

    @ApiOperation("根据课程的id查询课程视图页面")
    CourseView courseView(String id);

    @ApiOperation("根据课程的 Id 获取课程预览的url ")
    CoursePublishResult preview(String courseId);

    @ApiOperation("发布课程")
    CoursePublishResult publish(@PathVariable String id);

    @ApiOperation("保存媒资与课程计划的关系信息")
    ResponseResult saveMedia(TeachplanMedia teachplanMedia);



}