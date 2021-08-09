package com.xuecheng.framework.domain.course.ext;


import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * 用于课程介绍页面展示课程的全部信息
 */

@Data
@ToString
@NoArgsConstructor
public class CourseView implements Serializable {

    private CourseBase courseBase;       // 课程基础信息
    private CoursePic coursePic;         // 课程图片
    private CourseMarket courseMarket;   // 课程营销
    private TeachplanNode teachplanNode; // 教学计划

}
