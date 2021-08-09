package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthFilterService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 从头部取出 jwt 令牌
    public String getJwtFromHeader(HttpServletRequest request) {
        // 取出头信息
        String authorization = request.getHeader("Authorization");
        // 验证 Authorization
        if (StringUtils.isEmpty(authorization)) {
            return null;
        }
        // 校验开头
        if (!authorization.startsWith("Bearer ")) {
            return null;
        }
        // 取出真正的 jwt 返回
        return authorization.substring(8);
    }

    // 从 cookie 中取出令牌
    public String getTokenFromCookie(HttpServletRequest request) {
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String jti_token = cookieMap.get("uid");
        if (StringUtils.isEmpty(jti_token)) {
            return null;
        }
        return jti_token;
    }

    // 在 redis 中查看令牌的有效期
    public long getExpire(String jti_token) {
        String name = "user_token:" + jti_token;
        return redisTemplate.getExpire(name, TimeUnit.SECONDS);
    }
}
