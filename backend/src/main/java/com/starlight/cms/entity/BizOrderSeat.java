package com.starlight.cms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_order_seat")
public class BizOrderSeat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long scheduleId;
    private Long seatId;
    private Integer rowNo;
    private Integer colNo;
    private String seatName;
    private BigDecimal price;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
