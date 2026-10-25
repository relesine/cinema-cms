package com.starlight.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starlight.cms.entity.BizMemberLevel;
import com.starlight.cms.mapper.BizMemberLevelMapper;
import com.starlight.cms.service.IBizMemberLevelService;
import org.springframework.stereotype.Service;

@Service
public class BizMemberLevelServiceImpl extends ServiceImpl<BizMemberLevelMapper, BizMemberLevel>
        implements IBizMemberLevelService {
}
