package com.starlight.cms.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starlight.cms.common.Result;
import com.starlight.cms.dto.OrderCreateDTO;
import com.starlight.cms.dto.PayDTO;
import com.starlight.cms.security.UserContext;
import com.starlight.cms.service.IBizOrderService;
import com.starlight.cms.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 订单交易：下单、支付、取消、退票、我的订单、详情
 */
@RestController
@RequestMapping("/app/order")
@RequiredArgsConstructor
public class AppOrderController {

    private final IBizOrderService orderService;

    /** 创建订单（锁座） */
    @PostMapping("/create")
    public Result<OrderVO> create(@Validated @RequestBody OrderCreateDTO dto) {
        return Result.ok("下单成功，请在15分钟内完成支付", orderService.createOrder(UserContext.getUserId(), dto));
    }

    /** 模拟支付 */
    @PostMapping("/pay")
    public Result<OrderVO> pay(@Validated @RequestBody PayDTO dto) {
        return Result.ok("支付成功", orderService.payOrder(UserContext.getUserId(), dto));
    }

    /** 取消待支付订单 */
    @PostMapping("/cancel/{orderNo}")
    public Result<Void> cancel(@PathVariable String orderNo) {
        orderService.cancelOrder(UserContext.getUserId(), orderNo);
        return Result.ok();
    }

    /** 退票 */
    @PostMapping("/refund/{orderNo}")
    public Result<Void> refund(@PathVariable String orderNo) {
        orderService.refundOrder(UserContext.getUserId(), orderNo);
        return Result.ok("退票成功，座位已释放");
    }

    /** 我的订单（可按状态筛选） */
    @GetMapping("/mine")
    public Result<IPage<OrderVO>> mine(@RequestParam(required = false) Integer status,
                                       @RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size) {
        return Result.ok(orderService.myOrders(UserContext.getUserId(), status, current, size));
    }

    /** 订单详情 */
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(id));
    }
}
