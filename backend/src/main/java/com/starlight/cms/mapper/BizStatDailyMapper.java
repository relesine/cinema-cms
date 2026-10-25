package com.starlight.cms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starlight.cms.entity.BizStatDaily;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface BizStatDailyMapper extends BaseMapper<BizStatDaily> {

    /** 各电影累计票房排行（统计已支付及已完成订单） */
    @Select("SELECT m.name AS name, COALESCE(SUM(o.pay_amount),0) AS amount, " +
            "COALESCE(SUM(o.seat_count),0) AS tickets " +
            "FROM biz_movie m LEFT JOIN biz_order o ON o.movie_id=m.id " +
            "AND o.order_status IN (1,4) AND o.is_deleted=0 " +
            "WHERE m.is_deleted=0 GROUP BY m.id,m.name ORDER BY amount DESC")
    List<Map<String,Object>> movieBoxOffice();
}
