package com.xuecheng.framework.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * feign 拦截器，解决微服务之间相互调用时无法认证的问题，通过feign拦截器拦截请求，将当前请求的请求头添加为
 * feign 远程调用时的请求头，这样就将服务调用的jwt令牌传递到远程调用的请求中。
 * -------------------------------------------------------------------------------------
 */

public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = requestAttributes.getRequest();
        // 取出所有的请求头
        Enumeration<String> headerNames = request.getHeaderNames();
        // 将请求头封装后向下传递
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                // 将请求头向下传递
                template.header(headerName, headerValue);
            }
        }
    }
}
