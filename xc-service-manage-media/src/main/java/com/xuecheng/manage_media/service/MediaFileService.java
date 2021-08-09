package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MediaFileService {

    @Autowired
    private MediaFileRepository mediaFileRepository;


    /**
     * 分页查询用户媒资数据
     * @param page
     * @param size
     * @param queryMediaFileRequest
     * @return
     */
    public QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {

        // 构建查询条件
        MediaFile mediaFile = new MediaFile();

        if (queryMediaFileRequest == null) {
            queryMediaFileRequest = new QueryMediaFileRequest();
        }
        // 查询条件匹配器
        ExampleMatcher matcher = ExampleMatcher.matching()
                // tag 字段模糊匹配
                .withMatcher(
                    "tag",
                    ExampleMatcher.GenericPropertyMatchers
                            .contains()
                // 文件原始名称模糊匹配
                ).withMatcher(
                    "fileOriginalName",
                    ExampleMatcher.GenericPropertyMatchers
                            .contains()
                // 处理状态精确匹配(默认，可以不设置)
                ).withMatcher(
                    "processStatus",
                    ExampleMatcher.GenericPropertyMatchers
                            .exact()
                );
        // 查询条件对象
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getTag())) {
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())) {
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())) {
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }
        // 定义 example 示例
        Example<MediaFile> ex = Example.of(mediaFile, matcher);
        //----------------------
        // 分页参数
        page = page - 1;
        Pageable pageable = new PageRequest(page, size);
        // 分页查询
        Page<MediaFile> all = mediaFileRepository.findAll(ex, pageable);
        QueryResult<MediaFile> mediaFileQueryResult = new QueryResult<>();
        mediaFileQueryResult.setList(all.getContent());
        mediaFileQueryResult.setTotal(all.getTotalElements());
        return new QueryResponseResult(CommonCode.SUCCESS, mediaFileQueryResult);
    }

}

