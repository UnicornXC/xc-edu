package com.xuecheng.framework.domain.course.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AddCoursePicResult extends ResponseResult {
    public AddCoursePicResult(ResultCode code, String pic) {
        super(code);
        this.pic = pic;
    }

    private String pic;
}
