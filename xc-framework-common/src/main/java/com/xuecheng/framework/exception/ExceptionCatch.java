package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一异常捕获类
 */
@ControllerAdvice
public class ExceptionCatch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCast.class);

    //定义map,配置异常信息所对应的错误代码
    private static ImmutableMap<Class<? extends Throwable>, ResultCode> EXCEPTIONS;
    //定义构建异常ImmutableMap的builder
    protected static ImmutableMap.Builder<Class<? extends Throwable>, ResultCode> builder =
            ImmutableMap.builder();

    //将不可预知的异常的可能结果放入map中
    static {
        builder.put(HttpMessageNotReadableException.class, CommonCode.INVALID_PARAM);
    }


    @ExceptionHandler(CustomException.class)
    @ResponseBody  //将返回的错误信息转换为json
    public ResponseResult customException(CustomException e) {

        LOGGER.error("catch Exception{}", e.getMessage());
        ResultCode resultCode = e.getResultCode();
        return new ResponseResult(resultCode);

    }

    @ExceptionHandler(Exception.class)
    public ResponseResult exception(Exception e) {
        LOGGER.error("catch Exception{}", e.getMessage());
        if (EXCEPTIONS == null) {
            EXCEPTIONS = builder.build();
        }
        //查找map中是否有已经保存的异常对应的错误代码，找到则返回错误代码，否则返回99999异常
        ResultCode resultCode = EXCEPTIONS.get(e.getClass());
        if (resultCode != null) {
            return new ResponseResult(resultCode);
        } else {
            return new ResponseResult(CommonCode.SERVER_ERROR);
        }
    }
}
