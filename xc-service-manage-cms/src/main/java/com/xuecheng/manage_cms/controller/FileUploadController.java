package com.xuecheng.manage_cms.controller;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.filesystem.response.UploadMongoFileResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.GridFileService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/cms/file")
public class FileUploadController {

    @Autowired
    private GridFileService gridFileService;

    @PostMapping("saveFileMetadata")
    public UploadMongoFileResult upload(MultipartFile file){

        log.info(file.getOriginalFilename());
        try {
            ObjectId store = gridFileService.storeFile(file.getInputStream(), file.getOriginalFilename());
            return new UploadMongoFileResult(CommonCode.SUCCESS, store.toString());
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult delete(@PathVariable String id) {
        GridFSFile file = gridFileService.findFileById(id);
        if (file.getObjectId() == null) {
            return ResponseResult.SUCCESS();
        }
        gridFileService.deleteById(id);
        return ResponseResult.SUCCESS();
    }
}
