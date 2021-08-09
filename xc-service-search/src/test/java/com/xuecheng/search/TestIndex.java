package com.xuecheng.search;


import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestIndex {


    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    RestClient restClient;

    /**
     * 测试删除索引
     * @throws IOException
     */
    @Test
    public void testDeleteIndex() throws IOException {
        // 删除索引对象
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("xc_course");
        // 操作索引的客户端
        IndicesClient indicesClient = restHighLevelClient.indices();
        // 执行删除索引
        DeleteIndexResponse delete = indicesClient.delete(deleteIndexRequest);
        // 获取响应
        boolean result = delete.isAcknowledged();
        System.out.println(result);

    }


    /**
     * 测试创建索引
     *
     * => PUT http://localhost:9200/索引库名称/类型名称/_mapping
     *
     */
    @Test
    public void testCreateIndex() throws IOException {

        // 创建索引的请求对象
        CreateIndexRequest create = new CreateIndexRequest("xc_course");
        // 设置参数
        create.settings(Settings.builder()
                .put("number_of_shards",1)
                .put("number_of_replicas",0));
        // 设置映射
        create.mapping("doc","{\n" +
                "    \"properties\": {\n" +
                "        \"description\": {\n" +
                "            \"type\": \"text\", \n" +
                "            \"analyzer\": \"ik_max_word\", \n" +
                "            \"search_analyzer\": \"ik_smart\"\n" +
                "        }, \n" +
                "        \"name\": {\n" +
                "            \"type\": \"text\", \n" +
                "            \"analyzer\": \"ik_max_word\", \n" +
                "            \"search_analyzer\": \"ik_smart\"\n" +
                "        }, \n" +
                "        \"pic\": {\n" +
                "            \"type\": \"text\", \n" +
                "            \"index\": false\n" +
                "        }, \n" +
                "        \"price\": {\n" +
                "            \"type\": \"float\"\n" +
                "        }, \n" +
                "        \"studymodel\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "        }, \n" +
                "        \"timestamp\": {\n" +
                "            \"type\": \"date\", \n" +
                "            \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis\"\n" +
                "        }\n" +
                "    }\n" +
                "}", XContentType.JSON);

        // 从客户端拿到操作索引的对象
        IndicesClient client = restHighLevelClient.indices();
        // 接收操作之后的对象
        CreateIndexResponse response = client.create(create);

        // 解析拿到的返回对象
        boolean result = response.isAcknowledged();
        System.out.println(result);
    }

    /**
     * 测试添加文档
     *
     * =>  PUT /{index}/{type}/{id} { "field": "value", ... }
     *
     */
    @Test
    public void testAddDocument() throws IOException {

        Map<String,Object> jsonMap = new HashMap<>();
        jsonMap.put("name","Spring Cloud 实战");
        jsonMap.put("description",
                "Bootstrap是由Twitter"
               + "推出的一个前台页面开发框架，是一个非常流行的开发框架，此框架集成了多种页面效果。此开发框架包含了大量"
               + "的CSS、JS程序代码，可以帮助开发者（尤其是不擅长页面开发的程序人员）轻松的实现一个不受浏览器限制的精"
               + "美界面效果。");
        jsonMap.put("studymodel","201002");
        jsonMap.put("price",38.6f);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        jsonMap.put("timestamp",format.format(new Date()));
        jsonMap.put("pic","group1/M00/00/00/wKhlQFs6RCeAY0pHAAJx5ZjNDEM428.jpg");

        // 索引文档请求对象
        IndexRequest indexRequest = new IndexRequest("xc_course","doc");

        // 制定索引文档的具体内容
        indexRequest.source(jsonMap);

        // 从ES操作的客户端，获取操作索引文档的对象
        IndexResponse response = restHighLevelClient.index(indexRequest);

        // 解析结果
        DocWriteResponse.Result result = response.getResult();

        // 打印结果
        System.out.println(result);
    }

    /**
     * 查询文档  => GET /{index}/{type}/{id}
     */
    @Test
    public void testGetDocument() throws IOException {

        // 构建查询对象
        GetRequest getRequest = new GetRequest("xc_course","doc","FH1n2HoByyvr5msBD-p3");

        // 接收返回对象
        GetResponse response = restHighLevelClient.get(getRequest);

        // 是否存在结果
        boolean isExists = response.isExists();
        // 解析结果
        if(isExists){
            Map<String,Object> sourseAsMap = response.getSourceAsMap();
            System.out.println(sourseAsMap);
        }
    }

    /**
     * 更新索引文档
     *
     *  1 -> 完全替换更新，传递的是完整的文档对象，使用的接口是
     *
     *  => Post：http://localhost:9200/xc_test/doc/3
     *
     *  2 -> 局部更新 传递的是需要更新的字段
     *
     *  => Post: http://localhost:9200/xc_test/doc/3/_update
     *
     *
     * @throws IOException
     */

    @Test
    public void testUpdateDocument() throws IOException {

        // 构建请求对象
        //UpdateRequest updateRequest = new UpdateRequest("xc_course","doc","FH1n2HoByyvr5msBD-p3");
        UpdateRequest updateRequest = new UpdateRequest("xc_course","doc","1235");
        Map<String,String> map = new HashMap<>();
        //map.put("name","Bootstrap 实战");
        map.put("studymodel","201004");
        updateRequest.doc(map);
        UpdateResponse response = restHighLevelClient.update(updateRequest);

        // 返回的更新状态
        RestStatus status = response.status();
        System.out.println(status);
    }


    /**
     *
     * 1-> 删除文档，根据Id删除，格式如下：
     *
     * => DELETE /{index}/{type}/{id}
     *
     * 2-> 删除文档，搜索匹配删除，将搜索出来的记录删除，格式如下： (暂时没有提供相关的API)
     *
     * => POST /{index}/{type}/_delete_by_query
     *
     */
    @Test
    public void testDeleteById() throws IOException {
        String id = "FH1n2HoByyvr5msBD-p3";

        // 删除索引文档的请求对象
        DeleteRequest deleteRequest = new DeleteRequest("xc_course","doc",id);
        // 接收返回结果
        DeleteResponse response = restHighLevelClient.delete(deleteRequest);
        // 解析结果
        DocWriteResponse.Result result = response.getResult();
        System.out.println(result);

    }
}
