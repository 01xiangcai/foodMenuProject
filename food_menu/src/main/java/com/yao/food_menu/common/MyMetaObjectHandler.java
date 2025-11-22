package com.yao.food_menu.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Custom MetaObjectHandler to auto-fill common fields
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * Insertion operation auto-fill
     * 
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("Start insert fill...");
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createUser", Long.class, BaseContext.getCurrentId());
        this.strictInsertFill(metaObject, "updateUser", Long.class, BaseContext.getCurrentId());
    }

    /**
     * Update operation auto-fill
     * 
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("Start update fill...");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateUser", Long.class, BaseContext.getCurrentId());
    }
}
