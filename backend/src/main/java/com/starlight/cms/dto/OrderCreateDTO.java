package com.starlight.cms.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/** 创建订单（下单）入参：场次 + 选中的座位ID集合 */
@Data
public class OrderCreateDTO {
    @NotNull(message = "缺少场次信息")
    private Long scheduleId;
    @NotEmpty(message = "请至少选择一个座位")
    private List<Long> seatIds;
}
