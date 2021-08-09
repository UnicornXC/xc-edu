package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthFilterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class LoginFilter extends ZuulFilter {
    
    @Autowired
    private AuthFilterService authFilterService;
    

    /**
     * 过滤器的类型
     * 表示过滤器的执行时机
     * -------------------------------------------------------
     * - pre         请求被路由之前执行
     * - routing     路由请求时调用
     * - post        在 routing 和 error 之后被调用
     * - error       处理请求时发生错误时调用
     * -------------------------------------------------------
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 过滤器执行的顺序
     * -------------------------------------------------------
     * 返回一个整形的值，值越小，执行的优先级越高
     * -------------------------------------------------------
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * 用于判断该过滤器是否执行
     * -------------------------------------------------------
     *  - true  过滤器开启，执行
     *  - false 过滤器关闭不执行
     * -------------------------------------------------------
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 过滤器的代码逻辑
     * -------------------------------------------------------
     * - 需要过滤器做的业务逻辑判断
     * -------------------------------------------------------
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        // 过滤器真正执行的逻辑代码
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 得到请求的request
        HttpServletRequest request = requestContext.getRequest();
        // 得到响应的对象
        HttpServletResponse response = requestContext.getResponse();

        // 从 cookie 中获取 jti 令牌
        String jti_token = authFilterService.getTokenFromCookie(request);
        if (StringUtils.isEmpty(jti_token)) {
            // 拒绝访问
            access_denied(requestContext, response);
            return null;
        }
        // 从 header 中获取 access_token
        String access_token = authFilterService.getJwtFromHeader(request);
        if (StringUtils.isEmpty(access_token)) {
            // 拒绝访问
            access_denied(requestContext, response);
            return null;
        }
        // 校验 redis 中的过期时间
        long expire = authFilterService.getExpire(jti_token);
        if (expire < 0) {
            // 拒绝访问
            access_denied(requestContext, response);
            return null;
        }
        return null;
    }

    /**
     * 拒绝访问
     * @param requestContext
     * @param response
     */
    private void access_denied(RequestContext requestContext, HttpServletResponse response) {
        // 拒绝访问
        requestContext.setSendZuulResponse(false);
        // 设置响应的编码，
        requestContext.setResponseStatusCode(200);
        // 设置响应的信息
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        // 转换成JSON
        String jsonString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(jsonString);
        response.setContentType("application/json;charset=UTF-8");
    }
}
