package com.starlight.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.starlight.cms.common.BaseLogicEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_seat")
public class BizSeat extends BaseLogicEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long hallId;
    private Integer rowNo;
    private Integer colNo;
    private String seatName;
    /** 0普通 1优选 2VIP 3亲子 */
    private Integer seatType;
    private Integer status;
}
