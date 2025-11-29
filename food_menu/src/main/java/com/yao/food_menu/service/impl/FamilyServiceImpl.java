package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.dto.FamilyDto;
import com.yao.food_menu.dto.FamilyQueryDto;
import com.yao.food_menu.entity.Family;
import com.yao.food_menu.mapper.FamilyMapper;
import com.yao.food_menu.service.FamilyService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 家庭服务实现类
 */
@Service
public class FamilyServiceImpl extends ServiceImpl<FamilyMapper, Family> implements FamilyService {

    @Override
    public Page<Family> pageFamilies(FamilyQueryDto queryDto) {
        Page<Family> page = new Page<>(queryDto.getPage(), queryDto.getPageSize());

        LambdaQueryWrapper<Family> queryWrapper = new LambdaQueryWrapper<>();

        // 权限控制：非超级管理员只能查看自己的家庭
        Integer currentUserRole = com.yao.food_menu.common.context.FamilyContext.getUserRole();
        Long currentUserFamilyId = com.yao.food_menu.common.context.FamilyContext.getFamilyId();
        boolean isSuperAdmin = (currentUserRole != null && currentUserRole == 2);
        
        if (!isSuperAdmin && currentUserFamilyId != null) {
            // 非超级管理员只能查看自己的家庭
            queryWrapper.eq(Family::getId, currentUserFamilyId);
        }

        // 按名称模糊查询
        if (StringUtils.hasText(queryDto.getName())) {
            queryWrapper.like(Family::getName, queryDto.getName());
        }

        // 按邀请码查询
        if (StringUtils.hasText(queryDto.getInviteCode())) {
            queryWrapper.eq(Family::getInviteCode, queryDto.getInviteCode());
        }

        // 按状态查询
        if (queryDto.getStatus() != null) {
            queryWrapper.eq(Family::getStatus, queryDto.getStatus());
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(Family::getCreateTime);

        return this.page(page, queryWrapper);
    }

    @Override
    public Family createFamily(FamilyDto familyDto) {
        Family family = new Family();
        BeanUtils.copyProperties(familyDto, family);

        // 生成唯一邀请码
        if (!StringUtils.hasText(family.getInviteCode())) {
            family.setInviteCode(generateInviteCode());
        }

        // 默认状态为正常
        if (family.getStatus() == null) {
            family.setStatus(1);
        }

        this.save(family);
        return family;
    }

    @Override
    public void updateFamily(FamilyDto familyDto) {
        if (familyDto.getId() == null) {
            throw new RuntimeException("家庭ID不能为空");
        }

        Family family = this.getById(familyDto.getId());
        if (family == null) {
            throw new RuntimeException("家庭不存在");
        }

        // 权限控制
        Integer currentUserRole = com.yao.food_menu.common.context.FamilyContext.getUserRole();
        Long currentUserFamilyId = com.yao.food_menu.common.context.FamilyContext.getFamilyId();
        boolean isSuperAdmin = (currentUserRole != null && currentUserRole == 2);
        
        // 非超级管理员只能修改自己的家庭
        if (!isSuperAdmin) {
            if (currentUserFamilyId == null || !family.getId().equals(currentUserFamilyId)) {
                throw new RuntimeException("无权限修改其他家庭信息");
            }
        }

        // 复制属性（排除null值）
        if (StringUtils.hasText(familyDto.getName())) {
            family.setName(familyDto.getName());
        }
        if (StringUtils.hasText(familyDto.getDescription())) {
            family.setDescription(familyDto.getDescription());
        }
        
        // 只有超级管理员可以修改状态
        if (familyDto.getStatus() != null) {
            if (isSuperAdmin) {
                family.setStatus(familyDto.getStatus());
            }
            // 非超级管理员不允许修改状态，忽略此字段（不抛出异常，只是不更新状态）
        }

        this.updateById(family);
    }

    @Override
    public void deleteFamily(Long id) {
        if (id == null) {
            throw new RuntimeException("家庭ID不能为空");
        }

        Family family = this.getById(id);
        if (family == null) {
            throw new RuntimeException("家庭不存在");
        }

        // 权限控制：只有超级管理员可以删除家庭
        Integer currentUserRole = com.yao.food_menu.common.context.FamilyContext.getUserRole();
        boolean isSuperAdmin = (currentUserRole != null && currentUserRole == 2);
        
        if (!isSuperAdmin) {
            throw new RuntimeException("只有超级管理员可以删除家庭");
        }

        // TODO: 检查是否有关联数据，如果有则不允许删除

        this.removeById(id);
    }

    @Override
    public Family getByInviteCode(String inviteCode) {
        if (!StringUtils.hasText(inviteCode)) {
            throw new RuntimeException("邀请码不能为空");
        }

        LambdaQueryWrapper<Family> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Family::getInviteCode, inviteCode);

        return this.getOne(queryWrapper);
    }

    @Override
    public String generateInviteCode() {
        // 生成8位随机邀请码
        String code;
        int maxAttempts = 10;
        int attempts = 0;

        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            attempts++;

            if (attempts >= maxAttempts) {
                throw new RuntimeException("生成邀请码失败，请重试");
            }
        } while (getByInviteCode(code) != null);

        return code;
    }

    @Override
    public java.util.List<Family> listAllFamilies() {
        LambdaQueryWrapper<Family> queryWrapper = new LambdaQueryWrapper<>();
        
        // 权限控制：非超级管理员只能查看自己的家庭
        Integer currentUserRole = com.yao.food_menu.common.context.FamilyContext.getUserRole();
        Long currentUserFamilyId = com.yao.food_menu.common.context.FamilyContext.getFamilyId();
        boolean isSuperAdmin = (currentUserRole != null && currentUserRole == 2);
        
        if (!isSuperAdmin && currentUserFamilyId != null) {
            // 非超级管理员只能查看自己的家庭
            queryWrapper.eq(Family::getId, currentUserFamilyId);
        } else {
            // 只查询启用状态的家庭（超级管理员）
            queryWrapper.eq(Family::getStatus, 1);
        }
        
        // 按名称排序
        queryWrapper.orderByAsc(Family::getName);
        return this.list(queryWrapper);
    }

    @Override
    public Family getFamilyById(Long id) {
        if (id == null) {
            throw new RuntimeException("家庭ID不能为空");
        }
        
        Family family = this.getById(id);
        if (family == null) {
            throw new RuntimeException("家庭不存在");
        }
        
        // 权限控制：非超级管理员只能查看自己的家庭
        Integer currentUserRole = com.yao.food_menu.common.context.FamilyContext.getUserRole();
        Long currentUserFamilyId = com.yao.food_menu.common.context.FamilyContext.getFamilyId();
        boolean isSuperAdmin = (currentUserRole != null && currentUserRole == 2);
        
        if (!isSuperAdmin) {
            if (currentUserFamilyId == null || !family.getId().equals(currentUserFamilyId)) {
                throw new RuntimeException("无权限查看其他家庭信息");
            }
        }
        
        return family;
    }
}
