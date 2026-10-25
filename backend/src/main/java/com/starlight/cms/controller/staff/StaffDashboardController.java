package com.starlight.cms.controller.staff;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starlight.cms.common.Result;
import com.starlight.cms.entity.BizSchedule;
import com.starlight.cms.mapper.BizScheduleMapper;
import com.starlight.cms.security.SecurityUtils;
import com.starlight.cms.service.IStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工端 - 个人工作台
 */
@RestController
@RequestMapping("/staff/dashboard")
@RequiredArgsConstructor
public class StaffDashboardController {

    private final IStatService statService;
    private final BizScheduleMapper scheduleMapper;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        SecurityUtils.requireStaff();
        Map<String, Object> data = new HashMap<>(statService.overview());
        // 今日剩余待开场场次
        Long todaySchedules = scheduleMapper.selectCount(new LambdaQueryWrapper<BizSchedule>()
                .ge(BizSchedule::getStartTime, LocalDateTime.now().toLocalDate().atStartOfDay())
                .lt(BizSchedule::getStartTime, LocalDateTime.now().toLocalDate().plusDays(1).atStartOfDay())
                .eq(BizSchedule::getStatus, 1));
        data.put("todaySchedules", todaySchedules);
        return Result.ok(data);
    }
}
