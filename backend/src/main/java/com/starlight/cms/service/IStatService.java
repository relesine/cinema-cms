package com.starlight.cms.service;

import java.util.List;
import java.util.Map;

/** 票房数据统计服务（管理后台大盘） */
public interface IStatService {

    /** 核心指标概览 */
    Map<String, Object> overview();

    /** 各电影票房排行 */
    List<Map<String, Object>> movieRank();

    /** 近 N 天票房趋势 */
    List<Map<String, Object>> salesTrend(int days);
}
