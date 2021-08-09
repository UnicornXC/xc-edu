package com.xuecheng.manage_course.controller;

import com.xuecheng.api.cource.CourseControllerApi;
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
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.XcOauth2Util;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
public class CourseController extends BaseController implements CourseControllerApi{

    @Autowired
    CourseService courseService;

    @Override
    @PostMapping("/teachplan/add")
    public ResponseResult addTeachplan(@RequestBody Teachplan teachplan) {
        System.out.println(teachplan);
        return courseService.addTeachplan(teachplan);
    }

    @Override
    @PreAuthorize("hasAuthority('xc_teachmanager_course_plan')")
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(@PathVariable("courseId") String courseId) {
        return courseService.findTeachplanList(courseId);
    }

    @Override
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    @GetMapping("/coursebase/list/{page}/{size}")
    public QueryResponseResult<CourseInfo> qureyCourseByPage(
            @PathVariable("page") int page,
            @PathVariable("size") int size,
            CourseListRequest courseListRequest
    ) {
        // 获取用户对应的公司id, 查询时只能查询到自己所属的机构的信息
        XcOauth2Util xcOauth2Util = new XcOauth2Util();
        XcOauth2Util.UserJwt jwt = xcOauth2Util.getUserJwtFromHeader(request);
        String companyId = jwt.getCompanyId();
        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }
        courseListRequest.setCompanyId(companyId);
        return courseService.findCourseListPage(page,size,courseListRequest);
    }

    @Override
    @PostMapping("/coursebase/add")
    public AddCourseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);

    }

    @Override
    @GetMapping("/coursebase/get/{courseId}")
    public CourseBase getCourseBaseById(@PathVariable("courseId") String courseId) throws RuntimeException {
        return courseService.getCourseBaseById(courseId);
    }

    @Override
    @PutMapping("/coursebase/update/{courseId}")
    public ResponseResult updateCourseBase(@PathVariable("courseId") String courseId, @RequestBody CourseBase courseBase) {
        return courseService.updateCourseBaseById(courseId,courseBase);
    }

    @Override
    @GetMapping("/coursemarket/get/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId) {
        return courseService.getCourseMarketById(courseId);
    }

    @Override
    @PostMapping("/coursemarket/update/{courseId}")
    public ResponseResult updateCourseMarket(@PathVariable("courseId") String courseId, @RequestBody CourseMarket courseMarket) {
        CourseMarket marketInfo = courseService.updateCourseMarket(courseId,courseMarket);
        if ( marketInfo!=null){
            return new ResponseResult(CommonCode.SUCCESS);
        }else{
            return new ResponseResult(CommonCode.FAIL);
        }
    }

    @Override
    @GetMapping("/courseview/{id}")
    public CourseView courseView(@PathVariable("id")String id) {
        return courseService.getCourseView(id);
    }

    @Override
    @PostMapping("/preview/{courseId}")
    public CoursePublishResult preview(@PathVariable("courseId") String courseId) {
        return courseService.preview(courseId);
    }

    @Override
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable("id") String id) {
        return courseService.publish(id);
    }

    @Override
    @PostMapping("/savemedia")
    public ResponseResult saveMedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.saveMedia(teachplanMedia);
    }
}