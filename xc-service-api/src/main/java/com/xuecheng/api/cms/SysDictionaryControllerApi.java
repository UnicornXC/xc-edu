package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.system.SysDictionary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value= "数据字典接口",description = "提供数据字典接口的管理与查询")
public interface SysDictionaryControllerApi {

    @ApiOperation(value="根据类型查询数据字典的接口")
    public SysDictionary getByType(String type);


}
