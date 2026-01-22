package com.xuecheng.manage_cms.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class GridFileService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    public ObjectId storeFile(InputStream file, String name)  {
        return gridFsTemplate.store(file, name);
    }

    public void deleteById(String id) {
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is(id)));
    }

    public GridFSFile findFileById(String id) {
        return gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(id)));
    }

    public GridFsResource downloadFileById(String fileId) {

        GridFSFile file = findFileById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }
        //打开一个流对象
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(file.getObjectId());
        //
        return new GridFsResource(file, gridFSDownloadStream);

    }
}
