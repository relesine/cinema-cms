package com.starlight.cms.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** 模拟支付入参 */
@Data
public class PayDTO {
    @NotBlank(message = "缺少订单号")
    private String orderNo;
    /** 1模拟微信 2模拟支付宝 3会员余额 */
    @NotNull(message = "请选择支付方式")
    private Integer payType;
}
