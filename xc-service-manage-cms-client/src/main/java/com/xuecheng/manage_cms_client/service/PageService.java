package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

@Service
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    CmsSiteRepository cmsSiteRepository;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;

    /**
     * 保存html文件到物理路径
     * @param pageId
     */
    public void savePageServerPath(String pageId)  {
        //获取页面
        CmsPage cmsPage = this.findCmsPageById(pageId);
        //获取页面的htmlFileId
        String htmlFileId = cmsPage.getHtmlFileId();

        //根据站点的id获取站点信息
        CmsSite cmsSite = this.getCmsSiteById(cmsPage.getSiteId());
        //页面的路径
        String pagePath = cmsSite.getSitePhysicalPath()+cmsPage.getPagePhysicalPath()+cmsPage.getPageName();
        //将页面的文件保存到服务器的物理路径上
        try(
            InputStream inputStream = this.getFileId(htmlFileId).getInputStream();
            FileOutputStream  fileOutputStream = new FileOutputStream(pagePath);
        ) {
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //根据站点的id获取站点信息
    public CmsSite getCmsSiteById(String siteId){
        Optional<CmsSite> site = cmsSiteRepository.findById(siteId);
        return site.orElse(null);
    }

    //根据htmlFileId在GridFS中查找页面的信息
    public GridFsResource getFileId(String fileId)  {
        //文件对象
        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(file.getObjectId());
        //定义 GridFSResource
        return  new GridFsResource(file, gridFSDownloadStream);
    }

    //根页面的id查找页面信息s
    public CmsPage findCmsPageById(String pageId){
        Optional<CmsPage> page = cmsPageRepository.findById(pageId);
        return page.orElse(null);
    }
}
