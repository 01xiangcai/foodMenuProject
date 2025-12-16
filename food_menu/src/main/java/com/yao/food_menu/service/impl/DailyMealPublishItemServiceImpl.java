package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.entity.DailyMealPublishItem;
import com.yao.food_menu.mapper.DailyMealPublishItemMapper;
import com.yao.food_menu.service.DailyMealPublishItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 餐次发布菜品记录Service实现
 */
@Service
public class DailyMealPublishItemServiceImpl extends ServiceImpl<DailyMealPublishItemMapper, DailyMealPublishItem>
        implements DailyMealPublishItemService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchInsert(List<DailyMealPublishItem> publishItems) {
        if (publishItems != null && !publishItems.isEmpty()) {
            saveBatch(publishItems);
        }
    }

    @Override
    public List<DailyMealPublishItem> getByDailyMealOrderId(Long dailyMealOrderId) {
        LambdaQueryWrapper<DailyMealPublishItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyMealPublishItem::getDailyMealOrderId, dailyMealOrderId);
        wrapper.orderByAsc(DailyMealPublishItem::getCreateTime);
        return list(wrapper);
    }
}
