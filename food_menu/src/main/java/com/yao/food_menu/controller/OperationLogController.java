package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.dto.OperationLogQueryDto;
import com.yao.food_menu.entity.OperationLog;
import com.yao.food_menu.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志控制器
 * 提供操作日志查询功能（管理员使用）
 * 
 * @author yao
 */
@Tag(name = "操作日志管理", description = "操作日志查询、审计")
@RestController
@RequestMapping("/admin/operationLog")
@Slf4j
public class OperationLogController {
    
    @Autowired
    private OperationLogService operationLogService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 分页查询操作日志
     * 权限控制：
     * - 超级管理员(role=2)：可以看到所有数据，可按familyId筛选
     * - 普通管理员(role=0)和家庭管理员(role=1)：只能看到自己家庭的数据
     */
    @Operation(summary = "分页查询操作日志", description = "支持多条件查询操作日志，根据角色自动进行数据隔离")
    @GetMapping("/page")
    public Result<Page<OperationLog>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            OperationLogQueryDto queryDto,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        log.info("查询操作日志: page={}, pageSize={}, queryDto={}", page, pageSize, queryDto);
        
        try {
            // 获取当前用户角色和家庭ID
            Integer userRole = null;
            Long userFamilyId = null;
            
            if (StringUtils.hasText(token)) {
                // 移除"Bearer "前缀
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                try {
                    userRole = jwtUtil.getRole(token);
                    userFamilyId = jwtUtil.getFamilyId(token);
                } catch (Exception e) {
                    log.warn("解析Token失败: {}", e.getMessage());
                }
            }
            
            // 如果从Token获取失败，尝试从上下文获取
            if (userRole == null) {
                userRole = FamilyContext.getUserRole();
            }
            if (userFamilyId == null) {
                userFamilyId = FamilyContext.getFamilyId();
            }
            
            // 判断是否为超级管理员
            boolean isSuperAdmin = (userRole != null && userRole == 2);
            
            Page<OperationLog> pageInfo = new Page<>(page, pageSize);
            LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
            
            // 权限控制：非超级管理员只能查看自己家庭的数据
            if (!isSuperAdmin && userFamilyId != null) {
                queryWrapper.eq(OperationLog::getFamilyId, userFamilyId);
                log.debug("非超级管理员，限制查询范围: familyId={}", userFamilyId);
            } else if (isSuperAdmin) {
                // 超级管理员可以按家庭ID筛选（如果提供了familyId参数）
                if (queryDto.getFamilyId() != null) {
                    queryWrapper.eq(OperationLog::getFamilyId, queryDto.getFamilyId());
                    log.debug("超级管理员按家庭筛选: familyId={}", queryDto.getFamilyId());
                }
            } else {
                // 未登录或无法获取用户信息，返回空结果
                log.warn("无法获取用户信息，返回空结果");
                return Result.success(pageInfo);
            }
            
            // 按操作类型查询
            if (StringUtils.hasText(queryDto.getOperationType())) {
                queryWrapper.eq(OperationLog::getOperationType, queryDto.getOperationType());
            }
            
            // 按操作模块查询
            if (StringUtils.hasText(queryDto.getOperationModule())) {
                queryWrapper.eq(OperationLog::getOperationModule, queryDto.getOperationModule());
            }
            
            // 按操作人ID查询
            if (queryDto.getOperatorId() != null) {
                queryWrapper.eq(OperationLog::getOperatorId, queryDto.getOperatorId());
            }
            
            // 按操作人姓名模糊查询
            if (StringUtils.hasText(queryDto.getOperatorName())) {
                queryWrapper.like(OperationLog::getOperatorName, queryDto.getOperatorName());
            }
            
            // 按操作人类型查询
            if (StringUtils.hasText(queryDto.getOperatorType())) {
                queryWrapper.eq(OperationLog::getOperatorType, queryDto.getOperatorType());
            }
            
            // 按状态查询
            if (queryDto.getStatus() != null) {
                queryWrapper.eq(OperationLog::getStatus, queryDto.getStatus());
            }
            
            // 按IP地址查询
            if (StringUtils.hasText(queryDto.getIpAddress())) {
                queryWrapper.like(OperationLog::getIpAddress, queryDto.getIpAddress());
            }
            
            // 按时间范围查询
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (StringUtils.hasText(queryDto.getStartTime())) {
                try {
                    LocalDateTime startTime = LocalDateTime.parse(queryDto.getStartTime(), formatter);
                    queryWrapper.ge(OperationLog::getCreateTime, startTime);
                } catch (Exception e) {
                    log.warn("开始时间格式错误: {}", queryDto.getStartTime());
                }
            }
            if (StringUtils.hasText(queryDto.getEndTime())) {
                try {
                    LocalDateTime endTime = LocalDateTime.parse(queryDto.getEndTime(), formatter);
                    queryWrapper.le(OperationLog::getCreateTime, endTime);
                } catch (Exception e) {
                    log.warn("结束时间格式错误: {}", queryDto.getEndTime());
                }
            }
            
            // 按创建时间降序排序
            queryWrapper.orderByDesc(OperationLog::getCreateTime);
            
            operationLogService.page(pageInfo, queryWrapper);
            
            return Result.success(pageInfo);
        } catch (Exception e) {
            log.error("查询操作日志失败", e);
            return Result.error("查询操作日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询操作日志详情
     * 权限控制：非超级管理员只能查看自己家庭的日志
     */
    @Operation(summary = "查询操作日志详情", description = "根据ID查询单条操作日志详情，根据角色自动进行权限控制")
    @GetMapping("/{id}")
    public Result<OperationLog> getById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("查询操作日志详情: id={}", id);
        
        try {
            OperationLog operationLog = operationLogService.getById(id);
            if (operationLog == null) {
                return Result.error("操作日志不存在");
            }
            
            // 权限控制：检查是否有权限查看该日志
            if (!hasPermission(operationLog, token)) {
                return Result.error("无权查看该操作日志");
            }
            
            return Result.success(operationLog);
        } catch (Exception e) {
            log.error("查询操作日志详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除操作日志（仅管理员）
     * 权限控制：非超级管理员只能删除自己家庭的日志
     */
    @Operation(summary = "删除操作日志", description = "根据ID删除操作日志（慎用），根据角色自动进行权限控制")
    @DeleteMapping("/{id}")
    public Result<String> delete(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.warn("删除操作日志: id={}", id);
        
        try {
            OperationLog operationLog = operationLogService.getById(id);
            if (operationLog == null) {
                return Result.error("操作日志不存在");
            }
            
            // 权限控制：检查是否有权限删除该日志
            if (!hasPermission(operationLog, token)) {
                return Result.error("无权删除该操作日志");
            }
            
            boolean success = operationLogService.removeById(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除操作日志失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除操作日志（仅管理员）
     * 权限控制：非超级管理员只能删除自己家庭的日志
     */
    @Operation(summary = "批量删除操作日志", description = "根据时间范围批量删除历史日志，根据角色自动进行权限控制")
    @DeleteMapping("/batch")
    public Result<String> batchDelete(
            @RequestParam String endTime,
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.warn("批量删除操作日志: endTime={}", endTime);
        
        try {
            // 获取当前用户角色和家庭ID
            Integer userRole = null;
            Long userFamilyId = null;
            
            if (StringUtils.hasText(token)) {
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                try {
                    userRole = jwtUtil.getRole(token);
                    userFamilyId = jwtUtil.getFamilyId(token);
                } catch (Exception e) {
                    log.warn("解析Token失败: {}", e.getMessage());
                }
            }
            
            if (userRole == null) {
                userRole = FamilyContext.getUserRole();
            }
            if (userFamilyId == null) {
                userFamilyId = FamilyContext.getFamilyId();
            }
            
            boolean isSuperAdmin = (userRole != null && userRole == 2);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);
            
            LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.le(OperationLog::getCreateTime, end);
            
            // 权限控制：非超级管理员只能删除自己家庭的日志
            if (!isSuperAdmin && userFamilyId != null) {
                queryWrapper.eq(OperationLog::getFamilyId, userFamilyId);
            } else if (!isSuperAdmin) {
                return Result.error("无权执行批量删除操作");
            }
            
            boolean success = operationLogService.remove(queryWrapper);
            if (success) {
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除操作日志失败", e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否有权限操作该日志
     * 超级管理员可以操作所有日志，普通管理员和家庭管理员只能操作自己家庭的日志
     * 
     * @param operationLog 操作日志
     * @param token 用户Token
     * @return 是否有权限
     */
    private boolean hasPermission(OperationLog operationLog, String token) {
        try {
            // 获取当前用户角色和家庭ID
            Integer userRole = null;
            Long userFamilyId = null;
            
            if (StringUtils.hasText(token)) {
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                try {
                    userRole = jwtUtil.getRole(token);
                    userFamilyId = jwtUtil.getFamilyId(token);
                } catch (Exception e) {
                    log.warn("解析Token失败: {}", e.getMessage());
                }
            }
            
            if (userRole == null) {
                userRole = FamilyContext.getUserRole();
            }
            if (userFamilyId == null) {
                userFamilyId = FamilyContext.getFamilyId();
            }
            
            // 超级管理员可以操作所有日志
            if (userRole != null && userRole == 2) {
                return true;
            }
            
            // 普通管理员和家庭管理员只能操作自己家庭的日志
            if (userFamilyId != null && operationLog.getFamilyId() != null) {
                return userFamilyId.equals(operationLog.getFamilyId());
            }
            
            // 如果日志没有家庭ID，则不允许非超级管理员操作
            return false;
        } catch (Exception e) {
            log.error("权限检查失败", e);
            return false;
        }
    }
}

