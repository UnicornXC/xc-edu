package com.xuecheng.api.auth;

import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 用户认证中心服务接口
 *
 */
@Api(value = "用户认证", description = "用户认证服务接口", tags = {"用户认证服务接口"})
public interface AuthControllerApi {


    @ApiOperation("用户登录")
    public LoginResult login(LoginRequest loginRequest);


    @ApiOperation("退出登录")
    public ResponseResult logout();


    @ApiOperation("获取用户存储在redis中的令牌")
    public JwtResult getJwt();

}
