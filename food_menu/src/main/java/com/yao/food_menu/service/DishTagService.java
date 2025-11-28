package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.entity.DishTag;

import java.util.List;
import java.util.Map;

/**
 * 菜品标签Service接口
 */
public interface DishTagService extends IService<DishTag> {

    /**
     * 获取所有启用的标签列表
     * @return 标签列表
     */
    List<DishTag> getEnabledTags();

    /**
     * 获取标签图标映射(name -> icon)
     * @return 标签图标映射
     */
    Map<String, String> getTagIconMap();

    /**
     * 根据类型获取标签列表
     * @param type 标签类型
     * @return 标签列表
     */
    List<DishTag> getTagsByType(Integer type);
}

