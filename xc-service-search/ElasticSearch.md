### 索引
- 索引： 所以库类似于关系型数据库中的表，相同类型的文档存储在一个索引库中，而其中的document 
  类似表中的一条记录、
  
```shell
    Put http://localhost:9200/xc_course(索引库名称)
```

```json
{
  "settings": {
   "index" : {
     "number_of_shards" : 1,
     "number_of_replicas" : 0
   } 
  }
}
```

> 注意事项：

- 索引库名称在发送请求的路径中写入
- number_of_shards 指定集群中索引存储的分片数量
- number_of_shards 指定集群中数据备份的数量


### 映射
映射：在索引中的 document 都包含了多个field,创建映射的过程就是创建field 过程。
- 创建映射的时候应该使用 ik_max_word模式，
- 映射创建成功之后可以添加新字段，但是不允许修改已有字段， 
- 映射的删除通过删除索引来删除
- 数据类型：
    - String 
        * text(索引时候会进行分词)  
        * keyword(索引时不进行分词，例如：电话号码，邮箱等) 
    - Numeric Datatype
        * long, integer, short, byte, double, float, half-float, scaled-float
    - Date Datatype
        * date
    - Boolean Datatype
        * boolean
    - Binary Datatype
        * binary
    - Range Datatype
        * integer_range, float_range, long_range, double_range, date_range
    
```shell
    Post http://localhost:9200/xc_couse(索引库名)/doc(类型名)/_mapping
```

```json
{
  "properties": {
    "name": {
      "type": "text",
      "analyzer": "ik_max_word",
      "search_analyzer": "ik_smart"
    },
    "description": {
      "type": "text",
      "analyzer": "ik_max_word"
    },
    "studymodel": {
      "type": "keyword"
    },
    "timestamp" : {
      "type": "date",
      "format": "yyyy-MM-dd HH:mm:ss || yyyy-MM-dd"
    },
    "price":{
      "type": "scaled-float",
      "scaled-factor": 100
    },
    "pic" : {
      "type": "binary",
      "index" : false
    }
  }
}
```

> 注意事项

- 使用 analyzer 和 search_analyzer 来分别指定存储和搜索时的分词器，当不设置搜索时的分析器默认使用的是 
  存储的分词器
- index 属性表示这个字段是否参与索引，只有参与索引的数据在索引库中才能搜索到。
- store 属性用于表示是否要在 sourse 之外进行存储，一般默认情况下，文档会在_source 中保存一份原始文档，
- 日期类型：
    * format 用于日期类型进行格式化，日期类型一般也不需要进行分词，日期类型多用来排序
- 数值类型：
    * 尽可能使用范围小的数值类型，应用倍率的方式将精度要求不高的浮点数转化为整数进行存储，有利于节省
      磁盘空间。



### 文档 （document）

- 文档插入的时候，可以在请求的路径中指定id,不指定id 会自动生成一个id

```shell
   Post http://localhost:9200/xc_course/doc/482375353895357353(文档id)    
```

```json
{ 
  "name": "Bootstrap开发", 
  "description": "Bootstrap是由Twitter推出的一个前台页面开发框架，是一个非常流行的开发框架，此框架集成了多种页面效果。此开发框架包含了大量的CSS、JS程序代码，可以帮助开发者（尤其是不擅长页面开发的程序人员）轻松的实现一个不受浏览器限制的精美界面效果。",
  "studymodel": "201002", 
  "price":38.6, 
  "timestamp":"2018-04-25 19:11:35",
  "pic":"group1/M00/00/00/wKhlQFs6RCeAY0pHAAJx5ZjNDEM428.jpg"
}

```

### 类型 type 
(ES6之前的版本都有类型的概念，之后将会删除)，我们所说的type 可以类比索引库中的表， 因为在之前的版本构建中，
一个索引库中可以存档不同类型的文档，这样type 就相当于关系数据库中的表， 但是官方建议一个索引库中只存储
相同类型的文档，这样来说，一个索引库才是类比一个数据库表，这样的尴尬设计， 使得官方在ES9 版本之后彻底
移除了type, 现在的type 在使用的时候也只是一个形式，没有实际的作用。

### 分词器

- 分词器：当添加 document 记录的时候，需要将文档内容切分成分词列表，默认的分词器对英文有较好的分词效果，
  对于中文都是单字分词，这样无法达到中文分词的效果，这时候就需要指定分词器（ik_分词器）来切分内容。
  当然也可以通过配置（自定义词库）修改分词的规则。
    
- IK_分词器: 与官方的ElasticSearch 版本保持同步，以插件的形式在 ElasticSearch 中加载，主要有两种
  分词模式：
  - ik_max_word ： 词组最大化 在存储文档的时候使用
  - ik_smart    ： 智能分词，在搜索的时候使用
    
    
## 全文检索的 API 使用

#### 简单搜索
    简单搜索就是通过url 进行查询，以 get 方式请求 ES
```
    get ../_search?q=name:sping    # 查找 name 字段中包含 spring 的文档
```

#### DSL搜索

DSL (Domain Specific Language) 是 ES 提出的基于json 的搜索方式，在搜索的时候传入特定的
json 格式的数据来完成不同的搜索需求。
- DSL 搜索 比 url 方式的搜索功能更加强大，在项目中建议使用 DSL的方式完成搜索。