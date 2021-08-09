package com.xuecheng.manage_media.controller;

import com.xuecheng.api.media.MediaFileControllerApi;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.service.MediaFileService;
import com.xuecheng.manage_media.service.MediaUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/media/file")
public class MediaFileController implements MediaFileControllerApi {

    @Autowired
    private MediaFileService mediaFileService;

    @Autowired
    private MediaUploadService mediaUploadService;

    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult<MediaFile> findList(
            @PathVariable("page") int page,
            @PathVariable("size") int size,
            QueryMediaFileRequest queryMediaFileRequest
    ) {
        return mediaFileService.findList(page, size, queryMediaFileRequest);
    }

    @Override
    @PostMapping("/process")
    public ResponseResult videoProcess(@PathVariable("mediaId")String mediaId){
        return mediaUploadService.sendProcessVideoMsg(mediaId);
    }
}
