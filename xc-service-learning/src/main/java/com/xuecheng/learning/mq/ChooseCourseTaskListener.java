package com.xuecheng.learning.mq;


import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.service.LearningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class ChooseCourseTaskListener {

    @Autowired
    private LearningService learningService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final ThreadLocal<SimpleDateFormat> local = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    );

    @RabbitListener(
            queues = RabbitMQConfig.QUEUE_LEARNING_ADD_CHOOSE_COURSE
    )
    public void receiveChooseCourseTask(XcTask xcTask, Message message) {
        log.info("receive choose course message task: {}", xcTask.getId());

        String body = xcTask.getRequestBody();
        try {
            Map map = JSON.parseObject(body, Map.class);
            String userId = map.get("userId").toString();
            String courseId = map.get("courseId").toString();
            String valid = map.get("valid").toString();
            Date startTime = null;
            Date endTime = null;
            if (map.get("startTime") != null) {
                startTime = local.get().parse(map.get("startTime").toString());
            }
            if (map.get("endTime") != null) {
                endTime = local.get().parse(map.get("endTime").toString());
            }
            // 将数据请求service, 添加选课记录与添加任务历史记录
            ResponseResult responseResult
                    = learningService.addChooseCourse(userId, courseId, valid, startTime, endTime, xcTask);

            // 选课成功发送响应消息
            if (responseResult.isSuccess()) {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EX_LEARNING_ADD_CHOOSE_COURSE,
                        RabbitMQConfig.KEY_LEARNING_ADD_CHOOSE_COURSE_FINISH,
                        xcTask
                );
                log.info("send finish choose course message::{}", xcTask.getId());
            }
        } catch (ParseException e) {
            e.printStackTrace();
            log.error("解析选课消息内容异常,请检查内容格式是否正确");
        }
    }
}
