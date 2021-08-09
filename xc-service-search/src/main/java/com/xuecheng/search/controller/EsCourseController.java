package com.xuecheng.search.controller;

import com.xuecheng.api.search.EsCourseControllerApi;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.search.service.EsCourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/search/course")
public class EsCourseController implements EsCourseControllerApi {

    @Autowired
    private EsCourseService esCourseService;

    @Override
    @GetMapping(value = "/list/{page}/{size}")
    public QueryResponseResult<CoursePub> list(
            @PathVariable("page") int page,
            @PathVariable("size") int size,
            CourseSearchParam courseSearchParam
    ) {
        return esCourseService.list(page,size,courseSearchParam);
    }

    @Override
    @GetMapping("/getall/{id}")
    public Map<String, CoursePub> getAll(@PathVariable("id") String courseId) {
        return esCourseService.getAll(courseId);
    }

    @Override
    @GetMapping("/getmedia/{teachplanId}")
    public TeachplanMediaPub getMediaByTeachplanId(
            @PathVariable("teachplanId") String teachplanId
    ) {
        // 将课程计划Id放在数据中调用service服务

        QueryResponseResult<TeachplanMediaPub> medias
                = esCourseService.getMediaByTeachplanIds(new String[]{teachplanId});
        QueryResult<TeachplanMediaPub> queryResult = medias.getQueryResult();
        if (queryResult != null && queryResult.getList() != null && queryResult.getList().size() > 0) {
            // 返回课程计划对应的媒资信息
            return queryResult.getList().get(0);
        }
        // 查询失败返回一个空对象
        return new TeachplanMediaPub();
    }
}
