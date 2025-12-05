package com.yao.food_menu.interceptor;

import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.common.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT和数据隔离测试
 */
@SpringBootTest
public class DataIsolationTest {

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        FamilyContext.clear();
    }

    @AfterEach
    public void tearDown() {
        FamilyContext.clear();
    }

    @Test
    public void testJwtTokenGeneration() {
        // 生成包含familyId和role的Token
        String token = jwtUtil.generateToken(1L, "admin", 1L, 2);

        // 验证
        assertNotNull(token);
        assertTrue(token.length() > 0);

        // 解析Token
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        Long familyId = jwtUtil.getFamilyId(token);
        Integer role = jwtUtil.getRole(token);

        assertEquals(1L, userId);
        assertEquals("admin", username);
        assertEquals(1L, familyId);
        assertEquals(2, role);

        System.out.println("✅ JWT Token生成和解析测试通过");
        System.out.println("   Token包含: userId=" + userId + ", familyId=" + familyId + ", role=" + role);
    }

    @Test
    public void testJwtTokenWithoutFamilyId() {
        // 生成不包含familyId的Token（向后兼容）
        String token = jwtUtil.generateToken(1L, "admin");

        // 验证
        assertNotNull(token);

        Long userId = jwtUtil.getUserId(token);
        Long familyId = jwtUtil.getFamilyId(token);
        Integer role = jwtUtil.getRole(token);

        assertEquals(1L, userId);
        assertNull(familyId);
        assertNull(role);

        System.out.println("✅ JWT向后兼容测试通过");
    }

    @Test
    public void testFamilyContext() {
        // 设置FamilyContext
        FamilyContext.setUserId(1L);
        FamilyContext.setFamilyId(100L);
        FamilyContext.setUserRole(1);

        // 验证
        assertEquals(1L, FamilyContext.getUserId());
        assertEquals(100L, FamilyContext.getFamilyId());
        assertEquals(1, FamilyContext.getUserRole());
        assertFalse(FamilyContext.isSuperAdmin());
        assertTrue(FamilyContext.isFamilyAdmin());

        System.out.println("✅ FamilyContext测试通过");
    }

    @Test
    public void testSuperAdminRole() {
        // 设置超级管理员
        FamilyContext.setUserRole(2);

        // 验证
        assertTrue(FamilyContext.isSuperAdmin());
        assertFalse(FamilyContext.isFamilyAdmin());

        System.out.println("✅ 超级管理员角色测试通过");
    }

    @Test
    public void testFamilyContextClear() {
        // 设置上下文
        FamilyContext.setUserId(1L);
        FamilyContext.setFamilyId(100L);
        FamilyContext.setUserRole(1);

        // 清除
        FamilyContext.clear();

        // 验证
        assertNull(FamilyContext.getUserId());
        assertNull(FamilyContext.getFamilyId());
        assertNull(FamilyContext.getUserRole());

        System.out.println("✅ FamilyContext清除测试通过");
    }
}
