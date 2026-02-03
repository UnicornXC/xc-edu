package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 课程搜索
 */
@Slf4j
@Service
public class EsCourseService {

    //------------------课程发布后提供的索引信息----------------------------
    @Value("${xuecheng.course.index}")
    private String index;
    @Value("${xuecheng.course.type}")
    private String type;
    @Value("${xuecheng.course.source_field}")
    private String source_field;

    //------------------课程计划与媒资关联索引信息---------------------------
    @Value("${xuecheng.media.index}")
    private String media_index;
    @Value("${xuecheng.media.type}")
    private String media_type;
    @Value("${xuecheng.media.source_field}")
    private String media_source_field;
    
    //----------------- ES API 客户端 -----------------------------------
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private RestClient restClient;

    
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
        if(null == courseSearchParam){
            courseSearchParam = new CourseSearchParam();
        }
        // 创建搜索请求
        SearchRequest request = new SearchRequest(index);
        // request.types(type);
        final String[] fields = source_field.split(",");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(
                fields,
                new String[]{}
        );
        // 设置搜索的条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if(StringUtils.isNotEmpty(courseSearchParam.getKeyword())){
            boolQuery.must(
                QueryBuilders.multiMatchQuery(
                        courseSearchParam.getKeyword(),
                        "name","description","teachplan"
                ).minimumShouldMatch("70%").field("name",10)
            );
        }
        // 按照一级分类来搜索
        if(StringUtils.isNotEmpty(courseSearchParam.getMt())){
            boolQuery.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        // 按照二级分类来搜索
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())){
            boolQuery.filter(QueryBuilders.termQuery("st",courseSearchParam.getMt()));
        }
        // 按照难度等级来搜索
        if(StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            boolQuery.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }
        // -------------设置搜索分页-------------------------
        if (page <= 0){
            page = 1;
        }
        if(size <= 0){
            size = 20;
        }
        searchSourceBuilder.from((page-1) * size).size(size).query(boolQuery);
        // 设置搜索高亮信息
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .preTags("<font class='eslight'>").postTags("</font>");
        // 添加需要高亮的字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        // 发送搜索的请求
        request.source(searchSourceBuilder);
        QueryResult<CoursePub> result = new QueryResult<>();
        List<CoursePub> coursePubList = new ArrayList<>();
        try {
            // 执行查询
            SearchResponse response = restHighLevelClient.search(request);
            // 获取响应结果
            SearchHits searchHits = response.getHits();
            long total = searchHits.totalHits;
            result.setTotal(total);
            SearchHit[] hits = searchHits.getHits();
            Stream.of(hits).forEach(e->{
                // 获取源文档记录
                Map<String, Object> sourceAsMap = e.getSourceAsMap();
                // 构建返回的对象单体
                CoursePub coursePub = new CoursePub();
                String id = sourceAsMap.get("id").toString();
                coursePub.setId(id);
                // 根据前端展示的需求返回对应的信息
                String name = (String) sourceAsMap.get("name");
                Map<String, HighlightField> highlightFields = e.getHighlightFields();
                if (null != highlightFields){
                    HighlightField field = highlightFields.get("name");
                    if(null!=field){
                        Text[] fragments = field.getFragments();
                        StringBuilder stringBuilder = new StringBuilder();
                        Stream.of(fragments).forEach(stringBuilder::append);
                        name = stringBuilder.toString();
                    }
                }
                coursePub.setName(name);
                // 图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                // 查找价格
                Float price = null;
                try {
                    if(null != sourceAsMap.get("price")){
                        price = Float.parseFloat(sourceAsMap.get("price").toString());
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                coursePub.setPrice(price);
                Float price_old = null;
                try{
                    if (null != sourceAsMap.get("price_old")){
                        price_old = Float.parseFloat(sourceAsMap.get("price_old").toString());
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                coursePub.setPrice_old(price_old);
                coursePubList.add(coursePub);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        result.setList(coursePubList);
        return new QueryResponseResult<>(CommonCode.SUCCESS, result);
    }

    /**
     * 根据课程，查询课程的全部课程计划信息
     * @param courseId
     * @return
     */
    public Map<String, CoursePub> getAll(String courseId) {
        // 设置索引库
        SearchRequest request = new SearchRequest(index);
        request.types(type);
        // 查询条件构建
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(
                QueryBuilders.termQuery("id", courseId)
        );
        searchSourceBuilder.fetchSource(
                new String[]{"id","name","grade","charge","pic","description","teachplan"},
                new String[]{}
        );
        request.source(searchSourceBuilder);
        try {
            // 执行搜索
            SearchResponse response = restHighLevelClient.search(request);
            // 获取搜索结果
            SearchHits searchHits = response.getHits();
            SearchHit[] hits = searchHits.getHits();
            System.out.println(hits.length);
            Map<String, CoursePub> resultMap = new HashMap<>();
            Stream.of(hits).forEach(e->{
                // String id = e.getId();
                Map<String, Object> sourceMap = e.getSourceAsMap();
                String id = sourceMap.get("id").toString();
                String name = sourceMap.get("name").toString();
                String grade = sourceMap.get("grade").toString();
                String charge = sourceMap.get("charge").toString();
                String pic = sourceMap.get("pic").toString();
                String description = sourceMap.get("description").toString();
                String teachplan = sourceMap.get("teachplan").toString();
                CoursePub coursePub = new CoursePub();
                coursePub.setId(id);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setCharge(charge);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                resultMap.put(courseId, coursePub);
            });
            return resultMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据课程计划查询对应的媒资信息
     * --------------------------------------------------------------
     * 为提高程序的通用性，将课程计划传递的参数设置为数组
     * @param teachplanIds
     * @return
     */
    public QueryResponseResult<TeachplanMediaPub> getMediaByTeachplanIds(String[] teachplanIds) {
        //设置索引
        SearchRequest request = new SearchRequest(media_index);
        request.types(media_type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置查询的字段名称
        String[] media_fields = media_source_field.split(",");
        searchSourceBuilder.fetchSource(
            media_fields,
            new String[]{}
        );
        // 设置查询条件
        searchSourceBuilder.query(
                QueryBuilders.termsQuery(
                        "teachplan_id",teachplanIds
                )
        );
        request.source(searchSourceBuilder);
        try{
            // 进行查询并封装查询结果
            SearchResponse response = restHighLevelClient.search(request);
            SearchHits searchHits = response.getHits();
            SearchHit[] hits = searchHits.getHits();
            List<TeachplanMediaPub> pubs = new ArrayList<>();
            Stream.of(hits).forEach(e->{
                Map<String, Object> sourceAsMap = e.getSourceAsMap();
                String courseid = sourceAsMap.get("courseid").toString();
                String media_id = sourceAsMap.get("media_id").toString();
                String media_url = sourceAsMap.get("media_url").toString();
                String teachplan_id = sourceAsMap.get("teachplan_id").toString();
                String media_fileorigianlname = sourceAsMap.get("media_fileoriginalname").toString();

                TeachplanMediaPub pub = new TeachplanMediaPub();
                pub.setCourseId(courseid);
                pub.setMediaId(media_id);
                pub.setMediaUrl(media_url);
                pub.setTeachplanId(teachplan_id);
                pub.setMediaFileOriginalName(media_fileorigianlname);
                pubs.add(pub);
            });
            // 构建返回课程媒资信息对象
            QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
            queryResult.setList(pubs);
            return new QueryResponseResult<>(
                    CommonCode.SUCCESS, queryResult
            );
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询课程计划媒资信息,查询参数: {},\n------------------------\n错误信息:{}",teachplanIds,e.getMessage());
            return null;
        }
    }
}
