package com.xuecheng.search.config;


import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * 为项目配置 全文检索服务器的配置，所有的接口调用基于ElasticSearch 提供的 Restful 接口
 *  - High level Restful Client
 *  - 少数不支持的使用 low level restful client
 */
@Configuration
public class ElasticsearchConfig {

    @Value("${xuecheng.elasticsearch.hostlist}")
    private String hostlist;

    @Bean
    public RestHighLevelClient restHighLevelClient(){
        // 解析Hostlist中配置的搜索服务的全部节点信息
        String[] hosts = hostlist.split(",");
        // 创建HttpHost 数组，其中存放所有的ES主机和端口的信息
        HttpHost[] httpHostArray = new HttpHost[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            String item = hosts[i];
            httpHostArray[i] = new HttpHost(item.split(":")[0],Integer.parseInt(item.split(":")[1]));
        }
        // 创建RestHighLevelClient
        return new RestHighLevelClient(RestClient.builder(httpHostArray));
    }

    // 这是低级的客户端，官方计划放弃维护，在高级客户端还不完善的情况下，偶尔会使用
    @Bean
    public RestClient restClient(){
        // 解析配置文件中配置的ES主机的地址和端口信息，
        String[] hosts = hostlist.split(",");
        // 构建主机访问数组，用于建造连接客户端
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            httpHosts[i] = new HttpHost(hosts[i].split(":")[0],Integer.parseInt(hosts[i].split(":")[1]));
        }
        // 建造者模式获取连接主机与端口的信息，建造客户端后返回
        return RestClient.builder(httpHosts).build();
    }








}
