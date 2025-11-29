package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.FamilyDto;
import com.yao.food_menu.dto.FamilyQueryDto;
import com.yao.food_menu.entity.Family;

/**
 * 家庭服务接口
 */
public interface FamilyService extends IService<Family> {

    /**
     * 分页查询家庭列表
     *
     * @param queryDto 查询参数
     * @return 分页结果
     */
    Page<Family> pageFamilies(FamilyQueryDto queryDto);

    /**
     * 创建家庭
     *
     * @param familyDto 家庭信息
     * @return 创建的家庭
     */
    Family createFamily(FamilyDto familyDto);

    /**
     * 更新家庭信息
     *
     * @param familyDto 家庭信息
     */
    void updateFamily(FamilyDto familyDto);

    /**
     * 删除家庭
     *
     * @param id 家庭ID
     */
    void deleteFamily(Long id);

    /**
     * 通过邀请码查询家庭
     *
     * @param inviteCode 邀请码
     * @return 家庭信息
     */
    Family getByInviteCode(String inviteCode);

    /**
     * 生成唯一邀请码
     *
     * @return 邀请码
     */
    String generateInviteCode();
}
