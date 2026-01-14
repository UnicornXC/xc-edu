package com.xuecheng.manage_cms.controller;


import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.CmsPageService;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 页面预览接口实现类，接受页面预览的请求
 */
@Controller
@RequestMapping("/cms")
public class CmsPagePreviewController extends BaseController {

    @Autowired
    private CmsPageService pageService;

    //页面预览
    @GetMapping(value = "/preview/{pageId}")
    public void pagePreview(@PathVariable String pageId)
            throws IOException, TemplateException {
        String pageHtml = pageService.getPageHtml(pageId);
        response.setHeader("Content-type", "charset=utf-8");
        response.setContentType("text/html;charset=utf-8");
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(pageHtml.getBytes(StandardCharsets.UTF_8));
    }
}
