package com.starlight.cms.common;

/**
 * 系统常量：Redis key 前缀、订单状态、角色编码等，避免代码里写魔法值
 */
public class Constants {

    private Constants() {}

    /** 角色编码 */
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_STAFF = "ROLE_STAFF";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /** 订单状态：0待支付 1已支付 2已退票 3已取消 4已完成(已检票) */
    public static final int ORDER_UNPAID = 0;
    public static final int ORDER_PAID = 1;
    public static final int ORDER_REFUND = 2;
    public static final int ORDER_CANCEL = 3;
    public static final int ORDER_FINISH = 4;

    /** Redis：登录令牌前缀，值为用户ID */
    public static final String REDIS_TOKEN = "cms:login:token:";
    /** Redis：选座锁定前缀，key=前缀:场次ID:座位ID，value=订单号/用户，过期自动释放 */
    public static final String REDIS_SEAT_LOCK = "cms:seat:lock:";
    /** Redis：热门电影缓存前缀 */
    public static final String REDIS_MOVIE_HOT = "cms:movie:hot";
    public static final String REDIS_NOTICE = "cms:notice:list";

    /** 登录防爆破——失败计数前缀（值=连续失败次数） */
    public static final String REDIS_LOGIN_FAIL = "cms:login:fail:";
    /** 登录防爆破——锁定前缀（存在即锁定中） */
    public static final String REDIS_LOGIN_LOCK = "cms:login:lock:";
    /** 连续失败次数阈值：达到即锁定 */
    public static final int LOGIN_FAIL_LIMIT = 5;
    /** 锁定时长（分钟） */
    public static final long LOGIN_LOCK_MINUTES = 10L;
    /** 失败计数保留时长（分钟） */
    public static final long LOGIN_FAIL_MINUTES = 10L;

    /** 选座锁定时长（分钟），与订单支付超时一致 */
    public static final long SEAT_LOCK_MINUTES = 15L;
    /** 单次最多购票数（防黄牛/防误选） */
    public static final int MAX_SEATS_PER_ORDER = 6;
}
