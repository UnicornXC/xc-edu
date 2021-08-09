package com.xuecheng.learning.controller;

import com.xuecheng.api.learning.CourseLearningControllerApi;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.learning.service.LearningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/learning/course")
public class CourseLearningController implements CourseLearningControllerApi {

    @Autowired
    private LearningService learningService;

    @Override
    @GetMapping("/getmedia/{courseId}/{teachplanId}")
    public GetMediaResult getMedia(
            @PathVariable("courseId") String courseId,
            @PathVariable("teachplanId") String teachplanId
    ) {
        // 获取课程学习地址
        return learningService.getMediaInfo(courseId,teachplanId);
    }
}
