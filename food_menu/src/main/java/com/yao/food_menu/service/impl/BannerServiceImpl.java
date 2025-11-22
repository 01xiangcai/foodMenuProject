package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.entity.Banner;
import com.yao.food_menu.mapper.BannerMapper;
import com.yao.food_menu.service.BannerService;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {
}

