package com.starlight.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starlight.cms.entity.BizMember;
import com.starlight.cms.entity.BizMemberLevel;
import com.starlight.cms.entity.SysUser;
import com.starlight.cms.exception.BusinessException;
import com.starlight.cms.mapper.BizMemberLevelMapper;
import com.starlight.cms.mapper.BizMemberMapper;
import com.starlight.cms.mapper.SysUserMapper;
import com.starlight.cms.service.IBizMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizMemberServiceImpl extends ServiceImpl<BizMemberMapper, BizMember> implements IBizMemberService {

    private final BizMemberLevelMapper levelMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizMember openMember(Long userId) {
        BizMember exist = this.getOne(new LambdaQueryWrapper<BizMember>().eq(BizMember::getUserId, userId));
        if (exist != null) {
            return exist;
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        BizMember member = new BizMember();
        member.setUserId(userId);
        member.setLevelId(1L);
        member.setPoints(0);
        member.setBalance(BigDecimal.ZERO);
        member.setTotalConsume(BigDecimal.ZERO);
        member.setOpenTime(LocalDateTime.now());
        member.setStatus(1);
        this.save(member);
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充值金额必须大于0");
        }
        BizMember member = this.getOne(new LambdaQueryWrapper<BizMember>().eq(BizMember::getUserId, userId));
        if (member == null) {
            member = openMember(userId);
        }
        member.setBalance(member.getBalance().add(amount));
        this.updateById(member);
    }

    @Override
    public Map<String, Object> memberCenter(Long userId) {
        Map<String, Object> map = new HashMap<>();
        SysUser user = userMapper.selectById(userId);
        BizMember member = this.getOne(new LambdaQueryWrapper<BizMember>().eq(BizMember::getUserId, userId));
        map.put("user", user);
        map.put("opened", member != null);
        map.put("member", member);
        if (member != null) {
            map.put("level", levelMapper.selectById(member.getLevelId()));
        } else {
            map.put("levels", levelMapper.selectList(null));
        }
        return map;
    }

    @Override
    public IPage<Map<String, Object>> adminPage(long current, long size, String keyword) {
        IPage<BizMember> page = this.page(new Page<>(current, size),
                new LambdaQueryWrapper<BizMember>().orderByDesc(BizMember::getOpenTime));
        List<Long> userIds = page.getRecords().stream().map(BizMember::getUserId).collect(Collectors.toList());
        Map<Long, SysUser> userMap = userIds.isEmpty() ? new HashMap<>()
                : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        Map<Long, BizMemberLevel> levelMap = levelMapper.selectList(null).stream()
                .collect(Collectors.toMap(BizMemberLevel::getId, l -> l));
        IPage<Map<String, Object>> result = page.convert(m -> {
            Map<String, Object> row = new HashMap<>();
            row.put("member", m);
            SysUser u = userMap.get(m.getUserId());
            row.put("username", u == null ? null : u.getUsername());
            row.put("nickname", u == null ? null : u.getNickname());
            row.put("phone", u == null ? null : u.getPhone());
            BizMemberLevel level = levelMap.get(m.getLevelId());
            row.put("levelName", level == null ? null : level.getLevelName());
            row.put("discount", level == null ? null : level.getDiscount());
            if (StringUtils.hasText(keyword) && row.get("username") != null
                    && !row.get("username").toString().contains(keyword)
                    && (row.get("nickname") == null || !row.get("nickname").toString().contains(keyword))) {
                return null;
            }
            return row;
        });
        // keyword 在内存过滤后剔除 null
        result.getRecords().removeIf(Objects::isNull);
        return result;
    }
}
