package com.starlight.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.starlight.cms.common.BaseTimeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_member")
public class BizMember extends BaseTimeEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long levelId;
    private Integer points;
    private BigDecimal balance;
    private BigDecimal totalConsume;
    private LocalDateTime openTime;
    private Integer status;
}
