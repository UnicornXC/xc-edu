package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class XcTaskService {

    @Autowired
    private XcTaskRepository xcTaskRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private XcTaskHisRepository xcTaskHisRepository;

    /**
     * 根据任务的更新时间，查找到 1 minute 之前的 N 数据
     * @param updateTime
     * @param size
     * @return
     */
    public List<XcTask> findByUpdateTimeBefore(Date updateTime, int size) {

        // 构建分页对象
        Pageable pageable = new PageRequest(0,size);
        // 查询数据
        Page<XcTask> list = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);

        return list.getContent();

    }

    /**
     * 向 RabbitMQ 发布任务
     * @param e
     * @param exchange
     * @param routingKey
     */
    @Transactional
    public void publishTask(XcTask e, String exchange, String routingKey) {
        // 获取对象
        Optional<XcTask> optional = xcTaskRepository.findById(e.getId());
        if (optional.isPresent()) {
            XcTask xcTask = optional.get();
            rabbitTemplate.convertAndSend(exchange,routingKey,xcTask);
            // 更新任务开始的时间
            xcTask.setUpdateTime(new Date());
            xcTaskRepository.save(xcTask);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------
     * 利用数据库本身的行级锁，对更新数据做版本控制，实现乐观锁。
     * --------------
     *  - 当程序拿着查询的数据的版本去更新数据库的数据版本，更新记录成成功时，表示数据的任务被程序得到
     *  - 当程序拿着已经查询出的数据版本去更新数据库版本，更新失败，表示数据已经被其他程序更新，任务被其他程序占有
     * ---------------------------------------------------------------------------------------
     * @param id
     * @param version
     * @return
     */
    @Transactional
    public int checkTask(String id, Integer version) {
        return xcTaskRepository.UpdateTaskVersion(version, id);
    }


    /**
     * 结束选课任务
     * --------------------------------------------------------------------------------------
     *  - 移除选课任务，
     *  - 添加到选课历史
     * ---------------------------------
     *  - 本地事务控制
     * @param id
     */
    @Transactional
    public void finishTask(String id) {
        Optional<XcTask> optional = xcTaskRepository.findById(id);
        if (optional.isPresent()) {
            XcTask xcTask = optional.get();
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask, xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            xcTaskRepository.delete(xcTask);
        }
    }
}
