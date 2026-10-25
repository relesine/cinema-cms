package com.starlight.cms.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starlight.cms.common.Result;
import com.starlight.cms.entity.BizHall;
import com.starlight.cms.entity.BizSeat;
import com.starlight.cms.security.SecurityUtils;
import com.starlight.cms.service.IBizHallService;
import com.starlight.cms.service.IBizSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 影厅与座位管理
 */
@RestController
@RequestMapping("/admin/hall")
@RequiredArgsConstructor
public class AdminHallController {

    private final IBizHallService hallService;
    private final IBizSeatService seatService;

    @GetMapping("/list")
    public Result<List<BizHall>> list() {
        SecurityUtils.requireAdmin();
        return Result.ok(hallService.list(new LambdaQueryWrapper<BizHall>().orderByAsc(BizHall::getId)));
    }

    /** 新增/修改影厅，并按 行x列 自动生成座位 */
    @PostMapping("/save")
    public Result<Void> save(@RequestBody BizHall hall) {
        SecurityUtils.requireAdmin();
        if (hall.getRowCount() == null || hall.getColCount() == null
                || hall.getRowCount() <= 0 || hall.getColCount() <= 0) {
            return Result.error("影厅行列数必须大于0");
        }
        hall.setSeatCount(hall.getRowCount() * hall.getColCount());
        hallService.saveOrUpdate(hall);
        hallService.rebuildSeats(hall);
        return Result.ok();
    }

    /** 某影厅的座位矩阵 */
    @GetMapping("/seats/{hallId}")
    public Result<List<BizSeat>> seats(@PathVariable Long hallId) {
        SecurityUtils.requireAdmin();
        return Result.ok(seatService.list(new LambdaQueryWrapper<BizSeat>()
                .eq(BizSeat::getHallId, hallId)
                .orderByAsc(BizSeat::getRowNo).orderByAsc(BizSeat::getColNo)));
    }

    /** 修改单个座位状态（如设为维修） */
    @PostMapping("/seat/update")
    public Result<Void> updateSeat(@RequestBody BizSeat seat) {
        SecurityUtils.requireAdmin();
        seatService.updateById(seat);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SecurityUtils.requireAdmin();
        hallService.removeById(id);
        return Result.ok();
    }
}
