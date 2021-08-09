package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.XcTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Slf4j
@Component
public class ChooseCourseTask {

    /**
     * ----------------------------------------------------------------------------------------
     * 默认情况下
     * ----------------------------------------------------------------------------------------
     *   - 任务是串行执行的，多个任务按照顺序执行
     *   - 并行任务自行配置线程池 （AsyncTaskConfig）
     *
     * ----------------------------------------------------------------------------------------
     * cron 表达式, 六位子表达式的含义
     * *  (0-59) 秒
     * *  (0-59) 分
     * *  (0-23) 时
     * *  (1-31) 月中天
     * *  (1-12) 月
     * *  (1-7)  星期中的天
     * --------------------
     * "/"  时间增量
     * "*"  表示任意量
     * "-"  表示时间范围
     * ","  表示时间列表
     * "?"  仅用于 `月中天` 或 `星期中的天` 两个子表达式中的一个，表示不确定  (一般其中一个确定另一个不确定)
     * ---------------------------------------------------------------------------------------
     */

    // 上一次任务调度执行完成后 3 秒后开始下一次的调度
    // @Scheduled(fixedDelay = 3000)
    // 在任务开始后 3 秒后开启下一次调度, 当上一个任务没有执行完, 等待上一个任务完成再开始下一个任务
    // @Scheduled(fixedRate = 3001)
    // 初始化延迟 3 秒开始执行, 之后每 5 秒执行一次
    // @Scheduled(initialDelay = 3000, fixedRate = 5000)
    // 每隔 5 秒执行, 当上一个任务执行没有完成，等待上一个任务执行完成开始下一个任务
    // @Scheduled(cron = "0/5 * * * * *")
    public void printLogTask1(){
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("=====>>{}::log1 <<=====",Thread.currentThread().getName());
    }
    //@Scheduled(cron = "0/2 * * * * *")
    public void printLogTask2(){
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("=====>>{}::log2 <<=====",Thread.currentThread().getName());
    }

    // ---------------------------------------------------------------------------------------

    @Autowired
    private XcTaskService xcTaskService;

    @Scheduled(fixedDelay = 60000)
    public void getTaskData(){
        // 取出当前时间的 1 分钟之前
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE, -1);
        Date time = calendar.getTime();
        List<XcTask> xcTasks = xcTaskService.findByUpdateTimeBefore(time, 1000);
        // System.out.println(xcTasks);
        // 将消息发布到RabbitMQ
        xcTasks.forEach(e ->{
            // 使用乐观锁确认任务
            if (xcTaskService.checkTask(e.getId(), e.getVersion()) > 0) {
                String exchange = e.getMqExchange();
                String routingKey = e.getMqRoutingkey();
                xcTaskService.publishTask(e, exchange, routingKey);
                log.info("send choose course task id :: {}", e.getId());
            }
        });
    }

    // 监听 RabbitMQ 中选课完成的消息，结束选课任务
    @RabbitListener(
           queues = { RabbitMQConfig.QUEUE_LEARNING_ADD_CHOOSE_COURSE_FINISH }
    )
    public void finishChooseCourseListener(XcTask xcTask, Message message) {
        log.info("receive choose course Task finish :: {}", xcTask.getId());
        // 结束任务
        xcTaskService.finishTask(xcTask.getId());
    }
}
