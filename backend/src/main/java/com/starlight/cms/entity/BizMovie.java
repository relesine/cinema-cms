package com.starlight.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.starlight.cms.common.BaseLogicEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_movie")
public class BizMovie extends BaseLogicEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String nameEn;
    private String cover;
    private String director;
    private String actors;
    private String genre;
    private Integer duration;
    private LocalDate releaseDate;
    private String region;
    private String language;
    private String synopsis;
    private BigDecimal score;
    /** 1热映 0待映 2下架 */
    private Integer movieStatus;
}
