package com.starlight.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starlight.cms.entity.BizNotice;
import com.starlight.cms.mapper.BizNoticeMapper;
import com.starlight.cms.service.IBizNoticeService;
import org.springframework.stereotype.Service;

@Service
public class BizNoticeServiceImpl extends ServiceImpl<BizNoticeMapper, BizNotice> implements IBizNoticeService {
}
