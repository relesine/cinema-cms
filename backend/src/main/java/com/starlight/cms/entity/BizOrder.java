package com.starlight.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.starlight.cms.common.BaseLogicEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_order")
public class BizOrder extends BaseLogicEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long scheduleId;
    private Long movieId;
    private Long hallId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Integer seatCount;
    /** 0待支付 1已支付 2已退票 3已取消 4已完成 */
    private Integer orderStatus;
    private Integer payType;
    private LocalDateTime payTime;
    private LocalDateTime ticketTime;
    private LocalDateTime cancelTime;
    private LocalDateTime expireTime;
    private String remark;
}
