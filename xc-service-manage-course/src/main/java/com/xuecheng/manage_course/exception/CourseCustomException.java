package com.xuecheng.manage_course.exception;

import com.xuecheng.framework.exception.ExceptionCatch;
import com.xuecheng.framework.model.response.CommonCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;


/**
 *  在各自服务的项目中自己定义自己的异常
 * ---------------------------------------------------------------------------
 *  继承公共类中统一异常处理类, 然后在内部自定义异常对应的错误代码,
 *  ---------
 *  这样的话，统一异常处理类中会根据个这个扩展的异常类型错误代码进行返回
 *  --------------------------------------------------------------------------
 */

@Slf4j
@ControllerAdvice
public class CourseCustomException extends ExceptionCatch {


    static{
        // 由于父类中的定义的构建异常的 ImmutableMap 的 Builder 中是 protected 的;
        // 子类中可以直接使用
        builder.put(AccessDeniedException.class, CommonCode.UNAUTHORISE);
    }


}
