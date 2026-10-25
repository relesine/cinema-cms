package com.starlight.cms.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 选座页单个座位视图
 * sellStatus：0可选 1被他人锁定 2已售出 3维修不可选
 */
@Data
public class SeatVO {
    private Long seatId;
    private Integer rowNo;
    private Integer colNo;
    private String seatName;
    private Integer seatType;
    /** 物理座位状态：1正常 0维修 */
    private Integer seatStatus;
    private Integer sellStatus;
    private BigDecimal price;
}
