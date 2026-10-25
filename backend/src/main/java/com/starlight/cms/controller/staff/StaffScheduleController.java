package com.starlight.cms.controller.staff;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starlight.cms.common.Result;
import com.starlight.cms.security.SecurityUtils;
import com.starlight.cms.service.IBizScheduleService;
import com.starlight.cms.vo.ScheduleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 员工端 - 场次查询
 */
@RestController
@RequestMapping("/staff/schedule")
@RequiredArgsConstructor
public class StaffScheduleController {

    private final IBizScheduleService scheduleService;

    @GetMapping("/page")
    public Result<IPage<ScheduleVO>> page(@RequestParam(defaultValue = "1") long current,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) Long movieId,
                                          @RequestParam(required = false) Long hallId,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        SecurityUtils.requireStaff();
        return Result.ok(scheduleService.adminPage(current, size, movieId, hallId, keyword, date));
    }

    @GetMapping("/{id}")
    public Result<ScheduleVO> detail(@PathVariable Long id) {
        SecurityUtils.requireStaff();
        return Result.ok(scheduleService.detail(id));
    }
}
