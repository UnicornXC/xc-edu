package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CoursePublishResult;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.QueryCoursePicResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    CoursePubRepository coursePubRepository;
    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;
    @Autowired
    CmsPageClient cmsPageClient;

    /*
     * =============================================================
     * 注入课程详情页面预览的公共信息
     */
     @Value("${course-publish.dataUrlPre}")
     private String publishDataUrlPre;
     @Value("${course-publish.pagePhysicalPath}")
     private String publishPagePhysicalPath;
     @Value("${course-publish.pageWebPath}")
     private String publishPageWebPath;
     @Value("${course-publish.siteId}")
     private String publishSiteId;
     @Value("${course-publish.templateId}")
     private String publishTemplateId;
     @Value("${course-publish.previewUrl}")
     private String previewUrl;



    //课程计划查询
    public TeachplanNode findTeachplanList(String courseId) {

        return teachplanMapper.selectList(courseId);
    }

    /**
     * 添加课程的计划，将前端的相应的计划添加到父节点中去，若没有父节点，需要在课程计划数据库中查找相关课程的根节点添加，
     * 若课程计划数据可中没有该课程的父节点，需要在课程计划数据库中新建该课程的根节点
     * @param teachplan 前端传来的课程计划参数
     * @return 返回包含了 成功或是失败信息 的ResponseResult对象
     */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if (teachplan==null || StringUtils.isEmpty(teachplan.getCourseid())||
            StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程计划
        String courseId = teachplan.getCourseid();
        String parentId = teachplan.getParentid();
        if (StringUtils.isEmpty(parentId)){
            //取出课程根节点的信息
            parentId = this.getTeachplanRoot(courseId);
        }
        Optional<Teachplan> optionalTeachplan = teachplanRepository.findById(parentId);
        Teachplan parentNode = optionalTeachplan.get();
        // 可以获取grade
        String grade = parentNode.getGrade();
        //新节点
        Teachplan teachplanNew = new Teachplan();
        //将页面提交的teachplan信息拷贝teachplanNew中
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setParentid(parentId);
        teachplanNew.setCourseid(courseId);
        if (grade.equals("1")){//可以根据父节点的grade填写自己的节点grade信息
            teachplanNew.setGrade("2");
        }else{
            teachplanNew.setGrade("3");
        }
        teachplanRepository.save(teachplanNew);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程的根节点，如果查询不到，需要自己添加根节点
    private String getTeachplanRoot(String courseId){

        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()){
            return null;
        }
        //课程信息
        CourseBase courseBase = optional.get();
        //查询课程的根节点。
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId,"0");
        if (teachplanList==null || teachplanList.size()<=0){
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setCourseid(courseId);
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        return teachplanList.get(0).getId();
    }

    /**
     * 借用pageHelper的分页插件，实现后台分页
     * @param page 传递的分页的起始页码
     * @param size 传递的分页的页面大小
     * @param courseListRequest 传递的查询的参数
     * @return 返回指定分页的结果列表
     */
    public QueryResponseResult<CourseInfo> findCourseListPage(
            int page, int size, CourseListRequest courseListRequest
    ) {
        if (page<=0){
            page=0;
        }
        if (size<=0){
            size=10;
        }
        //设置分页参数
        PageHelper.startPage(page,size);
        //分页查询
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        //将查询的结果集封装到该结果类中
        QueryResult<CourseInfo> qr = new QueryResult<>();
        if (courseListPage != null) {
            qr.setList(courseListPage.getResult());
            qr.setTotal(courseListPage.getTotal());
        }
        return new QueryResponseResult<>(CommonCode.SUCCESS, qr);
    }
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase){
        //默认课程状态更改为未发布
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    public CourseBase getCourseBaseById(String courseId){
        Optional<CourseBase> option = courseBaseRepository.findById(courseId);
        return option.orElse(null);
        // ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
    }

    @Transactional
    public ResponseResult updateCourseBaseById(String courseId, CourseBase courseBase){
        CourseBase one = this.getCourseBaseById(courseId);
        if(one==null){
            ExceptionCast.cast(CourseCode.COURSE_BASE_UPDATE_QUERY_ISNULL);
        }
        one.setName(courseBase.getName());
        one.setMt(courseBase.getMt());
        one.setSt(courseBase.getSt());
        one.setGrade(courseBase.getGrade());
        one.setStudymodel(courseBase.getStudymodel());
        one.setUsers(courseBase.getUsers());
        one.setDescription(courseBase.getDescription());
        courseBaseRepository.save(courseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 根据课程的id信息查询对应课程的营销信息，当营销信息为空的时候返回空对象
     * @param courseId
     * @return
     */
    public CourseMarket getCourseMarketById(String courseId){
        Optional<CourseMarket> option = courseMarketRepository.findById(courseId);
        return option.orElseGet(CourseMarket::new);
    }

    /**
     * 根据课程的id先查询课程的营销信息，当营销信息为空的时候，添加营销信息，
     * 当营销信息不为空的时候，取出营销信息进行更改后再次保存。
     * @param courseId
     * @param courseMarket
     * @return
     */
    public CourseMarket updateCourseMarket(String courseId, CourseMarket courseMarket){
        CourseMarket marketInfo = this.getCourseMarketById(courseId);
        if (marketInfo != null) {
            marketInfo.setCharge(courseMarket.getCharge());
            marketInfo.setStartTime(courseMarket.getStartTime());
            marketInfo.setEndTime(courseMarket.getEndTime());
            marketInfo.setPrice(courseMarket.getPrice());
            marketInfo.setQq(courseMarket.getQq());
            marketInfo.setValid(courseMarket.getValid());
            courseMarketRepository.save(marketInfo);
        } else {
            /*添加课程营销信息*/
            marketInfo = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, marketInfo);
            /*设置课程的id信息*/
            marketInfo.setId(courseId);
            courseMarketRepository.save(marketInfo);
        }
        return marketInfo;
    }

    /**
     * 根据课程的id查询课程的全部视图信息
     * @param id 课程的id信息
     * @return CourseView
     */
    public CourseView getCourseView(String id) {
        CourseView courseView = new CourseView();

        // 根据id 去查询课程的基本信息
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(id);
        baseOptional.ifPresent(courseView::setCourseBase);
        // 根据id 去查询课程的图片信息
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        picOptional.ifPresent(courseView::setCoursePic);
        // 根据id 去查询课程的营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        marketOptional.ifPresent(courseView::setCourseMarket);
        // 根据id 去查询课程的计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);

        //返回课程的视图信息
        return courseView;

    }

    /**
     * 课程预览
     */
    public CoursePublishResult preview(String courseId){

        CourseBase base = this.getCourseBaseById(courseId);
        // 发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        // 将公共的信息注入到详情页面中
        cmsPage.setSiteId(publishSiteId);
        cmsPage.setTemplateId(publishTemplateId);
        cmsPage.setPageName(courseId+".html");
        cmsPage.setPageAliase(base.getName());
        cmsPage.setPageType("1");
        cmsPage.setPageCreateTime(new Date());
        cmsPage.setPageWebPath(publishPageWebPath);
        cmsPage.setPagePhysicalPath(publishPagePhysicalPath);
        cmsPage.setDataUrl(publishDataUrlPre + courseId);
        // OpenFeign 调用保存页面信息
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if(!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        // 获取页面id, 拼出预览页面所在的页面id
        String pageId = cmsPageResult.getCmsPage().getPageId();
        String pageUrl = previewUrl + pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);

    }

    /**
     * 课程发布
     * @param courseId
     * @return
     */
    @Transactional
    public CoursePublishResult publish(String courseId) {

        // 发布课程详情页面
        CmsPostPageResult cmsPostPageResult = publish_page(courseId);
        if(!cmsPostPageResult.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        // 更新课程状态
        CourseBase courseBase = saveCoursePubState(courseId);
        // 课程索引发布后为了方便维护索引，建立课程发布表，存储一个课程发布的信息
        CoursePub coursePub =  createCoursePub(courseId);
        // 将生成的课程发布信息保存到数据库
        CoursePub newCoursePub =  saveCoursePub(courseId,coursePub);
        if(newCoursePub == null){
            // 创建课程索引信息失败
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_CREATE_INDEX_ERROR);
        }
        // TODO 课程缓存...

        // 保存课程与媒资信息到索引表,方便建立索引搜索课程的媒资信息
        saveTeachplanMediaPub(courseId);
        // 页面url
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    private CoursePub saveCoursePub(String id, CoursePub coursePub) {

        if(StringUtils.isEmpty(id)){
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEID_ISNULL);
        }
        CoursePub coursePubNew = null;
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        if(coursePubOptional.isPresent()){
            coursePubNew = coursePubOptional.get();
        }
        if(coursePubNew == null){
            coursePubNew = new CoursePub();
        }
        BeanUtils.copyProperties(coursePub,coursePubNew);
        // 设置主键，设置更新时间,主要是给 logstash 使用来更新索引
        coursePubNew.setId(id);
        coursePubNew.setTimestamp(new Date());
        // 发布时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(new Date());
        coursePubNew.setPubTime(date);
        return coursePubRepository.save(coursePubNew);
    }

    private CoursePub createCoursePub(String courseId) {
        // 构建对象
        CoursePub coursePub = new CoursePub();
        coursePub.setId(courseId);

        // 设置基础信息
        Optional<CourseBase> base = courseBaseRepository.findById(courseId);
        if(base.isPresent()){
            CourseBase courseBase = base.get();
            BeanUtils.copyProperties(courseBase,coursePub);
        }
        // 查询课程图片
        Optional<CoursePic> pic = coursePicRepository.findById(courseId);
        if(pic.isPresent()){
            CoursePic coursePic = pic.get();
            BeanUtils.copyProperties(coursePic,coursePub);
        }
        // 查询课程的营销信息
        Optional<CourseMarket> market = courseMarketRepository.findById(courseId);
        if(market.isPresent()){
            CourseMarket courseMarket = market.get();
            BeanUtils.copyProperties(courseMarket,coursePub);
        }
        // 课程计划信息
        TeachplanNode node = teachplanMapper.selectList(courseId);
        // 课程计划转换为json 对象后保存到数据库
        String teachplan = JSON.toJSONString(node);
        coursePub.setTeachplan(teachplan);

        // 返回封装好的CoursePub
        return coursePub;
    }

    /**
     * 发布课程正式页面
     * @param courseId
     * @return
     */
    private CmsPostPageResult publish_page(String courseId) {
        // 课程信息
        CourseBase one = this.findCourseBaseById(courseId);
        // 发布课程预览的页面
        CmsPage cmsPage = new CmsPage();
        // 站点
        cmsPage.setSiteId(publishSiteId);
        // 模板
        cmsPage.setTemplateId(publishTemplateId);
        // 页面名称
        cmsPage.setPageName(courseId + ".html");
        // 页面别名
        cmsPage.setPageAliase(one.getName());
        // 页面访问路径
        cmsPage.setPageWebPath(publishPageWebPath);
        // 页面存储路径
        cmsPage.setPagePhysicalPath(publishPagePhysicalPath);
        // 数据url
        cmsPage.setDataUrl(publishDataUrlPre + courseId);
        // 发布页面
        return cmsPageClient.postPageQuick(cmsPage);
    }

    /* 改变课程发布的状态 */
    private CourseBase saveCoursePubState(String courseId) {
        // 课程信息
        CourseBase one = this.findCourseBaseById(courseId);
        // 课程发布状态
        one.setStatus("202002");
        CourseBase save = courseBaseRepository.save(one);
        return save;
    }

    /* 根据课程查找课程的基本信息 */
    private CourseBase findCourseBaseById(String courseId) {
        Optional<CourseBase> base =
                courseBaseRepository.findById(courseId);
        if(base.isPresent()){
           return base.get();
        }else{
            return null;
        }
    }

    // 保存课程计划与媒资之间的关联关系
    public ResponseResult saveMedia(TeachplanMedia teachplanMedia) {
        // 校验参数
        if (null == teachplanMedia
                || StringUtils.isEmpty(teachplanMedia.getMediaId())
                || StringUtils.isEmpty(teachplanMedia.getTeachplanId())
        ) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        // 课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        Teachplan tp = optional.get();
        // 只能为叶子节点选择课程
        String grade = tp.getGrade();
        if (StringUtils.isEmpty(grade) || !"3".equals(grade)) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADE_ERROR);
        }

        Optional<TeachplanMedia> op = teachplanMediaRepository.findById(teachplanId);
        // 有值的时候返回值，为 null 时返回一个新对象
        TeachplanMedia one = op.orElseGet(TeachplanMedia::new);
        // 保存媒资信息与课程计划信息的关系
        one.setTeachplanId(teachplanId);
        one.setCourseId(teachplanMedia.getCourseId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        teachplanMediaRepository.save(one);
        // 返回结果对象
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 根据课程的 Id 将课程与媒资的关联关系更新
     * @param courseId
     */
    private void saveTeachplanMediaPub(String courseId) {
        // 查询课程媒资信息
        List<TeachplanMedia> teachplanMediaList
                = teachplanMediaRepository.findByCourseId(courseId);
        // 将课程索引信息存储待索引表
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        teachplanMediaList.forEach(e->{
            TeachplanMediaPub pub = new TeachplanMediaPub();
            BeanUtils.copyProperties(e, pub);
            teachplanMediaPubList.add(pub);
        });
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }

    /**
     * 查询课程图片
     * @param courseId
     * @return
     */
    public QueryCoursePicResult queryCoursePic(String courseId) {
        CoursePic coursePic = coursePicRepository.findById(courseId).orElse(new CoursePic());
        return new QueryCoursePicResult(CommonCode.SUCCESS, coursePic.getPic());
    }


    /**
     * 添加课程图片
     */
    public ResponseResult addCoursePic(String courseId, String pic) {
        CoursePic picture = new CoursePic();
        picture.setCourseid(courseId);
        picture.setPic(pic);
        coursePicRepository.save(picture);
        return ResponseResult.SUCCESS();
    }


    /**
     * 删除课程图片
     * @param courseId
     * @return
     */
    public ResponseResult deleteCoursePic(String courseId) {
        coursePicRepository.deleteById(courseId);
        return ResponseResult.SUCCESS();
    }

}