package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import com.sun.jersey.core.util.Base64;
import com.xuecheng.framework.client.XcServiceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestTokenClient {


    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    RestTemplate restTemplate;

    /**
     * 为了不破坏 Spring Security 代码，我们通过 restTemplate 请求 spring security 所暴露 的申请 令牌的接口
     * 来申请令牌,在这里测试一下。
     * ------------------------------------------------------------------------------------------
     */
    @Test
    public void testTokenClient() {

        // 采用客户端负载均衡，从 eureka 获取认证服务的 ip 和 端口
        ServiceInstance serviceInstance = loadBalancerClient
                .choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);

        // 获取服务的请求地址
        URI uri = serviceInstance.getUri();
        String authUrl = uri + "/auth/oauth/token";

        System.out.println(authUrl);

        /** 申请服务的参数
         *  ----------------------------------------------------------------------------------
         * URI url, HttpMethod method, HttpEntity<?> httpEntity, Class<T> responseType
         *  - url          就是 申请令牌的 url
         *  - method       发起请求的方法
         *  - httpEntity   请求的内容
         *  - responseType 将响应结果生成的类型
         */
        //请求的内容分为两个部分
        // 1、header 信息，包含了http basic 认证信息
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String httpBasic = httpbasic("XcWebApp", "XcWebApp");
        //"Basic WGNXZWJBcHA6WGNXZWJBcHA="
        headers.add("Authorization", httpBasic);
        // 2、包含 grant_type, username, password
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        // 添加用户信息
        body.add("grant_type", "password");
        body.add("username", "itcast");
        body.add("password", "111111");
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        // 指定 restTemplate 当遇到 400 或者 401 的时候也不要抛出异常，正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });
        // 远程调用申请令牌
        ResponseEntity<Map> exchange = restTemplate.exchange(
                authUrl,
                HttpMethod.POST,
                entity,
                Map.class
        );
        Map resultBody = exchange.getBody();
        System.out.println(JSON.toJSONString(resultBody));
    }

    // 获取 http Basic 信息
    private String httpbasic(String clinetId, String clietnSecret) {
        // 将客户端 id 与客户端密码拼接，按 "客户端Id : 客户端密码" 格式
        String res = clinetId + ":" + clietnSecret;
        // 进行base64编码
        byte[] encode = Base64.encode(res.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encode);
    }
}
