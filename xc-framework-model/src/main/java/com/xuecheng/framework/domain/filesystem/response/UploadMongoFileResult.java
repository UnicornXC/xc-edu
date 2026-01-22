package com.xuecheng.framework.domain.filesystem.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UploadMongoFileResult extends ResponseResult {
    private String fileId;

    public UploadMongoFileResult(ResultCode resultCode, String fileId) {
        super(resultCode);
        this.fileId = fileId;
    }

}
