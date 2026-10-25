package com.starlight.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starlight.cms.common.Constants;
import com.starlight.cms.entity.BizOrder;
import com.starlight.cms.mapper.BizMemberMapper;
import com.starlight.cms.mapper.BizOrderMapper;
import com.starlight.cms.mapper.BizStatDailyMapper;
import com.starlight.cms.mapper.SysUserMapper;
import com.starlight.cms.service.IStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements IStatService {

    private final BizOrderMapper orderMapper;
    private final SysUserMapper userMapper;
    private final BizMemberMapper memberMapper;
    private final BizStatDailyMapper statDailyMapper;

    /** 统计口径：已支付(1) + 已完成(4) 计入票房 */
    private List<BizOrder> validOrders() {
        return orderMapper.selectList(new LambdaQueryWrapper<BizOrder>()
                .in(BizOrder::getOrderStatus, Constants.ORDER_PAID, Constants.ORDER_FINISH));
    }

    @Override
    public Map<String, Object> overview() {
        List<BizOrder> orders = validOrders();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int tickets = 0;
        BigDecimal todayAmount = BigDecimal.ZERO;
        int todayOrders = 0;
        LocalDate today = LocalDate.now();
        for (BizOrder o : orders) {
            totalAmount = totalAmount.add(o.getPayAmount());
            tickets += o.getSeatCount() == null ? 0 : o.getSeatCount();
            LocalDateTime pt = o.getPayTime() == null ? o.getCreateTime() : o.getPayTime();
            if (pt.toLocalDate().equals(today)) {
                todayAmount = todayAmount.add(o.getPayAmount());
                todayOrders++;
            }
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalAmount", totalAmount);
        map.put("orderCount", orders.size());
        map.put("ticketCount", tickets);
        map.put("userCount", userMapper.selectCount(null));
        map.put("memberCount", memberMapper.selectCount(null));
        map.put("todayAmount", todayAmount);
        map.put("todayOrders", todayOrders);
        return map;
    }

    @Override
    public List<Map<String, Object>> movieRank() {
        return statDailyMapper.movieBoxOffice();
    }

    @Override
    public List<Map<String, Object>> salesTrend(int days) {
        if (days <= 0) {
            days = 7;
        }
        List<BizOrder> orders = validOrders();
        // 初始化近 days 天，缺的日期补 0，保证图表连续
        Map<LocalDate, BigDecimal> amountByDate = new TreeMap<>();
        Map<LocalDate, Integer> countByDate = new TreeMap<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            amountByDate.put(d, BigDecimal.ZERO);
            countByDate.put(d, 0);
        }
        for (BizOrder o : orders) {
            LocalDateTime pt = o.getPayTime() == null ? o.getCreateTime() : o.getPayTime();
            LocalDate d = pt.toLocalDate();
            if (amountByDate.containsKey(d)) {
                amountByDate.put(d, amountByDate.get(d).add(o.getPayAmount()));
                countByDate.put(d, countByDate.get(d) + 1);
            }
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (LocalDate d : amountByDate.keySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", d.toString());
            row.put("amount", amountByDate.get(d));
            row.put("orderCount", countByDate.get(d));
            list.add(row);
        }
        return list;
    }
}
