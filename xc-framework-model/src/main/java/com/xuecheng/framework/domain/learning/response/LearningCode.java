package com.xuecheng.framework.domain.learning.response;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.ResultCode;
import io.swagger.annotations.ApiModelProperty;

public enum LearningCode implements ResultCode {

    LEARNING_GET_MEDIA_ERROR(false,23001, "获取学习地址失败"),
    CHOOSE_COURSE_USER_ISNULL(false,23002,"选课用户信息为空"),
    CHOOSE_COURSE_XCTASK_ISNULL(false, 23003, "选课信息无效");

    //操作代码
    @ApiModelProperty(value = "操作是否成功", example = "true", required = true)
    boolean success;

    //操作代码
    @ApiModelProperty(value = "操作代码", example = "22001", required = true)
    int code;
    //提示信息
    @ApiModelProperty(value = "操作提示", example = "操作过于频繁！", required = true)
    String message;
    private LearningCode(boolean success, int code, String message){
        this.success = success;
        this.code = code;
        this.message = message;
    }
    private static final ImmutableMap<Integer, LearningCode> CACHE;

    static {
        final ImmutableMap.Builder<Integer, LearningCode> builder = ImmutableMap.builder();
        for (LearningCode commonCode : values()) {
            builder.put(commonCode.code(), commonCode);
        }
        CACHE = builder.build();
    }

    @Override
    public boolean success() {
        return false;
    }

    @Override
    public int code() {
        return 0;
    }

    @Override
    public String message() {
        return null;
    }
}
