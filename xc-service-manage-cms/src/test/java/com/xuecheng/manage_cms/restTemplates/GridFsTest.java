package com.xuecheng.manage_cms.restTemplates;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFsTest {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    //测试在mongodb中存文件
    @Test
    public void testGridFs() throws FileNotFoundException {

        //File file = new File("D:/abc/index_banner.html");
        File file = new File("D:/GIT/ProCode/xcEdu/xcEduService01/test-framemarker/src/main/resources/templates/course.ftl");
        FileInputStream fis = new FileInputStream(file);
        ObjectId testGrid = gridFsTemplate.store(fis, "课程预览模板");
        System.out.println(testGrid);
    }

    //测试从mongodb中取文件
    public void testQueryFile() throws IOException {
        //根据文件的id查询文件，
        GridFSFile gridFSFile = gridFsTemplate
                .findOne(Query.query(Criteria.where("_id").is("5daa96eb922d08441c16c734")));
        //打开一个文件下载流对象
        GridFSDownloadStream gridFSDownloadStream =
                gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建GridFSResource对象获取流
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        //流中获取数据
        String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
        System.out.println(content);

    }

}
