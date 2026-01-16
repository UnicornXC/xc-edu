package com.xuecheng.manage_course;

import com.xuecheng.framework.interceptor.FeignClientInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author Administrator
 * @version 1.0
 **/
@EnableFeignClients     /* 注明开启feign客户端 */
@EnableDiscoveryClient  /* 注册eureka服务到eureka服务器,同时自己也是eureka 的调用者，所以需要发现其他服务 */
@EntityScan("com.xuecheng.framework.domain.course")//扫描实体类
@ComponentScan(basePackages={"com.xuecheng.api"})//扫描接口
@ComponentScan(basePackages={"com.xuecheng.manage_course"})
@ComponentScan(basePackages={"com.xuecheng.framework"})//扫描common下的所有类
@SpringBootApplication(
    exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
    }
)
public class ManageCourseApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ManageCourseApplication.class, args);
    }

    @Bean
    /*
    * 该注解可实现负载均衡
    *  配置不同的参数的时候可以使用不同的负载均衡算法
    *   随机 (Random)
    *   轮询 (RoundRobin)
    *   一致性哈希 (ConsistentHash)
    *   哈希 (Hash)
    *   加权（Weighted）
    *
    * */
    @LoadBalanced()
    public RestTemplate restTemplate(){
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }


    /**
     * 将 Feign 拦截器注入到服务中
     * @return
     */
    @Bean
    public FeignClientInterceptor feignClientInterceptor(){
        return new FeignClientInterceptor();
    }
}
