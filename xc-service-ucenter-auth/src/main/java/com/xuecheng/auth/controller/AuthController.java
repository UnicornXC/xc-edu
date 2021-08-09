package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@Slf4j
@RestController
public class AuthController implements AuthControllerApi {


    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;

    @Value("${auth.tokenValiditySeconds}")
    private int tokenValiditySeconds;

    @Autowired
    private AuthService authService;


    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        // 校验账号是否输入
        if (loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername())) {
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }
        // 校验密码是否输入
        if (StringUtils.isEmpty(loginRequest.getPassword())) {
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }
        //
        AuthToken authToken = authService.login(
                loginRequest.getUsername(),
                loginRequest.getPassword(),
                clientId,
                clientSecret
        );
        // 访问 token
        String access_token = authToken.getAccess_token();
        // 访问令牌存储到cookie
        saveCookie(access_token);
        return new LoginResult(CommonCode.SUCCESS, access_token);
    }
    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        // 将 redis 中存储的token信息清除
        String access_token = getJtiTokenFromCookie();
        boolean result = authService.delTokenFromRedis(access_token);
        // 将存储的cookie 信息清除
        clearCookie();
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Override
    @GetMapping("/userjwt")
    public JwtResult getJwt() {
        // 取出cookie 中存储的身份令牌
        String jti =  getJtiTokenFromCookie();
        if (jti == null) {
            return new JwtResult(CommonCode.FAIL,null);
        }
        // 从redis 查询令牌
        AuthToken authToken =  authService.getTokenFromRedis(jti);

        // 将令牌放回
        if (authToken != null) {
            String jwt_token = authToken.getJwt_token();
            return new JwtResult(CommonCode.SUCCESS, jwt_token);
        }
        return null;
    }

    private String getJtiTokenFromCookie() {
        HttpServletRequest request =
                ((ServletRequestAttributes)RequestContextHolder
                        .getRequestAttributes()).getRequest();
        Map<String, String> map = CookieUtil.readCookie(request,"uid");
        if (map != null && map.get("uid") != null) {
            return map.get("uid");
        }
        return null;
    }

    private void saveCookie(String token) {
        HttpServletResponse response = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getResponse();
        assert response != null;
        // 添加 cookie 认证令牌，最后一个参数设置为false, 表示允许浏览器获取
        CookieUtil.addCookie(
                response,
                cookieDomain,
                "/",
                "uid",
                token,
                cookieMaxAge,
                false
        );
    }

    private void clearCookie() {
        HttpServletResponse response
                = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(
                response,
                cookieDomain,
                "/",
                "uid",
                "",
                0,
                false
        );
    }
}
