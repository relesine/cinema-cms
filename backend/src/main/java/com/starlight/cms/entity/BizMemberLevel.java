package com.starlight.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.starlight.cms.common.BaseTimeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_member_level")
public class BizMemberLevel extends BaseTimeEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String levelName;
    private Integer minPoints;
    private BigDecimal discount;
    private String description;
}
