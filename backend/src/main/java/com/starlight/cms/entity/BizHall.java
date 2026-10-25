package com.starlight.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.starlight.cms.common.BaseLogicEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_hall")
public class BizHall extends BaseLogicEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** 1普通 2巨幕 3VIP 4儿童 */
    private Integer hallType;
    private Integer rowCount;
    private Integer colCount;
    private Integer seatCount;
    private Integer status;
}
