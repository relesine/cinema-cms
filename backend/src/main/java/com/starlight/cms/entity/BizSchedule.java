package com.starlight.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.starlight.cms.common.BaseLogicEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_schedule")
public class BizSchedule extends BaseLogicEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long movieId;
    private Long hallId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private String versionLang;
    private Integer status;
    /** 乐观锁版本号 */
    @Version
    private Integer version;
}
