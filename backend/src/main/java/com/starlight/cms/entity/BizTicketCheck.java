package com.starlight.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_ticket_check")
public class BizTicketCheck {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long scheduleId;
    private Long staffId;
    private Integer result;
    private String note;
    private LocalDateTime checkTime;
}
