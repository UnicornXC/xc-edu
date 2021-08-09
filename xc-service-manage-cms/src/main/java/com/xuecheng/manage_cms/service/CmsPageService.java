package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitMQConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class CmsPageService {

    @Autowired
    private CmsPageRepository pageRepository;
    @Autowired
    private CmsConfigRepository cmsConfigRepository;
    @Autowired
    private CmsTemplateRepository templateRepository;

    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * @page 页码，从1开始
     * @size 页面记录数
     * @param queryPageRequest  查询条件
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
         if (queryPageRequest==null){
             queryPageRequest = new QueryPageRequest();
         }
         //自定义条件查询
         //自定义条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase",
                        ExampleMatcher.GenericPropertyMatchers.contains());
         //定义值对象
        CmsPage cmsPage = new CmsPage();
        //设置条件值
        if (!StringUtils.isEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if (!StringUtils.isEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        if (!StringUtils.isEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //定义条件对象
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);

        if (page<=0){
            page=1;
        }
        page = page-1;
        if (size<=0){
            size=10;
        }
        //分页对象
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> list = pageRepository.findAll(example,pageable);//条件查询
        QueryResult queryResult = new QueryResult();
        queryResult.setList(list.getContent());
        queryResult.setTotal(list.getTotalElements());
        QueryResponseResult queryResponseResult =
                new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;

    }

    /**
     * 新增页面
     * @param cmsPage 需要新增的页面信息
     * @return
     */
    public CmsPageResult addPage(CmsPage cmsPage){
        //保证数据的唯一性，需要根据数据的siteId,pageName,pageWebPath进行校验
        CmsPage page = pageRepository.findBySiteIdAndAndPageNameAndPageWebPath(
                cmsPage.getSiteId(), cmsPage.getPageName(), cmsPage.getPageWebPath()
        );
        if (page == null){

            // 添加页面主键由系统（Spring Data）自动生成，
            cmsPage.setPageId(null);
            pageRepository.save(cmsPage);
            return new CmsPageResult(CommonCode.SUCCESS,cmsPage);
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /**
     * 根据id查询页面信息
     * @param id
     * @return
     */
    public CmsPage findById(String id){
        Optional<CmsPage> op = pageRepository.findById(id);
        if (op.isPresent()){
            CmsPage cmsPage = op.get();
            return cmsPage;
        }
        return null;
    }

    /**
     * 修改页面
     * @param id 需要修改的页面的id
     * @param cmsPage 需要修改的页面信息
     * @return
     */
    public CmsPageResult update(String id,CmsPage cmsPage){
        CmsPage cmsPage1 = this.findById(id);
        if (cmsPage1!=null){
            //修改数据
            cmsPage1.setPageAliase(cmsPage.getPageAliase());
            cmsPage1.setSiteId(cmsPage.getSiteId());
            cmsPage1.setPageName(cmsPage.getPageName());
            cmsPage1.setTemplateId(cmsPage.getTemplateId());
            cmsPage1.setPageWebPath(cmsPage.getPageWebPath());
            cmsPage1.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            cmsPage1.setPageStatus(cmsPage.getPageStatus());
            cmsPage1.setDataUrl(cmsPage.getDataUrl());
            pageRepository.save(cmsPage1);
            return new CmsPageResult(CommonCode.SUCCESS,cmsPage1);
        }
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    /**
     * 删除页面
     * @param id
     * @return
     */
    public ResponseResult delete(String id){
        //先查询
        Optional<CmsPage> c = pageRepository.findById(id);
        if (c.isPresent()){
            pageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 根据id查询cms的配置信息
     * @param id
     * @return
     */
    public CmsConfig getConfigById(String id){
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if (optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return  cmsConfig;
        }
        return null;
    }

    /**
     * 静态页面生成
     * @param pageId
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public String getPageHtml(String pageId) throws IOException, TemplateException {
        //静态化程序获取页面的dataUrl
        Map map = getModelByPageId(pageId);
        if (map==null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        String template = getTemplateByPageId(pageId);
        if (template==null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        log.debug("::页面已经生成==>>");
        //对页面进行静态化
        return generateHtml(template,map);
    }

    //生成静态页面
    private String generateHtml(String template, Map map) throws IOException, TemplateException {
        //配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());

        //创建模板加载器
        StringTemplateLoader templateLoader = new StringTemplateLoader();
        templateLoader.putTemplate("template",template);
        configuration.setTemplateLoader(templateLoader);
        Template template1 = configuration.getTemplate("template", "UTF_8");
        //合并页面与数据模型
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template1, map);
        return html;
    }

    //获取数据的模板信息
    private String getTemplateByPageId(String pageId){
        //取出页面的信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String templateId = cmsPage.getTemplateId();
        if (templateId==null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //借助tmplateId在数据库中查找对应的模板
        Optional<CmsTemplate> optional = templateRepository.findById(templateId);
        if (optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            String templateFileId = cmsTemplate.getTemplateFileId();
            //根据模板中的TemplateFileId查找gridFS文件系统的文件
            GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));

            //打开一个流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(file.getObjectId());
            //
            GridFsResource gridFsResource = new GridFsResource(file,gridFSDownloadStream);

            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }finally {

            }
        }
        return null;
    }

    //获取数据的模型
    private Map getModelByPageId(String pageId){
        //取出页面的信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出页面中的dataUrl
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //通过restTemplate请求dataUrl中的数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        if (forEntity != null) {
            Map body = forEntity.getBody();
            return body;
        }
        return null;
    }

    /**
     * 发布页面信息
     *
     * @param pageId
     * @return
     * @throws Exception
     */
    public ResponseResult post(String pageId) {
        //页面静态化
        String html = null;
        try {
            html = this.getPageHtml(pageId);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            log.error(e.getMessage(),"模板异常");
            e.printStackTrace();
        }
        if (StringUtils.isEmpty(html)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        //将静态化的文件存储到GridFS
        CmsPage page = saveHtml(pageId,html);
        //向rabbitMQ发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);

    }
    /* 保存文件到GridFS */
    private CmsPage saveHtml(String pageId, String html) {
        //查询页面
        Optional<CmsPage> optional = pageRepository.findById(pageId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();
        //存储之前先删除
        String htmlFileId = cmsPage.getHtmlFileId();
        if (StringUtils.isNotEmpty(htmlFileId)){
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }
        //保存文件
        InputStream is = IOUtils.toInputStream(html);
        ObjectId objectId = gridFsTemplate.store(is, cmsPage.getPageName());
        //文件id
        String fileId = objectId.toString();
        //将文件id存储到文件
        cmsPage.setHtmlFileId(fileId);
        pageRepository.save(cmsPage);
        return cmsPage;
    }
    /* 发送消息到rabbitMQ */
    private void sendPostPage(String pageId) {
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        Map<String,String> msgMap = new HashMap();
        msgMap.put("pageId",pageId);
        String msg = JSON.toJSONString(msgMap);
        String siteId = cmsPage.getSiteId();
        //发布消息
        this.rabbitTemplate.convertAndSend(RabbitMQConfig.EX_ROUTING_CMS_POSTPAGE,siteId,msg);
    }

    /**
     * 更新或添加预览页面
     * @param cmsPage
     * @return
     */
    public CmsPageResult save(CmsPage cmsPage) {
        //先检查页面是否已经存在，需要根据数据的siteId,pageName,pageWebPath进行校验
        CmsPage page = pageRepository.findBySiteIdAndAndPageNameAndPageWebPath(
                cmsPage.getSiteId(), cmsPage.getPageName(), cmsPage.getPageWebPath()
        );
        if (page != null) {
            // 页面已经存在则更新页面
            return this.update(page.getPageId(), cmsPage);
        }else{
            // 页面不存在，新建页面
            return this.addPage(cmsPage);
        }
    }

    /**
     * 一键发布页面的方法
     * @return
     */
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        // 先使用 save 方法判断页面是否存在
        CmsPageResult save = this.save(cmsPage);
        if(!save.isSuccess()){
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }
        CmsPage cmsp = save.getCmsPage();
        // 发布的页面id
        String pageId = cmsp.getPageId();
        // 发布页面
        ResponseResult post = this.post(pageId);
        if(!post.isSuccess()){
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }
        // 得到页面的url
        // 页面的url = 站点的域名+站点的webpath+网页的webpath+文件名
        // 站点ID
        String siteId = cmsp.getSiteId();
        // 站点信息
        CmsSite site = findCmsSiteById(siteId);
        // 取出站点路径
        String siteDomain = site.getSiteDomain();
        // 站点的web路径
        String siteWebpath = site.getSiteWebPath();
        // 页面的web路径
        String pageWebpath = cmsp.getPageWebPath();
        // 页面的名称
        String pageName = cmsp.getPageName();
        // 页面的访问路径
        String url = siteDomain + siteWebpath + pageWebpath + pageName;
        return new CmsPostPageResult(CommonCode.SUCCESS,url);

    }

    // 根据ID查询站点的信息
    private CmsSite findCmsSiteById(String siteId) {

        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        return optional.orElse(null);
    }
}
