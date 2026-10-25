package com.starlight.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starlight.cms.entity.BizMovie;
import com.starlight.cms.mapper.BizMovieMapper;
import com.starlight.cms.service.IBizMovieService;
import org.springframework.stereotype.Service;

@Service
public class BizMovieServiceImpl extends ServiceImpl<BizMovieMapper, BizMovie> implements IBizMovieService {
}
