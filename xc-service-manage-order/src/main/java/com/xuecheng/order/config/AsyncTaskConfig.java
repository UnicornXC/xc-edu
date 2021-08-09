package com.xuecheng.order.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;

@Configuration
public class AsyncTaskConfig implements SchedulingConfigurer, AsyncConfigurer{

    private int corePoolSize = 5;

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        // 构建任务线程池
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // 初始化线程池
        scheduler.initialize();
        // 设置核心线程数
        scheduler.setPoolSize(corePoolSize);
        return scheduler;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskScheduler();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return AsyncConfigurer.super.getAsyncUncaughtExceptionHandler();
    }

}
