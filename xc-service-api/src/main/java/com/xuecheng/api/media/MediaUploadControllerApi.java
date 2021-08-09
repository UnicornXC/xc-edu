package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

/**
 * 媒体资源的管理服务类
 */
@Api(value = "媒体资源管理", description = "媒体资源管理接口服务",tags = {"媒体资源的管理接口服务V01"})
public interface MediaUploadControllerApi {

    @ApiOperation("文件在服务器中进行注册")
    ResponseResult register(
            String fileMd5,
            String fileName,
            Long fileSize,
            String mimetype,
            String fileExt
    );

    @ApiOperation("校验分块文件是否存在")
    CheckChunkResult checkChunk(
            String fileMd5,
            Integer chunk,
            Integer chunkSize
    );

    @ApiOperation("上传分块文件")
    ResponseResult uploadChunk(
            MultipartFile file,
            String fileMd5,
            Integer chunk
    );

    @ApiOperation("合并文件分块")
    ResponseResult mergechunk(
            String fileMd5,
            String fileName,
            Long fileSize,
            String mimetype,
            String fileExt
    );
}
