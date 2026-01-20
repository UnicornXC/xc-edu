package com.xuecheng.framework.domain.course.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class QueryCoursePicResult extends ResponseResult {

    public QueryCoursePicResult(ResultCode resultCode, String pic) {
        super(resultCode);
        this.pic = pic;
    }

    private String pic;
}
