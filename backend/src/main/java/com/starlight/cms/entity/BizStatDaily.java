package com.starlight.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("biz_stat_daily")
public class BizStatDaily {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Long movieId;
    private Integer orderCount;
    private Integer ticketCount;
    private BigDecimal amount;
}
