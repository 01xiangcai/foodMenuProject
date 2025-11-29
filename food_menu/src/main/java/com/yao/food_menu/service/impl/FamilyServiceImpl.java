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

        // 复制属性（排除null值）
        if (StringUtils.hasText(familyDto.getName())) {
            family.setName(familyDto.getName());
        }
        if (StringUtils.hasText(familyDto.getDescription())) {
            family.setDescription(familyDto.getDescription());
        }
        if (familyDto.getStatus() != null) {
            family.setStatus(familyDto.getStatus());
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
}
