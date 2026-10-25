package com.starlight.cms.common;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公共字段基类：仅含创建/更新时间（适用于只有这两个通用字段的表）
 * 自动填充由 MyMetaObjectHandler 完成
 */
@Data
public class BaseTimeEntity implements Serializable {

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
