package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.entity.OrderDetail;
import com.yao.food_menu.mapper.OrderDetailMapper;
import com.yao.food_menu.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
