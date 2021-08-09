package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.client.CourseSearchClient;
import com.xuecheng.learning.dao.XcLearningCourseRepository;
import com.xuecheng.learning.dao.XcTaskHisRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class LearningService {

    @Autowired
    private CourseSearchClient courseSearchClient;

    @Autowired
    private XcLearningCourseRepository xcLearningCourseRepository;

    @Autowired
    private XcTaskHisRepository xcTaskHisRepository;

    // 在学习服务中调用课程管理服务,媒资管理服务获取课程学习地址
    public GetMediaResult getMediaInfo(String courseId, String teachplanId) {
        // 校验学生的学习权限，是否资费等
        // 调用搜索服务查询
        TeachplanMediaPub teachplanMediaPub = courseSearchClient.getMedia(teachplanId);
        if (teachplanMediaPub == null || StringUtils.isEmpty(teachplanMediaPub.getMediaUrl())) {
            // 获取课程播放地址出错
            ExceptionCast.cast(LearningCode.LEARNING_GET_MEDIA_ERROR);
        }
        // 返回媒资信息
        return new GetMediaResult(CommonCode.SUCCESS, teachplanMediaPub.getMediaUrl());
    }

    /**
     * 添加选课的记录，这个记录需要保证接口的幂等性，不能多次添加，不能少添加。
     * ----------------------------------------------------------
     * - 校验数据是否已经存在，存在就不再添加，直接返回，
     * - 数据不存在，需要添加新记录。
     * ----------------------------------------------------------
     * @param userId
     * @param courseId
     * @param valid
     * @param startTime
     * @param endTime
     * @param xcTask
     * @return
     */
    @Transactional
    public ResponseResult addChooseCourse(
            String userId, String courseId, String valid,
            Date startTime, Date endTime, XcTask xcTask
    ) {
        // 校验课程
        if (StringUtils.isEmpty(courseId)) {
            ExceptionCast.cast(LearningCode.LEARNING_GET_MEDIA_ERROR);
        }
        // 校验用户
        if (StringUtils.isEmpty(userId)) {
            ExceptionCast.cast(LearningCode.CHOOSE_COURSE_USER_ISNULL);
        }
        // 校验选课内容
        if (xcTask == null || StringUtils.isEmpty(xcTask.getId())) {
            ExceptionCast.cast(LearningCode.CHOOSE_COURSE_XCTASK_ISNULL);
        }
        //查询历史任务，看是否任务是否已经被处理过了
        Optional<XcTaskHis> optional = xcTaskHisRepository.findById(xcTask.getId());
        if (optional.isPresent()) {
            // 查询到结果，任务已经被处理了，不再处理，直接返回处理成功
            return new ResponseResult(CommonCode.SUCCESS);
        }
        // 不在历史任务中，这时候需要查询是否之前有选课记录，如果之前已经选了这个课程，那么只需要更新
        // 对应的记录即可
        XcLearningCourse xcLearningCourse =
                xcLearningCourseRepository.findByUserIdAndCourseId(userId, courseId);
        if (xcLearningCourse != null) {
            // 更新课程的起始，结束时间
            xcLearningCourse.setValid(valid);
            xcLearningCourse.setStatus("501001");
            xcLearningCourse.setStartTime(startTime);
            xcLearningCourse.setEndTime(endTime);
            xcLearningCourseRepository.save(xcLearningCourse);
        } else {
            xcLearningCourse = new XcLearningCourse();
            xcLearningCourse.setUserId(userId);
            xcLearningCourse.setCourseId(courseId);
            xcLearningCourse.setValid(valid);
            xcLearningCourse.setStartTime(startTime);
            xcLearningCourse.setEndTime(endTime);
            xcLearningCourse.setStatus("501001");
            xcLearningCourseRepository.save(xcLearningCourse);
        }
        // 向历史记录表中添加对应的记录
        XcTaskHis xcTaskHis = new XcTaskHis();
        BeanUtils.copyProperties(xcTask, xcTaskHis);
        xcTaskHisRepository.save(xcTaskHis);

        // 返回结果
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
