package com.starlight.cms.controller.staff;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starlight.cms.common.Result;
import com.starlight.cms.security.SecurityUtils;
import com.starlight.cms.service.IBizOrderService;
import com.starlight.cms.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 员工端 - 订单查询
 */
@RestController
@RequestMapping("/staff/order")
@RequiredArgsConstructor
public class StaffOrderController {

    private final IBizOrderService orderService;

    @GetMapping("/page")
    public Result<IPage<OrderVO>> page(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) String orderNo,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) Long scheduleId) {
        SecurityUtils.requireStaff();
        return Result.ok(orderService.manageOrders(orderNo, status, scheduleId, current, size));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        SecurityUtils.requireStaff();
        return Result.ok(orderService.detail(id));
    }
}
