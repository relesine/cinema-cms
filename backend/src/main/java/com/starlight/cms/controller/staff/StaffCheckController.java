package com.starlight.cms.controller.staff;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starlight.cms.common.Result;
import com.starlight.cms.entity.BizOrder;
import com.starlight.cms.entity.BizTicketCheck;
import com.starlight.cms.mapper.BizOrderMapper;
import com.starlight.cms.mapper.BizTicketCheckMapper;
import com.starlight.cms.security.SecurityUtils;
import com.starlight.cms.security.UserContext;
import com.starlight.cms.service.IBizOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工端 - 检票核销
 */
@RestController
@RequestMapping("/staff/check")
@RequiredArgsConstructor
public class StaffCheckController {

    private final IBizOrderService orderService;
    private final BizTicketCheckMapper ticketCheckMapper;
    private final BizOrderMapper orderMapper;

    /** 扫描/输入订单号核销 */
    @PostMapping("/scan/{orderNo}")
    public Result<Map<String, Object>> scan(@PathVariable String orderNo) {
        SecurityUtils.requireStaff();
        Long staffId = UserContext.getUserId();
        return Result.ok(orderService.checkTicket(staffId, orderNo));
    }

    /** 检票记录（最近50条，带订单号） */
    @GetMapping("/records")
    public Result<List<Map<String, Object>>> records() {
        SecurityUtils.requireStaff();
        List<BizTicketCheck> checks = ticketCheckMapper.selectList(new LambdaQueryWrapper<BizTicketCheck>()
                .orderByDesc(BizTicketCheck::getCheckTime).last("limit 50"));
        List<Long> orderIds = checks.stream().map(BizTicketCheck::getOrderId).collect(Collectors.toList());
        Map<Long, String> orderNoMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            for (BizOrder o : orderMapper.selectBatchIds(orderIds)) {
                orderNoMap.put(o.getId(), o.getOrderNo());
            }
        }
        List<Map<String, Object>> result = checks.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("record", c);
            m.put("orderNo", orderNoMap.get(c.getOrderId()));
            return m;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }
}
