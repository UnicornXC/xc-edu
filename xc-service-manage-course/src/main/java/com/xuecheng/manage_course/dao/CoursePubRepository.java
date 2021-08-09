package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePub;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 操作课程发布的Jpa
 */
public interface CoursePubRepository extends JpaRepository<CoursePub, String> {


}
