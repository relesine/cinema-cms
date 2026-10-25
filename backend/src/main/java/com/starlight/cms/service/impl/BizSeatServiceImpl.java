package com.starlight.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starlight.cms.entity.BizSeat;
import com.starlight.cms.mapper.BizSeatMapper;
import com.starlight.cms.service.IBizSeatService;
import org.springframework.stereotype.Service;

@Service
public class BizSeatServiceImpl extends ServiceImpl<BizSeatMapper, BizSeat> implements IBizSeatService {
}
