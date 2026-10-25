package com.starlight.cms.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.starlight.cms.common.Constants;
import com.starlight.cms.dto.OrderCreateDTO;
import com.starlight.cms.dto.PayDTO;
import com.starlight.cms.entity.*;
import com.starlight.cms.exception.BusinessException;
import com.starlight.cms.mapper.*;
import com.starlight.cms.service.impl.BizOrderServiceImpl;
import com.starlight.cms.util.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 订单核心链路单元测试
 * 覆盖：下单校验（场次/座位/超卖）、支付（越权/超时/余额）、退票时限、检票状态机
 * 说明：全部使用 Mockito 模拟 Mapper 与 Redis，无需真实数据库/中间件即可运行。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BizOrderServiceImplTest {

    @Mock private BizOrderMapper orderMapper;
    @Mock private BizOrderSeatMapper orderSeatMapper;
    @Mock private BizScheduleMapper scheduleMapper;
    @Mock private BizSeatMapper seatMapper;
    @Mock private BizMovieMapper movieMapper;
    @Mock private BizHallMapper hallMapper;
    @Mock private BizMemberMapper memberMapper;
    @Mock private BizMemberLevelMapper memberLevelMapper;
    @Mock private BizTicketCheckMapper ticketCheckMapper;
    @Mock private SysUserMapper userMapper;
    @Mock private RedisService redisService;

    @InjectMocks
    private BizOrderServiceImpl orderService;

    /** MyBatis-Plus ServiceImpl 的泛型 baseMapper 无法被 Mockito 自动注入，手动反射注入 */
    @BeforeEach
    void injectBaseMapper() {
        ReflectionTestUtils.setField(orderService, "baseMapper", orderMapper);
    }

    @Test
    @DisplayName("下单：场次不存在直接拒绝")
    void createOrderWithMissingSchedule() {
        when(scheduleMapper.selectById(1L)).thenReturn(null);
        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, dto(1L, Arrays.asList(101L))));
        assertEquals("场次不存在或已取消", e.getMessage());
    }

    @Test
    @DisplayName("下单：已取消场次拒绝")
    void createOrderWithCancelledSchedule() {
        BizSchedule s = schedule();
        s.setStatus(0);
        when(scheduleMapper.selectById(1L)).thenReturn(s);
        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, dto(1L, Arrays.asList(101L))));
        assertEquals("场次不存在或已取消", e.getMessage());
    }

    @Test
    @DisplayName("下单：已开场场次拒绝")
    void createOrderWithStartedSchedule() {
        BizSchedule s = schedule();
        s.setStartTime(LocalDateTime.now().minusMinutes(5));
        when(scheduleMapper.selectById(1L)).thenReturn(s);
        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, dto(1L, Arrays.asList(101L))));
        assertEquals("该场次已开场，无法购票", e.getMessage());
    }

    @Test
    @DisplayName("下单：未选择座位拒绝")
    void createOrderWithoutSeats() {
        when(scheduleMapper.selectById(1L)).thenReturn(schedule());
        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, dto(1L, Collections.emptyList())));
        assertEquals("请至少选择一个座位", e.getMessage());
    }

    @Test
    @DisplayName("下单：超过单笔 6 张上限拒绝（防黄牛）")
    void createOrderExceedingMaxSeats() {
        when(scheduleMapper.selectById(1L)).thenReturn(schedule());
        List<Long> seats = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L);
        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, dto(1L, seats)));
        assertTrue(e.getMessage().contains("单次最多购买"));
    }

    @Test
    @DisplayName("下单：座位 ID 无效（查回数量不一致）拒绝")
    void createOrderWithInvalidSeatIds() {
        when(scheduleMapper.selectById(1L)).thenReturn(schedule());
        when(seatMapper.selectBatchIds(anyCollection())).thenReturn(Collections.singletonList(seat(101L, 10L, 1)));
        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, dto(1L, Arrays.asList(101L, 102L))));
        assertEquals("存在无效座位，请重新选座", e.getMessage());
    }

    @Test
    @DisplayName("下单：座位不属于该影厅拒绝")
    void createOrderWithSeatInAnotherHall() {
        when(scheduleMapper.selectById(1L)).thenReturn(schedule());
        when(seatMapper.selectBatchIds(anyCollection())).thenReturn(Collections.singletonList(seat(101L, 99L, 1)));
        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, dto(1L, Collections.singletonList(101L))));
        assertEquals("所选座位不属于该场次影厅", e.getMessage());
    }

    @Test
    @DisplayName("下单：维修中座位拒绝")
    void createOrderWithMaintenanceSeat() {
        when(scheduleMapper.selectById(1L)).thenReturn(schedule());
        when(seatMapper.selectBatchIds(anyCollection())).thenReturn(Collections.singletonList(seat(101L, 10L, 0)));
        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, dto(1L, Collections.singletonList(101L))));
        assertTrue(e.getMessage().contains("维修中不可选"));
    }

    @Test
    @DisplayName("下单：数据库已售座位拒绝（防重复售卖）")
    void createOrderWithSeatAlreadySold() {
        when(scheduleMapper.selectById(1L)).thenReturn(schedule());
        when(seatMapper.selectBatchIds(anyCollection())).thenReturn(Collections.singletonList(seat(101L, 10L, 1)));
        BizOrder paidOrder = order(1L, Constants.ORDER_PAID, LocalDateTime.now().plusMinutes(10));
        when(orderMapper.selectList(any(Wrapper.class))).thenReturn(Collections.singletonList(paidOrder));
        BizOrderSeat os = new BizOrderSeat();
        os.setOrderId(1L);
        os.setSeatId(101L);
        when(orderSeatMapper.selectList(any(Wrapper.class))).thenReturn(Collections.singletonList(os));

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(7L, dto(1L, Collections.singletonList(101L))));
        assertTrue(e.getMessage().contains("手慢了"));
    }

    @Test
    @DisplayName("下单：Redis 抢锁失败时回滚已加锁座位（并发防超卖）")
    void createOrderShouldRollbackLocksOnConflict() {
        when(scheduleMapper.selectById(1L)).thenReturn(schedule());
        when(seatMapper.selectBatchIds(anyCollection()))
                .thenReturn(Arrays.asList(seat(101L, 10L, 1), seat(102L, 10L, 1)));
        when(orderMapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());
        when(redisService.setNx(eq(Constants.REDIS_SEAT_LOCK + "1:101"), any(), anyLong(), any()))
                .thenReturn(true);
        when(redisService.setNx(eq(Constants.REDIS_SEAT_LOCK + "1:102"), any(), anyLong(), any()))
                .thenReturn(false);
        when(redisService.get(anyString())).thenReturn("another-user:tag");

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(7L, dto(1L, Arrays.asList(101L, 102L))));

        assertTrue(e.getMessage().contains("座位竞争激烈"));
        // 已成功锁定的 101 必须被回滚释放
        verify(redisService).delete(Constants.REDIS_SEAT_LOCK + "1:101");
    }

    @Test
    @DisplayName("支付：订单号为空拒绝")
    void payWithoutOrderNo() {
        PayDTO dto = new PayDTO();
        dto.setPayType(1);
        BusinessException e = assertThrows(BusinessException.class, () -> orderService.payOrder(1L, dto));
        assertEquals("缺少订单号", e.getMessage());
    }

    @Test
    @DisplayName("支付：不能支付他人订单")
    void payOthersOrderShouldFail() {
        when(orderMapper.selectOne(any(Wrapper.class)))
                .thenReturn(order(2L, Constants.ORDER_UNPAID, LocalDateTime.now().plusMinutes(5)));
        BusinessException e = assertThrows(BusinessException.class, () -> orderService.payOrder(1L, payDto()));
        assertEquals("不能支付他人订单", e.getMessage());
    }

    @Test
    @DisplayName("支付：非待支付状态拒绝")
    void payNonUnpaidOrderShouldFail() {
        when(orderMapper.selectOne(any(Wrapper.class)))
                .thenReturn(order(1L, Constants.ORDER_PAID, LocalDateTime.now().plusMinutes(5)));
        BusinessException e = assertThrows(BusinessException.class, () -> orderService.payOrder(1L, payDto()));
        assertEquals("订单状态不是待支付，无法支付", e.getMessage());
    }

    @Test
    @DisplayName("支付：订单超时自动取消并释放座位")
    void payExpiredOrderShouldCancel() {
        BizOrder expired = order(1L, Constants.ORDER_UNPAID, LocalDateTime.now().minusMinutes(1));
        when(orderMapper.selectOne(any(Wrapper.class))).thenReturn(expired);
        when(orderMapper.updateById(any(BizOrder.class))).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class, () -> orderService.payOrder(1L, payDto()));

        assertTrue(e.getMessage().contains("订单已超时"));
        assertEquals(Constants.ORDER_CANCEL, expired.getOrderStatus());
        verify(orderMapper).updateById(expired);
    }

    @Test
    @DisplayName("支付：会员余额不足拒绝")
    void payWithInsufficientBalance() {
        BizOrder order = order(1L, Constants.ORDER_UNPAID, LocalDateTime.now().plusMinutes(10));
        order.setPayAmount(new BigDecimal("100.00"));
        when(orderMapper.selectOne(any(Wrapper.class))).thenReturn(order);
        BizMember member = new BizMember();
        member.setBalance(new BigDecimal("50.00"));
        when(memberMapper.selectOne(any(Wrapper.class))).thenReturn(member);

        PayDTO dto = payDto();
        dto.setPayType(3);
        BusinessException e = assertThrows(BusinessException.class, () -> orderService.payOrder(1L, dto));
        assertEquals("会员余额不足，请选择其它支付方式或充值", e.getMessage());
    }

    @Test
    @DisplayName("取消：不能操作他人订单")
    void cancelOthersOrderShouldFail() {
        when(orderMapper.selectOne(any(Wrapper.class)))
                .thenReturn(order(2L, Constants.ORDER_UNPAID, LocalDateTime.now().plusMinutes(5)));
        BusinessException e = assertThrows(BusinessException.class, () -> orderService.cancelOrder(1L, "CG001"));
        assertEquals("不能操作他人订单", e.getMessage());
    }

    @Test
    @DisplayName("取消：仅待支付订单可取消")
    void cancelPaidOrderShouldFail() {
        when(orderMapper.selectOne(any(Wrapper.class)))
                .thenReturn(order(1L, Constants.ORDER_PAID, LocalDateTime.now().plusMinutes(5)));
        BusinessException e = assertThrows(BusinessException.class, () -> orderService.cancelOrder(1L, "CG001"));
        assertEquals("仅待支付订单可取消", e.getMessage());
    }

    @Test
    @DisplayName("退票：未支付订单不可退票")
    void refundUnpaidOrderShouldFail() {
        when(orderMapper.selectOne(any(Wrapper.class)))
                .thenReturn(order(1L, Constants.ORDER_UNPAID, LocalDateTime.now().plusMinutes(5)));
        BusinessException e = assertThrows(BusinessException.class, () -> orderService.refundOrder(1L, "CG001"));
        assertEquals("仅已支付订单可退票", e.getMessage());
    }

    @Test
    @DisplayName("退票：开场前 30 分钟内不支持退票")
    void refundTooLateShouldFail() {
        BizOrder paid = order(1L, Constants.ORDER_PAID, LocalDateTime.now().plusMinutes(100));
        when(orderMapper.selectOne(any(Wrapper.class))).thenReturn(paid);
        BizSchedule soon = schedule();
        soon.setStartTime(LocalDateTime.now().plusMinutes(10));
        when(scheduleMapper.selectById(1L)).thenReturn(soon);

        BusinessException e = assertThrows(BusinessException.class, () -> orderService.refundOrder(1L, "CG001"));
        assertEquals("开场前30分钟内不支持退票", e.getMessage());
    }

    @Test
    @DisplayName("检票：非已支付订单核销失败并留痕")
    void checkTicketForNonPaidOrderShouldRecordFailure() {
        BizOrder cancelled = order(1L, Constants.ORDER_CANCEL, LocalDateTime.now().plusMinutes(5));
        when(orderMapper.selectOne(any(Wrapper.class))).thenReturn(cancelled);
        when(orderMapper.selectById(1L)).thenReturn(cancelled);
        when(ticketCheckMapper.insert(any(BizTicketCheck.class))).thenReturn(1);

        Map<String, Object> result = orderService.checkTicket(2L, "CG001");

        assertEquals(false, result.get("success"));
        verify(ticketCheckMapper).insert(argThat(check -> check != null && check.getResult() == 0));
    }

    private BizSchedule schedule() {
        BizSchedule s = new BizSchedule();
        s.setId(1L);
        s.setMovieId(100L);
        s.setHallId(10L);
        s.setStartTime(LocalDateTime.now().plusHours(2));
        s.setEndTime(LocalDateTime.now().plusHours(4));
        s.setPrice(new BigDecimal("50.00"));
        s.setStatus(1);
        return s;
    }

    private BizSeat seat(Long id, Long hallId, Integer status) {
        BizSeat s = new BizSeat();
        s.setId(id);
        s.setHallId(hallId);
        s.setRowNo(1);
        s.setColNo(1);
        s.setSeatName("A" + id);
        s.setStatus(status);
        return s;
    }

    private BizOrder order(Long userId, int status, LocalDateTime expireTime) {
        BizOrder o = new BizOrder();
        o.setId(1L);
        o.setOrderNo("CG001");
        o.setUserId(userId);
        o.setScheduleId(1L);
        o.setMovieId(100L);
        o.setHallId(10L);
        o.setTotalAmount(new BigDecimal("50.00"));
        o.setPayAmount(new BigDecimal("50.00"));
        o.setSeatCount(1);
        o.setOrderStatus(status);
        o.setExpireTime(expireTime);
        return o;
    }

    private OrderCreateDTO dto(Long scheduleId, List<Long> seatIds) {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setScheduleId(scheduleId);
        dto.setSeatIds(seatIds);
        return dto;
    }

    private PayDTO payDto() {
        PayDTO dto = new PayDTO();
        dto.setOrderNo("CG001");
        dto.setPayType(1);
        return dto;
    }
}
