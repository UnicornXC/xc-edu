package com.xuecheng.manage_media_process.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Administrator
 * @version 1.0
 * @create 2018-07-12 9:04
 **/
@Configuration
public class RabbitMQConfig {

    public static final String EX_MEDIA_PROCESS_TASK = "ex_media_processor";

    public static final String QUEUE_MEDIA_VIDEO_PROCESS_TASK = "queue_media_video_processtask";


    //视频处理队列
    @Value("${xc-service-manage-media.mq.queue-media-video-processor}")
    public  String queue_media_video_processtask;

    //视频处理路由
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    public  String routingkey_media_video;

    //消费者并发数量
    public static final int DEFAULT_CONCURRENT = 10;


    /**
     * 交换机配置
     * @return the exchange
     */
    @Bean(EX_MEDIA_PROCESS_TASK)
    public Exchange EX_MEDIA_VIDEOTASK() {
        return ExchangeBuilder.directExchange(EX_MEDIA_PROCESS_TASK).durable(true).build();
    }
    //声明队列
    @Bean(QUEUE_MEDIA_VIDEO_PROCESS_TASK)
    public Queue QUEUE_PROCESSTASK() {
        Queue queue = new Queue(queue_media_video_processtask,true,false,true);
        return queue;
    }
    /**
     * 绑定队列到交换机 .
     * @param queue    the queue
     * @param exchange the exchange
     * @return the binding
     */
    @Bean
    public Binding binding_queue_media_processtask(
            @Qualifier(QUEUE_MEDIA_VIDEO_PROCESS_TASK) Queue queue,
            @Qualifier(EX_MEDIA_PROCESS_TASK) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingkey_media_video).noargs();
    }

    /**
     * 由于视频处理是一个非常耗时的操作，默认的rabbitMQ 消息的消费是单线程监听，当消息任务较多的时候，每次消费一个
     * 消息，会造成大量消息堆积，处理缓慢，不利于利用硬件资源。
     * ---------------------------------------------------------------------------------------
     * 配置消息处理的容器工厂参数，增加并发处理数量，即实现多线程监听队列，实现多线程处理消息。
     * @param configurer
     * @param connectionFactory
     * @return
     */
    @Bean("customContainerFactory")
    public SimpleRabbitListenerContainerFactory containerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConcurrentConsumers(DEFAULT_CONCURRENT);
        factory.setMaxConcurrentConsumers(DEFAULT_CONCURRENT);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

}
