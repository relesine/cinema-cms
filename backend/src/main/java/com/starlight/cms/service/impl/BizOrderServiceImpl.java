package com.starlight.cms.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starlight.cms.common.Constants;
import com.starlight.cms.dto.OrderCreateDTO;
import com.starlight.cms.dto.PayDTO;
import com.starlight.cms.entity.*;
import com.starlight.cms.exception.BusinessException;
import com.starlight.cms.mapper.*;
import com.starlight.cms.service.IBizOrderService;
import com.starlight.cms.util.RedisService;
import com.starlight.cms.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizOrderServiceImpl extends ServiceImpl<BizOrderMapper, BizOrder> implements IBizOrderService {

    private final BizOrderSeatMapper orderSeatMapper;
    private final BizScheduleMapper scheduleMapper;
    private final BizSeatMapper seatMapper;
    private final BizMovieMapper movieMapper;
    private final BizHallMapper hallMapper;
    private final BizMemberMapper memberMapper;
    private final BizMemberLevelMapper memberLevelMapper;
    private final BizTicketCheckMapper ticketCheckMapper;
    private final SysUserMapper userMapper;
    private final RedisService redisService;

    private static final Map<Integer, String> STATUS_TEXT = new HashMap<>();
    static {
        STATUS_TEXT.put(0, "待支付");
        STATUS_TEXT.put(1, "已支付");
        STATUS_TEXT.put(2, "已退票");
        STATUS_TEXT.put(3, "已取消");
        STATUS_TEXT.put(4, "已完成");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, OrderCreateDTO dto) {
        // 1.校验场次
        BizSchedule schedule = scheduleMapper.selectById(dto.getScheduleId());
        if (schedule == null || schedule.getStatus() == 0) {
            throw new BusinessException("场次不存在或已取消");
        }
        if (schedule.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("该场次已开场，无法购票");
        }
        // 2.校验座位：去重、数量范围、归属影厅、非维修
        List<Long> seatIds = dto.getSeatIds().stream().distinct().collect(Collectors.toList());
        if (seatIds.isEmpty()) {
            throw new BusinessException("请至少选择一个座位");
        }
        if (seatIds.size() > Constants.MAX_SEATS_PER_ORDER) {
            throw new BusinessException("单次最多购买 " + Constants.MAX_SEATS_PER_ORDER + " 张票");
        }
        List<BizSeat> seats = seatMapper.selectBatchIds(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new BusinessException("存在无效座位，请重新选座");
        }
        for (BizSeat seat : seats) {
            if (!seat.getHallId().equals(schedule.getHallId())) {
                throw new BusinessException("所选座位不属于该场次影厅");
            }
            if (seat.getStatus() == 0) {
                throw new BusinessException("座位[" + seat.getSeatName() + "]维修中不可选");
            }
        }
        // 3.数据库层校验：这些座位是否已被有效订单占用（已售/待支付未超时）
        List<BizOrder> validOrders = baseMapper.selectList(new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getScheduleId, schedule.getId())
                .and(w -> w.eq(BizOrder::getOrderStatus, Constants.ORDER_PAID)
                        .or().eq(BizOrder::getOrderStatus, Constants.ORDER_FINISH)
                        .or(o -> o.eq(BizOrder::getOrderStatus, Constants.ORDER_UNPAID)
                                .gt(BizOrder::getExpireTime, LocalDateTime.now()))));
        if (!validOrders.isEmpty()) {
            Set<Long> occupiedSeatIds = orderSeatMapper.selectList(new LambdaQueryWrapper<BizOrderSeat>()
                            .in(BizOrderSeat::getOrderId, validOrders.stream().map(BizOrder::getId).collect(Collectors.toList())))
                    .stream().map(BizOrderSeat::getSeatId).collect(Collectors.toSet());
            for (Long sid : seatIds) {
                if (occupiedSeatIds.contains(sid)) {
                    throw new BusinessException("手慢了，所选座位中已有座位被他人购买或锁定");
                }
            }
        }
        // 4.Redis setnx 原子锁，防止并发瞬间超卖；任一失败回滚本次已加的锁
        String myTag = userId + ":" + UUID.randomUUID();
        List<String> lockedKeys = new ArrayList<>();
        for (Long sid : seatIds) {
            String key = lockKey(schedule.getId(), sid);
            boolean first = redisService.setNx(key, myTag, Constants.SEAT_LOCK_MINUTES, TimeUnit.MINUTES);
            if (!first) {
                Object v = redisService.get(key);
                if (v == null || !myTag.equals(v.toString())) {
                    // 被别人抢走，回滚
                    lockedKeys.forEach(k -> redisService.delete(k));
                    throw new BusinessException("座位竞争激烈，请重新选择（并发防超卖拦截）");
                }
                // 自己之前锁的，续期即可
                redisService.expire(key, Constants.SEAT_LOCK_MINUTES, TimeUnit.MINUTES);
            }
            lockedKeys.add(key);
        }
        // 5.计算价格与会员折扣
        BigDecimal total = schedule.getPrice().multiply(BigDecimal.valueOf(seatIds.size()));
        BigDecimal discount = getMemberDiscount(userId);
        BigDecimal pay = total.multiply(discount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = total.subtract(pay).setScale(2, RoundingMode.HALF_UP);
        // 6.生成订单
        BizOrder order = new BizOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setScheduleId(schedule.getId());
        order.setMovieId(schedule.getMovieId());
        order.setHallId(schedule.getHallId());
        order.setTotalAmount(total);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(pay);
        order.setSeatCount(seatIds.size());
        order.setOrderStatus(Constants.ORDER_UNPAID);
        order.setExpireTime(LocalDateTime.now().plusMinutes(Constants.SEAT_LOCK_MINUTES));
        baseMapper.insert(order);
        // 7.订单座位快照
        for (BizSeat seat : seats) {
            BizOrderSeat os = new BizOrderSeat();
            os.setOrderId(order.getId());
            os.setScheduleId(schedule.getId());
            os.setSeatId(seat.getId());
            os.setRowNo(seat.getRowNo());
            os.setColNo(seat.getColNo());
            os.setSeatName(seat.getSeatName());
            os.setPrice(schedule.getPrice());
            orderSeatMapper.insert(os);
        }
        log.info("用户{}创建待支付订单{}，座位{}，实付{}", userId, order.getOrderNo(), seatIds, pay);
        return detail(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO payOrder(Long userId, PayDTO dto) {
        BizOrder order = getByOrderNo(dto.getOrderNo());
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("不能支付他人订单");
        }
        if (order.getOrderStatus() != Constants.ORDER_UNPAID) {
            throw new BusinessException("订单状态不是待支付，无法支付");
        }
        if (order.getExpireTime().isBefore(LocalDateTime.now())) {
            order.setOrderStatus(Constants.ORDER_CANCEL);
            order.setCancelTime(LocalDateTime.now());
            baseMapper.updateById(order);
            releaseSeatLock(order);
            throw new BusinessException("订单已超时，请重新选座下单");
        }
        // 会员余额支付
        BizMember member = memberMapper.selectOne(new LambdaQueryWrapper<BizMember>().eq(BizMember::getUserId, userId));
        if (dto.getPayType() == 3) {
            if (member == null || member.getBalance().compareTo(order.getPayAmount()) < 0) {
                throw new BusinessException("会员余额不足，请选择其它支付方式或充值");
            }
            member.setBalance(member.getBalance().subtract(order.getPayAmount()));
            memberMapper.updateById(member);
        }
        order.setOrderStatus(Constants.ORDER_PAID);
        order.setPayType(dto.getPayType());
        order.setPayTime(LocalDateTime.now());
        baseMapper.updateById(order);
        // 支付成功：累计积分与消费并重算会员等级；释放 Redis 锁（之后以数据库已售为准）
        if (member != null) {
            member.setPoints(member.getPoints() + order.getPayAmount().intValue());
            member.setTotalConsume(member.getTotalConsume().add(order.getPayAmount()));
            member.setLevelId(refreshLevel(member.getPoints()));
            memberMapper.updateById(member);
        }
        releaseSeatLock(order);
        log.info("订单{}支付成功，方式{}", order.getOrderNo(), dto.getPayType());
        return detail(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, String orderNo) {
        BizOrder order = getByOrderNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("不能操作他人订单");
        }
        if (order.getOrderStatus() != Constants.ORDER_UNPAID) {
            throw new BusinessException("仅待支付订单可取消");
        }
        order.setOrderStatus(Constants.ORDER_CANCEL);
        order.setCancelTime(LocalDateTime.now());
        baseMapper.updateById(order);
        releaseSeatLock(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long userId, String orderNo) {
        BizOrder order = getByOrderNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("不能操作他人订单");
        }
        if (order.getOrderStatus() != Constants.ORDER_PAID) {
            throw new BusinessException("仅已支付订单可退票");
        }
        BizSchedule schedule = scheduleMapper.selectById(order.getScheduleId());
        // 退票时间限制：开场前30分钟以上才允许退
        if (schedule != null && schedule.getStartTime().minusMinutes(30).isBefore(LocalDateTime.now())) {
            throw new BusinessException("开场前30分钟内不支持退票");
        }
        order.setOrderStatus(Constants.ORDER_REFUND);
        order.setCancelTime(LocalDateTime.now());
        baseMapper.updateById(order);
        // 余额支付原路退回会员余额，并扣回积分
        BizMember member = memberMapper.selectOne(new LambdaQueryWrapper<BizMember>().eq(BizMember::getUserId, userId));
        if (member != null) {
            if (order.getPayType() != null && order.getPayType() == 3) {
                member.setBalance(member.getBalance().add(order.getPayAmount()));
            }
            member.setPoints(Math.max(0, member.getPoints() - order.getPayAmount().intValue()));
            member.setLevelId(refreshLevel(member.getPoints()));
            memberMapper.updateById(member);
        }
        log.info("订单{}已退票，座位释放", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> checkTicket(Long staffId, String orderNo) {
        Map<String, Object> result = new HashMap<>();
        BizOrder order = getByOrderNo(orderNo);
        BizTicketCheck check = new BizTicketCheck();
        check.setOrderId(order.getId());
        check.setUserId(order.getUserId());
        check.setScheduleId(order.getScheduleId());
        check.setStaffId(staffId);
        check.setCheckTime(LocalDateTime.now());
        if (order.getOrderStatus() == Constants.ORDER_PAID) {
            order.setOrderStatus(Constants.ORDER_FINISH);
            order.setTicketTime(LocalDateTime.now());
            baseMapper.updateById(order);
            check.setResult(1);
            check.setNote("核销成功");
            result.put("success", true);
            result.put("message", "核销成功");
        } else {
            check.setResult(0);
            String reason = "当前状态[" + STATUS_TEXT.get(order.getOrderStatus()) + "]不可检票";
            check.setNote(reason);
            result.put("success", false);
            result.put("message", reason);
        }
        ticketCheckMapper.insert(check);
        result.put("order", detail(order.getId()));
        return result;
    }

    @Override
    public IPage<OrderVO> myOrders(Long userId, Integer status, long current, long size) {
        LambdaQueryWrapper<BizOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizOrder::getUserId, userId)
                .eq(status != null, BizOrder::getOrderStatus, status)
                .orderByDesc(BizOrder::getCreateTime);
        IPage<BizOrder> page = this.page(new Page<>(current, size), wrapper);
        return page.convert(o -> toVO(o, false));
    }

    @Override
    public IPage<OrderVO> manageOrders(String orderNo, Integer status, Long scheduleId, long current, long size) {
        LambdaQueryWrapper<BizOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), BizOrder::getOrderNo, orderNo)
                .eq(status != null, BizOrder::getOrderStatus, status)
                .eq(scheduleId != null, BizOrder::getScheduleId, scheduleId)
                .orderByDesc(BizOrder::getCreateTime);
        IPage<BizOrder> page = this.page(new Page<>(current, size), wrapper);
        return page.convert(o -> toVO(o, true));
    }

    @Override
    public OrderVO detail(Long orderId) {
        BizOrder order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return toVO(order, true);
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional(rollbackFor = Exception.class)
    public void autoCancelExpired() {
        List<BizOrder> expired = baseMapper.selectList(new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getOrderStatus, Constants.ORDER_UNPAID)
                .lt(BizOrder::getExpireTime, LocalDateTime.now()));
        for (BizOrder order : expired) {
            order.setOrderStatus(Constants.ORDER_CANCEL);
            order.setCancelTime(LocalDateTime.now());
            baseMapper.updateById(order);
            releaseSeatLock(order);
            log.info("超时未支付订单{}自动取消并释放座位", order.getOrderNo());
        }
    }

    private BizOrder getByOrderNo(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            throw new BusinessException("缺少订单号");
        }
        BizOrder order = baseMapper.selectOne(new LambdaQueryWrapper<BizOrder>().eq(BizOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private String lockKey(Long scheduleId, Long seatId) {
        return Constants.REDIS_SEAT_LOCK + scheduleId + ":" + seatId;
    }

    /** 释放订单相关座位的 Redis 锁 */
    private void releaseSeatLock(BizOrder order) {
        List<BizOrderSeat> oss = orderSeatMapper.selectList(
                new LambdaQueryWrapper<BizOrderSeat>().eq(BizOrderSeat::getOrderId, order.getId()));
        for (BizOrderSeat os : oss) {
            redisService.delete(lockKey(order.getScheduleId(), os.getSeatId()));
        }
    }

    /** 取会员折扣：非会员或冻结会员按原价 */
    private BigDecimal getMemberDiscount(Long userId) {
        BizMember member = memberMapper.selectOne(new LambdaQueryWrapper<BizMember>().eq(BizMember::getUserId, userId));
        if (member == null || member.getStatus() == 0) {
            return BigDecimal.ONE;
        }
        BizMemberLevel level = memberLevelMapper.selectById(member.getLevelId());
        return level == null ? BigDecimal.ONE : level.getDiscount();
    }

    /** 按积分匹配最高可达等级ID */
    private Long refreshLevel(int points) {
        List<BizMemberLevel> levels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<BizMemberLevel>().orderByDesc(BizMemberLevel::getMinPoints));
        for (BizMemberLevel level : levels) {
            if (points >= level.getMinPoints()) {
                return level.getId();
            }
        }
        return 1L;
    }

    private String generateOrderNo() {
        return "CG" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(4);
    }

    /** 订单实体转视图，withUser 控制是否附带用户信息（后台/员工需要） */
    private OrderVO toVO(BizOrder o, boolean withUser) {
        OrderVO vo = new OrderVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setUserId(o.getUserId());
        vo.setScheduleId(o.getScheduleId());
        vo.setMovieId(o.getMovieId());
        vo.setHallId(o.getHallId());
        vo.setTotalAmount(o.getTotalAmount());
        vo.setDiscountAmount(o.getDiscountAmount());
        vo.setPayAmount(o.getPayAmount());
        vo.setSeatCount(o.getSeatCount());
        vo.setOrderStatus(o.getOrderStatus());
        vo.setOrderStatusText(STATUS_TEXT.get(o.getOrderStatus()));
        vo.setPayType(o.getPayType());
        vo.setPayTime(o.getPayTime());
        vo.setTicketTime(o.getTicketTime());
        vo.setExpireTime(o.getExpireTime());
        vo.setCreateTime(o.getCreateTime());
        BizMovie movie = movieMapper.selectById(o.getMovieId());
        if (movie != null) {
            vo.setMovieName(movie.getName());
            vo.setCover(movie.getCover());
        }
        BizHall hall = hallMapper.selectById(o.getHallId());
        if (hall != null) {
            vo.setHallName(hall.getName());
        }
        BizSchedule schedule = scheduleMapper.selectById(o.getScheduleId());
        if (schedule != null) {
            vo.setStartTime(schedule.getStartTime());
            vo.setEndTime(schedule.getEndTime());
            vo.setVersionLang(schedule.getVersionLang());
        }
        List<BizOrderSeat> oss = orderSeatMapper.selectList(
                new LambdaQueryWrapper<BizOrderSeat>().eq(BizOrderSeat::getOrderId, o.getId())
                        .orderByAsc(BizOrderSeat::getRowNo).orderByAsc(BizOrderSeat::getColNo));
        vo.setSeatNames(oss.stream().map(BizOrderSeat::getSeatName).collect(Collectors.toList()));
        if (withUser) {
            SysUser user = userMapper.selectById(o.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            }
        }
        return vo;
    }
}
