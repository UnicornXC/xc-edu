package com.xuecheng.manage_course.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableResourceServer
// 用于激活方法上的@PreAuthorize 注解,用于控制方法的权限问题
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    // 公钥
    private static final String PUBLIC_KEY = "public.key";

    // 定义JwtTokenStore, 使用 jwt 令牌
    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter){
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    // 定义JwtAccessTokenConverter 使用jwt令牌
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setVerifierKey(getPubKey());
        return converter;
    }

    /**
     * 获取非对称加密的公钥 key
     */
    private String getPubKey(){
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        try{
            InputStreamReader inputStreamReader
                    = new InputStreamReader(resource.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            log.error("--------获取公共密钥失败------");
            return null;
        }
    }

    //Http安全配置, 对每个到达系统的http请求连接进行校验
    @Override
    public void configure(HttpSecurity http) throws Exception {
        // 所有请求必须先经过认证
        http.authorizeRequests()
            // 下边的路径放行
            .antMatchers(
                    "/v2/api-docs",
                    "/swagger-resources",
                    "/swagger-resources/configuration/ui",
                    "/swagger-resources/configuration/security",
                    "/swagger-ui.html",
                    "/webjars/**",
                    "/course/courseview/**"  // 获取课程的页面动态数据，加载预览页面与生成静态页面时使用，无需校验
            ).permitAll()
            .anyRequest().authenticated();
    }
}
