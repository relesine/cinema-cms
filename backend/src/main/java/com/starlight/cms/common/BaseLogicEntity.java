package com.starlight.cms.common;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 逻辑删除基类：创建时间 + 更新时间 + 逻辑删除标记
 * 继承它的实体对应数据表必须含有 create_time / update_time / is_deleted 三列
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BaseLogicEntity extends BaseTimeEntity implements Serializable {

    /** 逻辑删除：0正常 1已删除，@TableLogic 让 MP 查询自动追加 is_deleted=0 */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;
}
