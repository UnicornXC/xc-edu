package com.xuecheng.manage_cms.dao;


import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {
    @Autowired
    CmsPageRepository cmsPageRepository;

    @Test
    public void testFindAll(){
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
        System.out.println("12364848");
    }

    @Test
    public void testFindPage(){
        int page=1;
        int size=10;
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }

    @Test
    public void testUPdate(){
        Optional<CmsPage> option =
                cmsPageRepository.findById("5a754adf6abb500ad05688d9");
        if (option.isPresent()) {
            CmsPage page = option.get();
            page.setPageAliase("首页test");
            cmsPageRepository.save(page);
        }
    }

    @Test
    public void testAutoDefid(){
        CmsPage page = cmsPageRepository.findByPageName("index.html");
        System.out.println(page.getPageId());
    }
    //自定义条件查询
    @Test
    public void testFindAllByExample(){
        //分页参数
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page,size);
        //条件参数
        //构建值对象
        CmsPage cmsPage = new CmsPage();
        cmsPage.setPageAliase("轮播");

        //构建适配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase",
                        ExampleMatcher.GenericPropertyMatchers.contains());

        //构建条件对象
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);

        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> list = all.getContent();
        System.out.println(list);
    }
}
