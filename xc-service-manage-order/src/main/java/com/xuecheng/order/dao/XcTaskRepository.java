package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.task.XcTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface XcTaskRepository extends JpaRepository<XcTask,String> {

    // 查 1 minute 之前的 N 条 数据
    Page<XcTask> findByUpdateTimeBefore(Pageable pageable, Date update);

    /** 更新数据的更新时间
     * ---------------------------------------------------------
     *  - 由于 Jpa 是面向对象的，所以更新的是对象(不是表名)中的属性(不是表字段)
     *  - 添加 @Modifying 注解
     *  - 定义的参数名称 需要与 SQL 中传递的参数名称一致
     * ---------------------------------------------------------
     */
    @Modifying
    @Query("update XcTask t set t.updateTime = :date where t.id = :id")
    int updateTaskTime(@Param("id") String id, @Param("date") Date date);

    /**
     * 更新数据库记录的版本
     */
    @Modifying
    @Query("update XcTask t set t.version = :version+1 where t.id = :id and t.version = :version")
    public int UpdateTaskVersion(@Param("version")Integer version, @Param("id") String id);


}
