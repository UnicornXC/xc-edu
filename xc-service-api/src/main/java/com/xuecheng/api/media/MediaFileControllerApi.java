package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 用于管理用户媒资文件的接口类
 */
@Api(value = "媒资文件管理",description = "媒体文件管理接口", tags = {"媒体文件管理接口"})
public interface MediaFileControllerApi {

    @ApiOperation("查询文件列表")
    QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest);

    @ApiOperation("客户端手动发起视频的处理请求")
    ResponseResult videoProcess(String mediaId);

}

