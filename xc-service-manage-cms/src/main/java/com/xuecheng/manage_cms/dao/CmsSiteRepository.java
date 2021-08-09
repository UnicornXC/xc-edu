package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 用于获取站点的信息（站点域名，站点的访问路径）
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite,String> {

}
