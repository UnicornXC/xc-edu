package com.xuecheng.manage_cms.dao;


import com.xuecheng.manage_cms.service.CmsPageService;
import freemarker.template.TemplateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PageGenerateTest {
    @Autowired
    CmsPageService pageService;

    @Test
    public void testGenerateHtml() throws IOException, TemplateException {
        String pageHtml = pageService.getPageHtml("5da843b1922d081c7041a14b");
        System.out.println(pageHtml);
    }



}
