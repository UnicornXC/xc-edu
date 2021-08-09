package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CmsPageRepository extends MongoRepository<CmsPage,String> {

        /*
        *  支持自定义方法查询
        * -find+需要依据的参数名称
        * -count+需要依据的参数名称
        * */
        CmsPage findByPageName(String pageName);

        CmsPage findBySiteIdAndAndPageNameAndPageWebPath(String siteId, String pageName, String pageWebPath);

}
