package com.xuecheng.manage_course.client;

import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * SpringCloud 对 Feign 增强兼容了SpringMVC的注解，在Feign调用中使用SpringMVC注解的
 * 时候需要注意的是:
 * - 1. FeignClient接口在有参数的时候，必须要在使用的时候添加
 *     + @PatVariable()
 *     + @RequestParam();
 *     + @RequestBody()
 * - 2. FeignClient返回值为复杂对象的时，其类型必须要含有无参构造函数
 */

// 指定调用的远程服务的名称
@FeignClient(value= XcServiceList.XC_SERVICE_MANAGE_CMS)
public interface CmsPageClient {

    /* 根据页面的id查询页面的信息，远程调用 */
    @GetMapping("/cms/page/get/{id}")
    CmsPage findCmsPageById(@PathVariable("id")String id);


    /* 通过 OpenFeign 客户端进行远程调用，实现在课程管理项目对页面的保存和预览需求 */
    @PostMapping("/cms/page/save")
    CmsPageResult save(@RequestBody CmsPage cmsPage);


    /* 调用CMS服务发布页面，返回发布的结果 */
    @PostMapping("/cms/page/postPageQuick")
    CmsPostPageResult postPageQuick(CmsPage cmsPage);

}
