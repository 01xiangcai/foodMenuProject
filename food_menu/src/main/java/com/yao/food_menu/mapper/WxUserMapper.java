package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.entity.WxUser;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * WxUser Mapper
 */
@Mapper
public interface WxUserMapper extends BaseMapper<WxUser> {

    /**
     * 根据ID查询用户（不应用家庭过滤）
     * 用于WebSocket等无法设置FamilyContext的场景
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM wx_user WHERE id = #{id} AND deleted = 0")
    WxUser selectByIdWithoutFamilyFilter(Long id);
}
