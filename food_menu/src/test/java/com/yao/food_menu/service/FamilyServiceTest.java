package com.yao.food_menu.service;

import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.dto.FamilyDto;
import com.yao.food_menu.entity.Family;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 家庭服务测试类
 */
@SpringBootTest
@Transactional
public class FamilyServiceTest {

    @Autowired
    private FamilyService familyService;

    @BeforeEach
    public void setUp() {
        // 设置超级管理员上下文
        FamilyContext.setUserId(1L);
        FamilyContext.setUserRole(2); // 超级管理员
    }

    @AfterEach
    public void tearDown() {
        FamilyContext.clear();
    }

    @Test
    public void testCreateFamily() {
        // 创建家庭
        FamilyDto familyDto = new FamilyDto();
        familyDto.setName("测试家庭");
        familyDto.setDescription("这是一个测试家庭");
        familyDto.setStatus(1);

        Family created = familyService.createFamily(familyDto);

        // 验证
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("测试家庭", created.getName());
        assertNotNull(created.getInviteCode());
        assertEquals(8, created.getInviteCode().length());

        System.out.println("✅ 创建家庭测试通过");
        System.out.println("   家庭ID: " + created.getId());
        System.out.println("   邀请码: " + created.getInviteCode());
    }

    @Test
    public void testGetFamilyById() {
        // 查询默认家庭
        Family family = familyService.getById(1L);

        // 验证
        assertNotNull(family);
        assertEquals(1L, family.getId());
        assertEquals("默认家庭", family.getName());

        System.out.println("✅ 查询家庭测试通过");
        System.out.println("   家庭名称: " + family.getName());
    }

    @Test
    public void testGetFamilyByInviteCode() {
        // 通过邀请码查询
        Family family = familyService.getByInviteCode("DEFAULT001");

        // 验证
        assertNotNull(family);
        assertEquals("默认家庭", family.getName());

        System.out.println("✅ 邀请码查询测试通过");
        System.out.println("   邀请码: DEFAULT001");
    }

    @Test
    public void testUpdateFamily() {
        // 更新家庭
        FamilyDto familyDto = new FamilyDto();
        familyDto.setId(1L);
        familyDto.setName("更新后的家庭名称");
        familyDto.setDescription("更新后的描述");

        familyService.updateFamily(familyDto);

        // 验证
        Family updated = familyService.getById(1L);
        assertEquals("更新后的家庭名称", updated.getName());
        assertEquals("更新后的描述", updated.getDescription());

        System.out.println("✅ 更新家庭测试通过");
    }

    @Test
    public void testInviteCodeUniqueness() {
        // 创建多个家庭，验证邀请码唯一性
        FamilyDto family1 = new FamilyDto();
        family1.setName("家庭1");
        family1.setStatus(1);

        FamilyDto family2 = new FamilyDto();
        family2.setName("家庭2");
        family2.setStatus(1);

        Family created1 = familyService.createFamily(family1);
        Family created2 = familyService.createFamily(family2);

        // 验证邀请码不同
        assertNotEquals(created1.getInviteCode(), created2.getInviteCode());

        System.out.println("✅ 邀请码唯一性测试通过");
        System.out.println("   邀请码1: " + created1.getInviteCode());
        System.out.println("   邀请码2: " + created2.getInviteCode());
    }
}
