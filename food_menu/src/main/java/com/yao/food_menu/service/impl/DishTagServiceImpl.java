package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.entity.DishTag;
import com.yao.food_menu.mapper.DishTagMapper;
import com.yao.food_menu.service.DishTagService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜品标签Service实现类
 */
@Service
public class DishTagServiceImpl extends ServiceImpl<DishTagMapper, DishTag> implements DishTagService {

    @Override
    public List<DishTag> getEnabledTags() {
        LambdaQueryWrapper<DishTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishTag::getStatus, 1)
                .orderByAsc(DishTag::getType)
                .orderByAsc(DishTag::getSort);
        return this.list(queryWrapper);
    }

    @Override
    public Map<String, String> getTagIconMap() {
        List<DishTag> tags = getEnabledTags();
        return tags.stream()
                .collect(Collectors.toMap(
                        DishTag::getName,
                        tag -> tag.getIcon() != null ? tag.getIcon() : "🔸",
                        (existing, replacement) -> existing
                ));
    }

    @Override
    public List<DishTag> getTagsByType(Integer type) {
        LambdaQueryWrapper<DishTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishTag::getStatus, 1);
        if (type != null) {
            queryWrapper.eq(DishTag::getType, type);
        }
        queryWrapper.orderByAsc(DishTag::getSort);
        return this.list(queryWrapper);
    }
}

