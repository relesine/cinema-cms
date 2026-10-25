package com.starlight.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.starlight.cms.common.BaseLogicEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_notice")
public class BizNotice extends BaseLogicEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private Integer noticeType;
    private Integer status;
    private LocalDateTime publishTime;
}
