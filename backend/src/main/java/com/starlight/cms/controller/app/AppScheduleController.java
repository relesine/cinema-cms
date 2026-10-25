package com.starlight.cms.controller.app;

import com.starlight.cms.common.Result;
import com.starlight.cms.service.IBizScheduleService;
import com.starlight.cms.vo.ScheduleVO;
import com.starlight.cms.vo.SeatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户端 - 场次与选座
 */
@RestController
@RequestMapping("/app/schedule")
@RequiredArgsConstructor
public class AppScheduleController {

    private final IBizScheduleService scheduleService;

    /** 某电影的场次列表（可按日期） */
    @GetMapping("/movie/{movieId}")
    public Result<List<ScheduleVO>> byMovie(@PathVariable Long movieId,
                                            @RequestParam(required = false)
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(scheduleService.listByMovie(movieId, date));
    }

    /** 某场次详情 */
    @GetMapping("/{id}")
    public Result<ScheduleVO> detail(@PathVariable Long id) {
        return Result.ok(scheduleService.detail(id));
    }

    /** 某场次可视化座位图 */
    @GetMapping("/seat/{scheduleId}")
    public Result<List<SeatVO>> seatMap(@PathVariable Long scheduleId) {
        return Result.ok(scheduleService.seatMap(scheduleId));
    }
}
