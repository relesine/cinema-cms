package com.starlight.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starlight.cms.entity.BizHall;

public interface IBizHallService extends IService<BizHall> {

    /** 新增/修改影厅后，按行列自动生成（或补齐）座位 */
    void rebuildSeats(BizHall hall);
}
