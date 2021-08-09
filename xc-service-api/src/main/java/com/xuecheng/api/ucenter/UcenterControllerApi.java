package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "用户中心", description = "用户中心管理", tags = {"用户中心管理"})
public interface UcenterControllerApi {

    @ApiOperation("根据用户查询用户的信息与用户的扩展信息")
    XcUserExt getUserExt(String username);

}
