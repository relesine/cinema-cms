package com.starlight.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starlight.cms.entity.BizMember;

import java.math.BigDecimal;
import java.util.Map;

public interface IBizMemberService extends IService<BizMember> {

    /** 用户开通会员（默认普通等级），已开通则直接返回 */
    BizMember openMember(Long userId);

    /** 会员充值（模拟） */
    void recharge(Long userId, BigDecimal amount);

    /** 会员中心：会员信息 + 等级信息 + 用户信息 */
    Map<String, Object> memberCenter(Long userId);

    /** 后台会员分页（带用户昵称、等级名） */
    IPage<Map<String, Object>> adminPage(long current, long size, String keyword);
}
