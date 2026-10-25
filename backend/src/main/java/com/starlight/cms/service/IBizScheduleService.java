package com.starlight.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starlight.cms.entity.BizSchedule;
import com.starlight.cms.vo.ScheduleVO;
import com.starlight.cms.vo.SeatVO;

import java.time.LocalDate;
import java.util.List;

public interface IBizScheduleService extends IService<BizSchedule> {

    /** 后台分页查询场次（可按电影/影厅/日期/电影名过滤） */
    IPage<ScheduleVO> adminPage(long current, long size, Long movieId, Long hallId,
                                String keyword, LocalDate date);

    /** 用户端：查询某电影的场次（默认未来未开场） */
    List<ScheduleVO> listByMovie(Long movieId, LocalDate date);

    /** 场次详情（含电影、影厅信息） */
    ScheduleVO detail(Long id);

    /** 新增排片：自动计算散场时间并校验同影厅时间冲突 */
    Long createSchedule(BizSchedule schedule);

    /** 修改排片（同样校验冲突） */
    void updateSchedule(BizSchedule schedule);

    /** 获取某场次的可视化座位图（标注可选/锁定/已售/维修） */
    List<SeatVO> seatMap(Long scheduleId);
}
