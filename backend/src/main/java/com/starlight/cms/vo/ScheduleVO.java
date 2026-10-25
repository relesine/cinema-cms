package com.starlight.cms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 场次列表视图：冗余电影名、影厅名与剩余座位数，前端无需多次查询 */
@Data
public class ScheduleVO {
    private Long id;
    private Long movieId;
    private String movieName;
    private String cover;
    private Long hallId;
    private String hallName;
    private Integer hallType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private String versionLang;
    private Integer status;
    private Integer totalSeats;
    private Integer remainSeats;
}
