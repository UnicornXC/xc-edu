
## 课程发布的正式索引

- Post `http://127.0.0.1:9200/xc_course/doc/_mapping`

```json
 {
  "properties": {
    "description": {
      "analyzer": "ik_max_word",
      "search_analyzer": "ik_smart",
      "type": "text"
    },
    "grade": {
      "type": "keyword"
    },
    "id": {
      "type": "keyword"
    },
    "mt": {
      "type": "keyword"
    },
    "name": {
      "analyzer": "ik_max_word",
      "search_analyzer": "ik_smart",
      "type": "text"
    },
    "users": {
      "index": false,
      "type": "text"
    },
    "charge": {
      "type": "keyword"
    },
    "valid": {
      "type": "keyword"
    },
    "pic": {
      "index": false,
      "type": "keyword"
    },
    "qq": {
      "index": false,
      "type": "keyword"
    },
    "price": {
      "type": "float"
    },
    "price_old": {
      "type": "float"
    },
    "st": {
      "type": "keyword"
    },
    "status": {
      "type": "keyword"
    },
    "studymodel": {
      "type": "keyword"
    },
    "teachmode": {
      "type": "keyword"
    },
    "teachplan": {
      "analyzer": "ik_max_word",
      "search_analyzer": "ik_smart",
      "type": "text"
    },
    "expires": {
      "type": "date",
      "format": "yyyy-MM-dd HH:mm:ss"
    },
    "pub_time": {
      "type": "date",
      "format": "yyyy-MM-dd HH:mm:ss"
    },
    "start_time": {
      "type": "date",
      "format": "yyyy-MM-dd HH:mm:ss"
    },
    "end_time": {
      "type": "date",
      "format": "yyyy-MM-dd HH:mm:ss"
    }
  }
} 
```