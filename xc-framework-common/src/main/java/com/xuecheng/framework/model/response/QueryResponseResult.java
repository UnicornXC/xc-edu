package com.xuecheng.framework.model.response;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class QueryResponseResult<E> extends ResponseResult {

    QueryResult<E> queryResult;

    public QueryResponseResult(ResultCode resultCode,QueryResult<E> queryResult){
        super(resultCode);
        this.queryResult = queryResult;
    }

}
