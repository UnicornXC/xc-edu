package com.xuecheng.manage_cms_client.mq;


import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * 监听MQ,接收页面发布的消息
 */
@Slf4j
@Component
public class ConsumePostPage {

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    PageService pageService;

    @RabbitListener(queues={"${xuecheng.mq.queue}"})
    public void  postPage(String msg) {
        //解析消息
        Map map = JSON.parseObject(msg,Map.class);
        String pageId = (String) map.get("pageId");

        //校验页面是否合法
        CmsPage cmsPage = pageService.findCmsPageById(pageId);
        if (cmsPage==null){
            log.error("recieve post msg,CmsPage is null pageId:"+pageId);
            return;
        }

        //调用pageService 将页面从GridFS下载
        pageService.savePageServerPath(pageId);

    }


}
