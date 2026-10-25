package com.starlight.cms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 订单视图（含电影、影厅、场次、座位名等展示信息） */
@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private String nickname;
    private Long scheduleId;
    private Long movieId;
    private String movieName;
    private String cover;
    private Long hallId;
    private String hallName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String versionLang;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Integer seatCount;
    private Integer orderStatus;
    private String orderStatusText;
    private Integer payType;
    private LocalDateTime payTime;
    private LocalDateTime ticketTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    /** 座位名集合，如 3排5座 */
    private List<String> seatNames;
}
