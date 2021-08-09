package com.xuecheng.search;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestManageIndex {


    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private RestClient restClient;

    /**
     * => Post https://localhost:9200/xc_course/doc/_search
     * {
     *     ”query“: {
     *         "match_all":{}
     *     },
     *     "_sourse": ["name","studymodel"]
     * }
     * _sourse 指定查询结果中的原文档显示的字段
     * @throws IOException
     */
    @Test
    public void testSearchAll() throws IOException {

        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 查询所有的内容
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // source 源字段过滤, 限制查询出来的 _sourse 原始文档包含的内容
        searchSourceBuilder.fetchSource(
                new String[]{"name","studymodel"},
                new String[]{}
        );
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();

        Stream.of(searchHits).forEach(e -> {
            String index = e.getIndex();
            String type = e.getType();
            String id = e.getId();
            float score = e.getScore();
            String sourceAsString = e.getSourceAsString();
            Map<String,Object> sourceAsMap = e.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        });
    }

    /**
     * 测试分页查询
     * post http://localhost:9200/xc_course/doc/_search
     * {
     *     "from": 0,
     *     "size": 1,
     *     "query":{
     *         "match_all":{}
     *     },
     *     "_source": ["name","studymodel"]
     * }
     */
    @Test
    public void testSearchPage() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // 分页查询，设置起始的下标，从 0 开始
        searchSourceBuilder.from(1);
        // 每页显示的个数
        searchSourceBuilder.size(2);
        // source 源中显示的字段
        searchSourceBuilder.fetchSource(
                new String[]{"name","studymodel"},
                new String[]{}
        );
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        // 查询结果解析
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        Stream.of(searchHits).forEach(e -> {
            String index = e.getIndex();
            String type = e.getType();
            String id = e.getId();
            float score = e.getScore();
            // 转化为字符串
            String sourceAsString = e.getSourceAsString();
            System.out.println(sourceAsString);
            // 转化为map
            Map<String,Object> sourceAsMap = e.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        });
    }

    /**
     * 测试根据 Term 精确查询, 查询时会精确匹配，不会将查询的关键字分词
     * Post http://localhost:9200/xc_cource/doc/_search
     * {
     *     "query": {
     *         "term": {
     *             "name": "spring"
     *         }
     *     }
     *     "_source": ["name", "studymodel"]
     * }
     * @throws IOException
     */
    @Test
    public void testTermQuery() throws IOException{
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        // 构建查询的条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(
                //QueryBuilders.termQuery("name","spring")
                QueryBuilders.termQuery("id","4028e58161bcf7f40161bcf8b77c0000")
        );
        // source 源字段过滤
        searchSourceBuilder.fetchSource(
            new String[]{"name","studymodel"},
            new String[]{}
        );
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest);
        // 查询结果解析
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        Stream.of(searchHits).forEach(e -> {
            // 转化为字符串
            String sourceAsString = e.getSourceAsString();
            System.out.println(sourceAsString);
        });
    }

    /**
     * ES 提供根据多个 Id 进行匹配的方法
     * {
     *     "query": {
     *         "ids": {
     *             "type": "doc",
     *             "values": ["1234", "3" , "100"]
     *         }
     *     }
     * }
     */
    @Test
    public void testQueryByIds() throws IOException {
        SearchRequest request = new SearchRequest("xc_course");
        request.types("doc");
        String[] ids = new String[]{"1235","100"};
        List<String> idList = Arrays.asList(ids);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(
                // 注意这里的termsQuery() 与上面的 termQuery() 不同
                QueryBuilders.termsQuery("_id",idList)
        );
        request.source(searchSourceBuilder);
        SearchResponse response = client.search(request);
        // 查询结果解析
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        Stream.of(searchHits).forEach(e -> {
            // 转化为字符串
            String sourceAsString = e.getSourceAsString();
            System.out.println(sourceAsString);
        });
    }

    /**
     * March Query即全文检索，它的搜索方式是先将搜索的字符串进行分词，
     * 再使用各个词条在索引中进行搜索。与termQuery()的区别是，MatchQuery() 先对搜索的字符串
     * 进行分词，然后将分词之后的各个词语在索引中搜索。
     * {
     *      "query": {
     *          ”match": {
     *              "description": {
     *                  "query": "spring开发",
     *                  "operator": "or"
     *              }
     *          }
     *      }
     * }
     * - query 搜索的字符串，对于英文的多个单词则中间需要使用半角逗号进行分隔，对于中文中间可以
     *   使用逗号分隔也可以不用
     * - operator : or 表示只要有一个词在文档中出现就符合查询条件，and 表示所有的词都出现才
     *   符合条件
     * ----------------------------------------------------------------
     * 执行过程:
     *  1、 将 spring开发分词为 spring, 开发两个词，
     *  2、再使用这两个词再索引中搜索
     *  3、由于设置了 operator 为or，只要文档中包含其中一个词语就返回该文档。
     *  4、minimum_should_match: 80%可以设置文档关键词匹配比例, 按照分词的比例进行删选文档
     *      假设搜索的字符串分词之后是 7 个，哪个需要匹配 7 * 80% = 5.6 向上取整后表示至少要
     *      有5个词与文档匹配才会返回。
     *
     */
    @Test
    public void testMatchQuery() throws IOException {
        SearchRequest request = new SearchRequest("xc_course");
        request.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // sourse 源字段过滤、
        searchSourceBuilder.fetchSource(
                new String[]{"name","description"},
                new String[]{}
        );
        // 匹配关键字
        searchSourceBuilder.query(
                QueryBuilders.matchQuery(
                        "description",
                        "spring开发框架"
                // 设置匹配规则,只有两个词的时候是适用的
                //).operator(Operator.OR)
                // 设置匹配的百分比
                ).minimumShouldMatch("80%")
        );
        request.source(searchSourceBuilder);
        SearchResponse response = client.search(request);
        // 查询结果解析
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        Stream.of(searchHits).forEach(e -> {
            // 转化为字符串
            String sourceAsString = e.getSourceAsString();
            System.out.println(sourceAsString);
        });
    }

    /**
     * 适用 MultiQuery 匹配多个 Field, 一次可以匹配多个字段
     * 单项匹配是与一个 Field 匹配，多项匹配是拿搜索的字符串与多个Field 进行匹配。
     * Post http://localhost:9200/xc_course/doc/_search
     * - 拿数据与 name 和 description 匹配
     * {
     *     "query":{
     *         "multi_match": {
     *             "query": "spring css",
     *             "minimum_should_match":"50%",
     *             "field": ["name^10","description"]
     *         }
     *     }
     * }
     * - 匹配多个字段字段的时候，boost 可以提升某个字段的权重来提高这个字段出现的文本的排名优先级
     * - 上例中 name 权重提升 10 倍, 修改这个值的大小，可以控制文档中这个字段出现的优先级。
     */
    @Test
    public void multiQuery() throws IOException {
        SearchRequest request = new SearchRequest("xc_course");
        request.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(
            QueryBuilders
                // 需要进行匹配的内容
                .multiMatchQuery("spring框架", "name","description")
                // 匹配的最小指标
                .minimumShouldMatch("50%")
                // 匹配的字段的权重
                .field("name",10)
        );
        request.source(searchSourceBuilder);
        SearchResponse response = client.search(request);
        // 查询结果解析
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        Stream.of(searchHits).forEach(e -> {
            // 转化为字符串
            String sourceAsString = e.getSourceAsString();
            System.out.println(sourceAsString);
        });
    }

    /**
     * 布尔查询: 对应 Lucene 中的 BooleanQuery, 意在将多个查询组合起来。
     * -------------------------------------------------------------
     * 此查询共有三个参数：
     * must     : 文档中必须匹配must中匹配的条件，相当于 "AND"
     * should   : 文档应该匹配should 中的一个或多个, 相当于 "OR"
     * must_not : 文档不应包含 must_not 包含的查询条件， 相当于 "NOT"
     * -------------------------------------------------------------
     * POST http://localhost:9200/xc_course/doc/_search
     * {
     *     "_source" : [ "name", "studymodel", "description"],
     *     "from" : 0,
     *     "size": 1,
     *     "query":{
     *         "bool":{
     *             "must":[
     *                 "multi_match": {
     *                     "query": "spring框架",
     *                     "minimum_should_match": 50%,
     *                     "field": [ "name^10", "description"]
     *                 },
     *                 {
     *                      "term": {
     *                         "studymodel": "201001
     *                      }
     *                 }
     *             ]
     *         }
     *     }
     * }
     * - 分别使用 must, should, must_not 对上述报文进行测试，查看结果
     */
    @Test
    public void testBoolQuery() throws IOException {
       SearchRequest request = new SearchRequest("xc_course");
       request.types("doc");
       // 构建查询的条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(
                new String[]{"name","pic","studymodel"},
                new String[]{}
        );
        searchSourceBuilder.query(
            QueryBuilders.boolQuery()
                .must(
                    QueryBuilders.multiMatchQuery(
                            "spring框架",
                            "name","description"
                    ).minimumShouldMatch("50%").field("name",10)
                ).must(
                    QueryBuilders.termQuery(
                            "studymodel","201001"
                    )
                )
        );
        request.source(searchSourceBuilder);
        SearchResponse response = client.search(request);
        SearchHits searchHits = response.getHits();
        SearchHit[] hits = searchHits.getHits();
        Stream.of(hits).forEach(e -> {
            String sourceAsString = e.getSourceAsString();
            System.out.println(sourceAsString);
        });
    }

    /**
     * Filter 过滤器
     * -------------------------------------------------------------------------------
     * 过滤是针对搜索的结果进行过滤，过滤器主要判断的是文档是否匹配，不去计算和判断文档的匹配度得分
     * 所以过滤器性能比查询要高得多，且方便缓存，推荐尽量使用过滤器去实现查询或者过滤器和查询共同使用。
     * {
     *     "_source": ["name","studymodel","description","price"],
     *     "query": {
     *         "bool": {
     *             "must": [{
     *                  "multi_match" : {
     *                      "query": "spring框架",
     *                      "minimum_should_match": "50%",
     *                      "fields": ["name^10","description"]
     *                  }
     *             }],
     *             "filter": [
     *                  { "term": {"studymodel": "201001" }},
     *                  { "range": { "price": { "gte": 60, "lte": 100 }}}
     *             ]
     *         }
     *     }
     * }
     * - term 项匹配过滤，保留 studymodel 等于 "201001" 的记录
     * - range 和 term 一次只能对一个 Field 设置范围过滤
     */
    @Test
    public void testFilter() throws IOException {
        SearchRequest request = new SearchRequest("xc_course");
        request.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 源字段过滤
        searchSourceBuilder.fetchSource(
                new String[]{"name", "studymodel","price","description"},
                new String[]{}
        );
        searchSourceBuilder.query(
            // 布尔查询, 对查询结果进行过滤
            QueryBuilders.boolQuery()
                .must(
                    QueryBuilders.multiMatchQuery(
                            "spring框架",
                            "name","description"
                    ).minimumShouldMatch("50%").field("name",10)
                ).filter(
                    QueryBuilders.termQuery("studymodel","201001")
                ).filter(
                    QueryBuilders.rangeQuery("price").gte(60).lte(100)
                )
        );
        request.source(searchSourceBuilder);
        SearchResponse response = client.search(request);
        SearchHits searchHits = response.getHits();
        SearchHit[] hits = searchHits.getHits();
        Stream.of(hits).forEach(e -> {
            String sourceAsString = e.getSourceAsString();
            System.out.println(sourceAsString);
        });
    }

    /**
     * 对查询的结果进行排序,注意的是，排序是再查询之后，对查询的结果进行排序的。
     * 排序字段可以添加一个或多个，可以添加在keyword, date, float 等类型上
     * text 类型是不允许添加排序的
     * ---------------------------------------------------------------------
     * POST http://localhost:9200/xc_course/doc/_search
     * {
     *      "_source": ["name","price","studymodel","description"]
     *      "query": {
     *          "bool":{
     *              "filter": [
     *                  {"range": {"price": {"gte": 0, "lte": 100 }}}
     *              ]
     *          }
     *      },
     *      "sort": [
     *          {"studymodel": "desc" },
     *          {"price": "asc" }
     *      ]
     * }
     * ---------------------------------------------------------------------
     */
    @Test
    public void testSortResult() throws IOException {
        SearchRequest request = new SearchRequest("xc_course");
        request.types("doc");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(
                new String[]{"name","description","price","studymodel"},
                new String[]{}
        );
        searchSourceBuilder.query(
            QueryBuilders.boolQuery().filter(
                    QueryBuilders.rangeQuery("price").gte(0).lte(100)
            )
        // 对查询的结果进行排序, 越靠前的排序优先级越高,后续的排序都是在前一个排序的基础之上
        ).sort(
                // 构建排序的字段对象并设置排序的规则
                new FieldSortBuilder("price").order(SortOrder.ASC)
        ).sort(
                // 构建排序的字段对象并设置排序的规则
                new FieldSortBuilder("studymodel").order(SortOrder.DESC)
                // 对查询的结果进行排序, 对第二个字段进行排序
        );
        request.source(searchSourceBuilder);
        SearchResponse response = client.search(request);
        SearchHits searchHits = response.getHits();
        SearchHit[] hits = searchHits.getHits();
        Stream.of(hits).forEach(e -> {
            String sourceAsString = e.getSourceAsString();
            System.out.println(sourceAsString);
        });
    }

    /**
     * 测试高亮 ： 搜索结果高亮 => 针对关键字搜索高亮显示可用
     * --------------------------------------------------------------------------
     * POST http://localhost:9200/xc_course/doc/_search
     * {
     *      "_source": [ "name", "description"],
     *      "query": {
     *          "bool": {
     *              "must": [{
     *                 "multi_match" : {
     *                     "query" : "spring框架",
     *                     "minimum_should_match": "50%",
     *                     "fields" : ["name","description"],
     *                     "type" : "best_fields"
     *                 }
     *              }],
     *              "filter": [
     *                  {"range" : {"price" : {"gte": 0, "lte": 100}}}
     *              ]
     *          }
     *      },
     *      "sort": [
     *          {"price":"asc"}
     *      ],
     *      "highlight" : {
     *          "pre_tags": ["<<"],
     *          "post_tags": [">>"],
     *          "fields": {
     *               "name" : {},
     *               "description": {}
     *          }
     *      }
     * }
     */
    @Test
    public void testHighlight() throws IOException {
        SearchRequest request = new SearchRequest("xc_course");
        request.types("doc");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.fetchSource(
                new String[]{"name","description","price"},
                new String[]{}
        );
        HighlightBuilder highlightBuilder = new HighlightBuilder().preTags("<<").postTags(">>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        highlightBuilder.fields().add(new HighlightBuilder.Field("description"));
        searchSourceBuilder.query(
            QueryBuilders.boolQuery()
                .must(
                    QueryBuilders.multiMatchQuery(
                            "spring框架",
                            "name","description"
                    ).minimumShouldMatch("50%").field("name",4)
                ).filter(
                        QueryBuilders.rangeQuery("price").gte(0).lte(100)
                )
        ).sort(
                new FieldSortBuilder("price").order(SortOrder.ASC)
        ).highlighter(highlightBuilder);
        request.source(searchSourceBuilder);
        SearchResponse response = client.search(request);
        SearchHits searchHits =  response.getHits();
        SearchHit[] hits = searchHits.getHits();
        Stream.of(hits).forEach(e -> {
            String sourceAsString = e.getSourceAsString();
            System.out.println(sourceAsString);
            Map<String, Object> sourceAsMap = e.getSourceAsMap();
            Object name = sourceAsMap.get("name");
            Map<String, HighlightField> highlightFields = e.getHighlightFields();
            if(null != highlightFields){
                HighlightField hname = highlightFields.get("name");
                if(null != hname){
                    // 由于得到的内容是以高亮字段截取的文本数组, 需要将这些文本拼接之后返回
                    Text[] fragments = hname.getFragments();
                    StringBuilder builder = new StringBuilder();
                    Stream.of(fragments).forEach(builder::append);
                    name = builder.toString();
                }
            }
            System.out.println(name);
        });
    }
}
