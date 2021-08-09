package com.xuecheng.order.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    //添加选课任务交换机
    public static final String EX_LEARNING_ADD_CHOOSE_COURSE = "ex_learning_add_choose_course";

    // 添加 开始选课的消息队列
    public static final String QUEUE_LEARNING_ADD_CHOOSE_COURSE = "queue_learning_add_choose_course";
    // 添加 开始选课路由key
    public static final String KEY_XC_LEARNING_ADD_CHOOSE_COURSE = "key_add_choose_course";
    // 添加 完成选课消息队列
    public static final String QUEUE_LEARNING_ADD_CHOOSE_COURSE_FINISH = "queue_learning_add_choose_course_finish";
    // 添加 完成选课路由key
    public static final String KEY_LEARNING_ADD_CHOOSE_COURSE_FINISH = "key_add_choose_course_finish";

    /**
     * 交换机配置
     * @return the exchange
     */
    @Bean(EX_LEARNING_ADD_CHOOSE_COURSE)
    public Exchange EX_DECLARE() {
        return ExchangeBuilder.directExchange(EX_LEARNING_ADD_CHOOSE_COURSE)
                .durable(true).build();
    }

    //---------------------------------------------------------------------------
    // 声明 开始选课消息队列
    @Bean(QUEUE_LEARNING_ADD_CHOOSE_COURSE)
    public Queue chooseQueue(){
        return new Queue(
                QUEUE_LEARNING_ADD_CHOOSE_COURSE,
                true,
                false,
                true
        );
    }
    // 绑定 开始选课的队列到交换机器
    @Bean
    public Binding bind_queue_choose_course_start(
            @Qualifier(QUEUE_LEARNING_ADD_CHOOSE_COURSE)Queue queue,
            @Qualifier(EX_LEARNING_ADD_CHOOSE_COURSE)Exchange exchange
    ){
        return BindingBuilder.bind(queue).to(exchange)
                .with(KEY_XC_LEARNING_ADD_CHOOSE_COURSE).noargs();
    }

    //---------------------------------------------------------------------------
    //声明 选课完成消息 队列
    @Bean(QUEUE_LEARNING_ADD_CHOOSE_COURSE_FINISH)
    public Queue finishQueue() {
        return new Queue(
                QUEUE_LEARNING_ADD_CHOOSE_COURSE_FINISH,
                true,
                false,
                true
        );
    }
    /**
     * 绑定 选课完成队列到交换机.
     * @param queue    the queue
     * @param exchange the exchange
     * @return the binding
     */
    @Bean
    public Binding binding_queue_choose_course_success(
            @Qualifier(QUEUE_LEARNING_ADD_CHOOSE_COURSE_FINISH) Queue queue,
            @Qualifier(EX_LEARNING_ADD_CHOOSE_COURSE) Exchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange)
                .with(KEY_LEARNING_ADD_CHOOSE_COURSE_FINISH).noargs();
    }
}
