package com.starlight.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starlight.cms.dto.OrderCreateDTO;
import com.starlight.cms.dto.PayDTO;
import com.starlight.cms.vo.OrderVO;

import java.util.Map;

/**
 * 订单核心交易服务：下单(锁座防超卖)、模拟支付、取消、退票、检票、超时释放
 */
public interface IBizOrderService {

    /** 创建订单（原子锁座，返回订单详情） */
    OrderVO createOrder(Long userId, OrderCreateDTO dto);

    /** 模拟支付 */
    OrderVO payOrder(Long userId, PayDTO dto);

    /** 用户主动取消待支付订单 */
    void cancelOrder(Long userId, String orderNo);

    /** 退票（开场前限制时间之外可退） */
    void refundOrder(Long userId, String orderNo);

    /** 员工检票核销，返回核销结果信息 */
    Map<String, Object> checkTicket(Long staffId, String orderNo);

    /** 我的订单分页 */
    IPage<OrderVO> myOrders(Long userId, Integer status, long current, long size);

    /** 员工/后台订单分页（可按订单号、状态过滤） */
    IPage<OrderVO> manageOrders(String orderNo, Integer status, Long scheduleId, long current, long size);

    /** 订单详情 */
    OrderVO detail(Long orderId);

    /** 定时任务：自动取消超时未支付订单并释放座位锁 */
    void autoCancelExpired();
}
