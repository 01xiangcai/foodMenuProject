package com.yao.food_menu;

import com.yao.food_menu.service.ThumbnailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;

/**
 * 缩略图批量生成测试类
 * 用于手动触发历史图片的缩略图补齐任务
 */
@SpringBootTest
@Slf4j
public class ThumbnailBatchTest {

    @Autowired
    private ThumbnailService thumbnailService;

    @Test
    public void runBatchGenerate() {
        log.info(">>>>>> [测试开始] 正在扫描文件系统并批量生成缩略图...");
        
        // 调用业务层执行异步批量逻辑
        thumbnailService.batchGenerateThumbnails();
        
        log.info(">>>>>> [测试已启动] 异步任务正在后台运行，请前往管理后台或观察控制台日志。");
        
        System.out.println("\n------------------------------------------------");
        System.out.println("  批量缩略图生成任务已在后台启动！");
        System.out.println("  请检查控制台日志或前往管理后台查看进度条。");
        System.out.println("------------------------------------------------\n");
    }
}
