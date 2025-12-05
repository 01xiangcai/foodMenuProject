# SQL注入防护检查报告

## 📋 检查概述

**检查日期：** 2024-12-05  
**检查范围：** food_menu后端项目所有SQL语句  
**检查结果：** ✅ 通过（无SQL注入风险）

---

## ✅ 检查结果

### 1. MyBatis XML映射文件

**文件：** `DishMapper.xml`

```xml
<!-- ✅ 安全：使用参数化查询 #{} -->
<select id="selectTopSelling" resultType="com.yao.food_menu.dto.DishDto">
    SELECT d.*, IFNULL(SUM(oi.quantity), 0) as sales
    FROM dish d
    LEFT JOIN order_items oi ON d.id = oi.dish_id
    WHERE d.status = 1 AND d.deleted = 0
    <if test="!isSuperAdmin and familyId != null">
        AND d.family_id = #{familyId}
    </if>
    GROUP BY d.id
    ORDER BY sales DESC
    LIMIT #{limit}
</select>
```

**结论：** 所有参数都使用 `#{}` 进行参数化查询，安全 ✅

### 2. 注解SQL

**文件：** `DishStatisticsMapper.java`

```java
// ✅ 安全：使用参数化查询
@Update("INSERT INTO dish_statistics (dish_id, total_order_count, last_order_time) " +
        "VALUES (#{dishId}, 1, NOW()) " +
        "ON DUPLICATE KEY UPDATE " +
        "total_order_count = total_order_count + 1, " +
        "last_order_time = NOW()")
int incrementOrderCount(@Param("dishId") Long dishId);
```

**结论：** 使用 `#{dishId}` 参数化查询，安全 ✅

### 3. MyBatis-Plus自动生成SQL

项目大量使用MyBatis-Plus的CRUD方法：
- `save()`, `updateById()`, `removeById()`, `list()` 等
- 这些方法都使用预编译SQL，自动防注入

**结论：** MyBatis-Plus内置防护，安全 ✅

---

## 🔒 防护措施总结

### 已采取的防护措施

1. **✅ 参数化查询**
   - 所有SQL都使用 `#{}` 而非 `${}`
   - 预编译SQL，参数与SQL分离

2. **✅ MyBatis-Plus ORM**
   - 使用MyBatis-Plus的CRUD方法
   - 自动生成安全的SQL

3. **✅ 无字符串拼接SQL**
   - 没有发现直接字符串拼接SQL的情况
   - 没有使用 `${}`（不安全的占位符）

4. **✅ 输入验证**
   - Controller层有基础的参数校验
   - 使用DTO进行数据传输

---

## 📖 SQL注入防护最佳实践

### 1. 使用参数化查询（推荐）

#### ✅ 正确示例

```xml
<!-- MyBatis XML -->
<select id="getUserById" resultType="User">
    SELECT * FROM user WHERE id = #{id}
</select>

<!-- MyBatis注解 -->
@Select("SELECT * FROM user WHERE id = #{id}")
User getUserById(@Param("id") Long id);

<!-- MyBatis-Plus -->
userMapper.selectById(userId);
```

#### ❌ 错误示例

```xml
<!-- ❌ 危险：字符串拼接，容易SQL注入 -->
<select id="getUserById" resultType="User">
    SELECT * FROM user WHERE id = ${id}
</select>
```

### 2. 动态排序字段处理

当需要动态ORDER BY时，不能直接使用参数化查询（因为字段名不能参数化）。

#### ✅ 推荐方案：白名单验证

```java
public List<User> listUsers(String sortField, String sortOrder) {
    // 白名单验证
    List<String> allowedFields = Arrays.asList("id", "name", "create_time");
    List<String> allowedOrders = Arrays.asList("ASC", "DESC");
    
    if (!allowedFields.contains(sortField) || !allowedOrders.contains(sortOrder.toUpperCase())) {
        throw new IllegalArgumentException("非法的排序参数");
    }
    
    // 使用MyBatis-Plus的OrderItem（已内置防护）
    return userMapper.selectList(
        Wrappers.lambdaQuery(User.class)
            .orderBy(true, "ASC".equals(sortOrder), 
                SFunction根据白名单获取)
    );
}
```

#### ❌ 错误示例

```java
// ❌ 危险：直接拼接用户输入
String sql = "SELECT * FROM user ORDER BY " + sortField + " " + sortOrder;
```

### 3. LIKE查询处理

#### ✅ 正确示例

```xml
<!-- 方式1：在XML中拼接 % -->
<select id="searchUser" resultType="User">
    SELECT * FROM user 
    WHERE name LIKE CONCAT('%', #{keyword}, '%')
</select>

<!-- 方式2：在Java代码中拼接（推荐） -->
```

```java
// 在Service层处理
String keyword = "%" + keyword + "%";
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .like(User::getName, keyword);
```

### 4. IN查询处理

#### ✅ 正确示例

```xml
<!-- MyBatis foreach -->
<select id="selectByIds" resultType="User">
    SELECT * FROM user WHERE id IN
    <foreach collection="ids" item="id" open="(" close=")" separator=",">
        #{id}
    </foreach>
</select>
```

```java
// MyBatis-Plus
userMapper.selectBatchIds(Arrays.asList(1L, 2L, 3L));
```

---

## 🚨 需要注意的场景

### 1. 全文搜索

如果需要实现复杂的搜索功能，建议使用：
- Elasticsearch
- MySQL全文索引
- 避免在WHERE子句中使用函数（如 `LOWER(name) LIKE ...`）

### 2. 动态表名/字段名

**原则：** 表名和字段名无法参数化，必须使用白名单验证

```java
// ✅ 正确：白名单验证
private static final Map<String, String> TABLE_MAP = Map.of(
    "user", "user",
    "order", "orders",
    "dish", "dish"
);

public List<?> queryByTableName(String tableName) {
    String safeTableName = TABLE_MAP.get(tableName);
    if (safeTableName == null) {
        throw new IllegalArgumentException("非法的表名");
    }
    // 使用安全的表名执行查询
}
```

### 3. 原生JDBC

如果必须使用原生JDBC，务必使用PreparedStatement：

```java
// ✅ 正确
String sql = "SELECT * FROM user WHERE id = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setLong(1, userId);
ResultSet rs = pstmt.executeQuery();

// ❌ 错误
String sql = "SELECT * FROM user WHERE id = " + userId;
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(sql);
```

---

## 🛠️ 开发规范

### 代码审查检查清单

- [ ] 所有SQL使用 `#{}` 而非 `${}`
- [ ] 动态字段/表名使用白名单验证
- [ ] LIKE查询正确处理通配符
- [ ] 没有直接拼接用户输入到SQL
- [ ] 使用MyBatis-Plus的API而非原生SQL
- [ ] 输入参数有合理的校验

### Git Hook建议

可以添加Git pre-commit hook检查：

```bash
#!/bin/bash
# 检查是否有使用 ${} 的情况
if git diff --cached --name-only | grep -E '\.xml$|\.java$' | xargs grep -n '\$\{' ; then
    echo "警告：发现使用 \${} 的SQL，可能存在注入风险！"
    echo "请使用 #{} 参数化查询"
    exit 1
fi
```

---

## 📊 检查统计

| 项目 | 数量 | 状态 |
|------|------|------|
| XML映射文件 | 1 | ✅ 安全 |
| 注解SQL | 2 | ✅ 安全 |
| 使用 `${}` | 0 | ✅ 无风险 |
| 使用 `#{}` | 所有 | ✅ 安全 |
| MyBatis-Plus | 大量 | ✅ 安全 |

---

## ✅ 结论

**项目当前的SQL注入防护措施完善，无明显安全风险。**

建议：
1. ✅ 保持使用MyBatis-Plus作为主要ORM
2. ✅ 继续使用 `#{}` 参数化查询
3. ⚠️ 如需添加动态排序，记得使用白名单验证
4. ⚠️ 定期进行代码审查，确保新增SQL符合安全规范

---

**报告生成时间：** 2024-12-05  
**下次检查建议：** 每次发布前或每月一次

