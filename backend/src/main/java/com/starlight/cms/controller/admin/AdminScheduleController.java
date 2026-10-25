package com.starlight.cms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starlight.cms.common.Result;
import com.starlight.cms.entity.BizSchedule;
import com.starlight.cms.security.SecurityUtils;
import com.starlight.cms.service.IBizScheduleService;
import com.starlight.cms.vo.ScheduleVO;
import com.starlight.cms.vo.SeatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 管理后台 - 排片场次管理
 */
@RestController
@RequestMapping("/admin/schedule")
@RequiredArgsConstructor
public class AdminScheduleController {

    private final IBizScheduleService scheduleService;

    @GetMapping("/page")
    public Result<IPage<ScheduleVO>> page(@RequestParam(defaultValue = "1") long current,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) Long movieId,
                                          @RequestParam(required = false) Long hallId,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        SecurityUtils.requireAdmin();
        return Result.ok(scheduleService.adminPage(current, size, movieId, hallId, keyword, date));
    }

    /** 新增排片（后端自动算散场时间并做冲突校验） */
    @PostMapping("/save")
    public Result<Void> save(@RequestBody BizSchedule schedule) {
        SecurityUtils.requireAdmin();
        if (schedule.getId() == null) {
            scheduleService.createSchedule(schedule);
        } else {
            scheduleService.updateSchedule(schedule);
        }
        return Result.ok();
    }

    /** 取消场次 */
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        SecurityUtils.requireAdmin();
        BizSchedule s = new BizSchedule();
        s.setId(id);
        s.setStatus(0);
        scheduleService.updateById(s);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SecurityUtils.requireAdmin();
        scheduleService.removeById(id);
        return Result.ok();
    }

    /** 查看某场次座位售卖情况 */
    @GetMapping("/seat/{scheduleId}")
    public Result<List<SeatVO>> seat(@PathVariable Long scheduleId) {
        SecurityUtils.requireAdmin();
        return Result.ok(scheduleService.seatMap(scheduleId));
    }
}
