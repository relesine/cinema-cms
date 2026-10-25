package com.starlight.cms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starlight.cms.common.Result;
import com.starlight.cms.security.SecurityUtils;
import com.starlight.cms.service.IBizOrderService;
import com.starlight.cms.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台 - 订单管理（查询为主，交易由用户端发起）
 */
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final IBizOrderService orderService;

    @GetMapping("/page")
    public Result<IPage<OrderVO>> page(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) String orderNo,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) Long scheduleId) {
        SecurityUtils.requireAdmin();
        return Result.ok(orderService.manageOrders(orderNo, status, scheduleId, current, size));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        SecurityUtils.requireAdmin();
        return Result.ok(orderService.detail(id));
    }
}
