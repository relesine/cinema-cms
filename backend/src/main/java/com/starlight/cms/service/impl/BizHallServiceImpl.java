package com.starlight.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starlight.cms.entity.BizHall;
import com.starlight.cms.entity.BizSeat;
import com.starlight.cms.mapper.BizHallMapper;
import com.starlight.cms.mapper.BizSeatMapper;
import com.starlight.cms.service.IBizHallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BizHallServiceImpl extends ServiceImpl<BizHallMapper, BizHall> implements IBizHallService {

    private final BizSeatMapper seatMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rebuildSeats(BizHall hall) {
        // 学习项目简化处理：重建前删除旧座位，再按 行x列 全量生成
        seatMapper.delete(new LambdaQueryWrapper<BizSeat>().eq(BizSeat::getHallId, hall.getId()));
        List<BizSeat> seats = new ArrayList<>();
        for (int r = 1; r <= hall.getRowCount(); r++) {
            for (int c = 1; c <= hall.getColCount(); c++) {
                BizSeat seat = new BizSeat();
                seat.setHallId(hall.getId());
                seat.setRowNo(r);
                seat.setColNo(c);
                seat.setSeatName(r + "排" + c + "座");
                // VIP厅全部VIP座，其余默认普通座
                seat.setSeatType(hall.getHallType() != null && hall.getHallType() == 3 ? 2 : 0);
                seat.setStatus(1);
                seats.add(seat);
            }
        }
        seats.forEach(seatMapper::insert);
        hall.setSeatCount(seats.size());
        this.updateById(hall);
    }
}
