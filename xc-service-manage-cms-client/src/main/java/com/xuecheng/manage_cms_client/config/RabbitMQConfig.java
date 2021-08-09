package com.xuecheng.manage_cms_client.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    //队列Bean的名称
    public static final String QUEUE_CMS_POSTPAGE="queue_cms_postpage";
    //交换机Bean的名称
    public static final String EX_ROUTING_CMS_POSTPAGE="ex_routing_cms_postpage";
    //队列的名称
    @Value("${xuecheng.mq.queue}")
    public String queue_cms_postpage_name;
    @Value("${xuecheng.mq.routingKey}")
    public String routingKey;

    /**
     *声明交换机,交换机配置使用direct类型
     *
     */
    @Bean(EX_ROUTING_CMS_POSTPAGE)
    public Exchange EX_ROUTING_CMS_POSTPAGE(){
        return ExchangeBuilder.directExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();
    }
    /**
     *声明队列
     *
     */
    @Bean(QUEUE_CMS_POSTPAGE)
    public Queue QUEUE_CMS_POSTPAGE(){
        return new Queue(queue_cms_postpage_name);
    }
    /**
     *队列与交换机绑定
     *
     */
    @Bean
    public Binding BIND_QUEUE_CMS_POSTPAGE(
            @Qualifier(QUEUE_CMS_POSTPAGE)Queue queue,
            @Qualifier(EX_ROUTING_CMS_POSTPAGE)Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
    }
}
