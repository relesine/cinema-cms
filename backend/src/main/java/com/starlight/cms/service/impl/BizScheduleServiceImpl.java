package com.starlight.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starlight.cms.common.Constants;
import com.starlight.cms.entity.*;
import com.starlight.cms.exception.BusinessException;
import com.starlight.cms.mapper.*;
import com.starlight.cms.service.IBizScheduleService;
import com.starlight.cms.util.RedisService;
import com.starlight.cms.vo.ScheduleVO;
import com.starlight.cms.vo.SeatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizScheduleServiceImpl extends ServiceImpl<BizScheduleMapper, BizSchedule> implements IBizScheduleService {

    private final BizMovieMapper movieMapper;
    private final BizHallMapper hallMapper;
    private final BizSeatMapper seatMapper;
    private final BizOrderMapper orderMapper;
    private final BizOrderSeatMapper orderSeatMapper;
    private final RedisService redisService;

    @Override
    public IPage<ScheduleVO> adminPage(long current, long size, Long movieId, Long hallId,
                                       String keyword, LocalDate date) {
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(movieId != null, BizSchedule::getMovieId, movieId)
                .eq(hallId != null, BizSchedule::getHallId, hallId)
                .ge(date != null, BizSchedule::getStartTime, date == null ? null : date.atStartOfDay())
                .lt(date != null, BizSchedule::getStartTime, date == null ? null : date.plusDays(1).atStartOfDay())
                .orderByAsc(BizSchedule::getStartTime);
        if (StringUtils.hasText(keyword)) {
            // 按电影名过滤：先查出匹配电影 id
            List<Long> movieIds = movieMapper.selectList(
                            new LambdaQueryWrapper<BizMovie>().like(BizMovie::getName, keyword))
                    .stream().map(BizMovie::getId).collect(Collectors.toList());
            if (movieIds.isEmpty()) {
                return new Page<>(current, size, 0);
            }
            wrapper.in(BizSchedule::getMovieId, movieIds);
        }
        IPage<BizSchedule> page = this.page(new Page<>(current, size), wrapper);
        IPage<ScheduleVO> result = page.convert(this::toVO);
        fillRemain(result.getRecords());
        return result;
    }

    @Override
    public List<ScheduleVO> listByMovie(Long movieId, LocalDate date) {
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizSchedule::getStatus, 1)
                .eq(movieId != null, BizSchedule::getMovieId, movieId)
                .ge(date != null, BizSchedule::getStartTime, date == null ? null : date.atStartOfDay())
                .lt(date != null, BizSchedule::getStartTime, date == null ? null : date.plusDays(1).atStartOfDay())
                .ge(date == null, BizSchedule::getStartTime, LocalDateTime.now())
                .orderByAsc(BizSchedule::getStartTime);
        List<ScheduleVO> list = this.list(wrapper).stream().map(this::toVO).collect(Collectors.toList());
        fillRemain(list);
        return list;
    }

    @Override
    public ScheduleVO detail(Long id) {
        BizSchedule schedule = this.getById(id);
        if (schedule == null) {
            throw new BusinessException("场次不存在");
        }
        ScheduleVO vo = toVO(schedule);
        fillRemain(Collections.singletonList(vo));
        return vo;
    }

    @Override
    public Long createSchedule(BizSchedule schedule) {
        prepareAndCheck(schedule, false);
        this.save(schedule);
        return schedule.getId();
    }

    @Override
    public void updateSchedule(BizSchedule schedule) {
        if (schedule.getId() == null) {
            throw new BusinessException("缺少场次ID");
        }
        prepareAndCheck(schedule, true);
        boolean ok = this.updateById(schedule);
        if (!ok) {
            throw new BusinessException("排片已被他人修改，请刷新后重试（乐观锁拦截）");
        }
    }

    /** 补全场次散场时间，并校验同一影厅是否存在时间重叠 */
    private void prepareAndCheck(BizSchedule schedule, boolean isUpdate) {
        if (schedule.getMovieId() == null || schedule.getHallId() == null || schedule.getStartTime() == null) {
            throw new BusinessException("电影、影厅、开场时间不能为空");
        }
        BizMovie movie = movieMapper.selectById(schedule.getMovieId());
        if (movie == null) {
            throw new BusinessException("所选电影不存在");
        }
        if (schedule.getPrice() == null) {
            throw new BusinessException("票价不能为空");
        }
        // 散场时间 = 开场 + 片长，后端统一计算，避免前后端不一致
        schedule.setEndTime(schedule.getStartTime().plusMinutes(movie.getDuration()));
        if (!StringUtils.hasText(schedule.getVersionLang())) {
            schedule.setVersionLang("国语2D");
        }
        if (schedule.getStatus() == null) {
            schedule.setStatus(1);
        }
        // 同影厅时间冲突：已存在场次满足 existing.start < 新.end 且 existing.end > 新.start 即重叠
        List<BizSchedule> sameHall = this.list(new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getHallId, schedule.getHallId())
                .eq(BizSchedule::getStatus, 1));
        for (BizSchedule exist : sameHall) {
            if (isUpdate && exist.getId().equals(schedule.getId())) {
                continue;
            }
            boolean overlap = exist.getStartTime().isBefore(schedule.getEndTime())
                    && exist.getEndTime().isAfter(schedule.getStartTime());
            if (overlap) {
                throw new BusinessException("该影厅在此时间段已有其它排片，时间冲突");
            }
        }
    }

    @Override
    public List<SeatVO> seatMap(Long scheduleId) {
        BizSchedule schedule = this.getById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("场次不存在");
        }
        // 1.该影厅全部物理座位，按排、列排序
        List<BizSeat> seats = seatMapper.selectList(new LambdaQueryWrapper<BizSeat>()
                .eq(BizSeat::getHallId, schedule.getHallId())
                .orderByAsc(BizSeat::getRowNo).orderByAsc(BizSeat::getColNo));
        // 2.该场次有效订单（已支付/已完成=售出；待支付且未超时=锁定）
        List<BizOrder> validOrders = orderMapper.selectList(new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getScheduleId, scheduleId)
                .and(w -> w.eq(BizOrder::getOrderStatus, Constants.ORDER_PAID)
                        .or().eq(BizOrder::getOrderStatus, Constants.ORDER_FINISH)
                        .or(o -> o.eq(BizOrder::getOrderStatus, Constants.ORDER_UNPAID)
                                .gt(BizOrder::getExpireTime, LocalDateTime.now()))));
        Map<Long, Integer> orderStatusMap = validOrders.stream()
                .collect(Collectors.toMap(BizOrder::getId, BizOrder::getOrderStatus));
        // 3.订单占用的座位 -> 售出/锁定
        Map<Long, Integer> occupied = new HashMap<>();
        if (!orderStatusMap.isEmpty()) {
            List<BizOrderSeat> oss = orderSeatMapper.selectList(new LambdaQueryWrapper<BizOrderSeat>()
                    .eq(BizOrderSeat::getScheduleId, scheduleId)
                    .in(BizOrderSeat::getOrderId, orderStatusMap.keySet()));
            for (BizOrderSeat os : oss) {
                Integer st = orderStatusMap.get(os.getOrderId());
                // 已支付/已完成=2售出，待支付=1锁定
                occupied.put(os.getSeatId(),
                        (st == Constants.ORDER_PAID || st == Constants.ORDER_FINISH) ? 2 : 1);
            }
        }
        // 4.组装视图，同时叠加 Redis 瞬时锁
        List<SeatVO> result = new ArrayList<>();
        for (BizSeat seat : seats) {
            SeatVO vo = new SeatVO();
            vo.setSeatId(seat.getId());
            vo.setRowNo(seat.getRowNo());
            vo.setColNo(seat.getColNo());
            vo.setSeatName(seat.getSeatName());
            vo.setSeatType(seat.getSeatType());
            vo.setSeatStatus(seat.getStatus());
            vo.setPrice(schedule.getPrice());
            int sell;
            if (seat.getStatus() != null && seat.getStatus() == 0) {
                sell = 3; // 维修
            } else if (occupied.containsKey(seat.getId())) {
                sell = occupied.get(seat.getId());
            } else if (Boolean.TRUE.equals(redisService.exists(lockKey(scheduleId, seat.getId())))) {
                sell = 1; // Redis 选座瞬时锁
            } else {
                sell = 0; // 可选
            }
            vo.setSellStatus(sell);
            result.add(vo);
        }
        return result;
    }

    private String lockKey(Long scheduleId, Long seatId) {
        return Constants.REDIS_SEAT_LOCK + scheduleId + ":" + seatId;
    }

    /** 实体转视图（补电影名、影厅名） */
    private ScheduleVO toVO(BizSchedule s) {
        ScheduleVO vo = new ScheduleVO();
        vo.setId(s.getId());
        vo.setMovieId(s.getMovieId());
        vo.setHallId(s.getHallId());
        vo.setStartTime(s.getStartTime());
        vo.setEndTime(s.getEndTime());
        vo.setPrice(s.getPrice());
        vo.setVersionLang(s.getVersionLang());
        vo.setStatus(s.getStatus());
        BizMovie movie = movieMapper.selectById(s.getMovieId());
        if (movie != null) {
            vo.setMovieName(movie.getName());
            vo.setCover(movie.getCover());
            vo.setHallType(null);
        }
        BizHall hall = hallMapper.selectById(s.getHallId());
        if (hall != null) {
            vo.setHallName(hall.getName());
            vo.setHallType(hall.getHallType());
            vo.setTotalSeats(hall.getSeatCount());
        }
        return vo;
    }

    /** 批量填充剩余座位（一条聚合查询，避免N+1） */
    private void fillRemain(List<ScheduleVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> scheduleIds = records.stream().map(ScheduleVO::getId).collect(Collectors.toList());
        List<BizOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<BizOrder>()
                .in(BizOrder::getScheduleId, scheduleIds)
                .and(w -> w.eq(BizOrder::getOrderStatus, Constants.ORDER_PAID)
                        .or().eq(BizOrder::getOrderStatus, Constants.ORDER_FINISH)
                        .or(o -> o.eq(BizOrder::getOrderStatus, Constants.ORDER_UNPAID)
                                .gt(BizOrder::getExpireTime, LocalDateTime.now()))));
        Map<Long, Long> orderSchedule = orders.stream()
                .collect(Collectors.toMap(BizOrder::getId, BizOrder::getScheduleId));
        Map<Long, Integer> lockCount = new HashMap<>();
        if (!orderSchedule.isEmpty()) {
            List<BizOrderSeat> oss = orderSeatMapper.selectList(new LambdaQueryWrapper<BizOrderSeat>()
                    .in(BizOrderSeat::getOrderId, orderSchedule.keySet()));
            for (BizOrderSeat os : oss) {
                Long sid = orderSchedule.get(os.getOrderId());
                lockCount.merge(sid, 1, Integer::sum);
            }
        }
        for (ScheduleVO vo : records) {
            int locked = lockCount.getOrDefault(vo.getId(), 0);
            int total = vo.getTotalSeats() == null ? 0 : vo.getTotalSeats();
            vo.setRemainSeats(Math.max(0, total - locked));
        }
    }
}
