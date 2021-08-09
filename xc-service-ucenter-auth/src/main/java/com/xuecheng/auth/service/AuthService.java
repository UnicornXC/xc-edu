package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {

    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    // 用户认证的方法
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        // 申请令牌
        AuthToken authToken = applyToken(username, password, clientId, clientSecret);
        if (null == authToken) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_ERROR);
        }
        // 将token 存储到 redis 中
        String access_token = authToken.getAccess_token();
        String content = JSON.toJSONString(authToken);
        boolean saveTokenResult = saveToken(access_token, content, tokenValiditySeconds);
        if (!saveTokenResult) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVE_FAIL);
        }
        return authToken;
    }

    /* 保存 token 到 redis */
    private boolean saveToken(String access_token, String content, int ttl) {
        // 令牌名称
        String name = "user_token:" + access_token;
        // 保存数据到redis
        redisTemplate.boundValueOps(name).set(content, ttl, TimeUnit.SECONDS);
        // 获取过期时间
        Long expire = redisTemplate.getExpire(name);
        return expire > 0;
    }

    /* 获取token */
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {

        // 选中认证服务的地址
        ServiceInstance serviceInstance
                = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        if (null == serviceInstance) {
            log.error("choose an auth instance fail");
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_SERVER_NOT_FOUND);
        }
        // 获取令牌的url
        String applyUrl = serviceInstance.getUri().toString() + "/auth/oauth/token";
        // 定义请求token 的 body
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("grant_type", "password");
        formData.add("username", username);
        formData.add("password", password);
        // 定义请求头
        MultiValueMap<String,String> header = new LinkedMultiValueMap<>();
        header.add("Authorization", httpBasic(clientId, clientSecret));
        // 指定 restTemplate 遇到 400 401 响应的时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
                if (401 != response.getRawStatusCode() && 400 != response.getRawStatusCode()) {
                    super.handleError(url, method, response);
                }
            }
        });
        Map map = null;
        try {
            // http 请求 spring security 申请令牌接口
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    applyUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(formData, header),
                    Map.class
            );
            map = responseEntity.getBody();
        } catch (RestClientException e) {
            e.printStackTrace();
            log.error("request oauth_token_password error: {}", e.getMessage());
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLY_TOKEN_FAIL);
        }
        if (null == map
            || map.get("access_token") == null
            || map.get("refresh_token") == null
            || map.get("jti") == null
        ){
            // 解析 spring security 返回的错误信息，将错误信息解析一下，
            // 直接返回申请token 失败，面向用户不友好
            if (map != null && map.get("error_description") != null) {
                // 包含了密码错误的信息,拿到 spring security 返回的信息之后返回密码错误
                String errorMdg = map.get("error_description").toString();
                if (StringUtils.contains(errorMdg,"UserDetailsService returned null")) {
                   ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOT_EXISTS);
                }else if(StringUtils.contains(errorMdg,"坏的凭证")){
                    // 账号或密码错误
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }
            }
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLY_TOKEN_FAIL);
        }
        AuthToken authToken = new AuthToken();
        // 访问令牌 (jwt)
        String jwt_token = map.get("access_token").toString();
        // 刷新令牌 (jwt)
        String refresh_token = map.get("refresh_token").toString();
        // jti 作为用户的身份标识
        String access_token = map.get("jti").toString();
        authToken.setAccess_token(access_token);
        authToken.setJwt_token(jwt_token);
        authToken.setRefresh_token(refresh_token);
        return authToken;
    }

    private String httpBasic(String clientId, String clientSecret) {
        // 将客户端 id 和客户端密码拼接，按 " 客户端Id:客户端密码 "
        String node = clientId + ":" + clientSecret;
        // 进行 base64 编码
        byte[] encode = Base64.encode(node.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encode);
    }

    public AuthToken getTokenFromRedis(String jti) {
        String jtiKey = "user_token:" + jti;
        String value = redisTemplate.opsForValue().get(jtiKey);
        AuthToken authToken = null;
        try {
            authToken = JSON.parseObject(value, AuthToken.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return authToken;
    }

    public boolean delTokenFromRedis(String access_token) {
        // 令牌名称
        String name = "user_token:" + access_token;
        // 保存数据到redis
        redisTemplate.delete(name);
        // 获取过期时间
        Long expire = redisTemplate.getExpire(name);
        return expire < 0;
    }
}
