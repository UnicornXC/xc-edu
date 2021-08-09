package com.xuecheng.manage_course.ribbon;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRibbon {
    @Autowired
    RestTemplate restTemplate;

    @Test
    public void testRibbon(){
        // 确定需要获取的服务名称（默认在eureka中注册的服务名称就是当前项目名称的大写）
        String serviceID = "XC-SERVICE-MANAGE-CMS";
        // 使用for循环进行测试负载均衡的效果
        for (int i = 0; i <10; i++) {
            //Ribbon客户端从eurekaServer中获取服务列表。
            ResponseEntity<CmsPage> forEntity = restTemplate.getForEntity("http://"+serviceID+"/cms/page/get/5a754adf6abb500ad05688d9", CmsPage.class);
            CmsPage cmsPage = forEntity.getBody();
            System.out.println(cmsPage);
        }
    }
}
