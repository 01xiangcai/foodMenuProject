package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yao.food_menu.dto.MigrationStatus;
import com.yao.food_menu.entity.Banner;
import com.yao.food_menu.entity.Dish;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.mapper.BannerMapper;
import com.yao.food_menu.mapper.DishMapper;
import com.yao.food_menu.mapper.WxUserMapper;
import com.yao.food_menu.service.ImageMigrationService;

import com.yao.food_menu.common.config.LocalStorageProperties;
import com.yao.food_menu.service.storage.impl.OssStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 图片迁移服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageMigrationServiceImpl implements ImageMigrationService {

    private final DishMapper dishMapper;
    private final WxUserMapper wxUserMapper;
    private final BannerMapper bannerMapper;
    private final OssStorageStrategy ossStorageStrategy;
    private final LocalStorageProperties localProperties;

    private final AtomicBoolean dishMigrationRunning = new AtomicBoolean(false);
    private final AtomicBoolean avatarMigrationRunning = new AtomicBoolean(false);

    private MigrationStatus dishStatus = new MigrationStatus();
    private MigrationStatus avatarStatus = new MigrationStatus();

    @Override
    @Async
    public void migrateDishImages() {
        if (!dishMigrationRunning.compareAndSet(false, true)) {
            log.warn("菜品图片迁移已在运行中");
            return;
        }

        try {
            dishStatus = new MigrationStatus();
            dishStatus.setType("DISH");
            dishStatus.setRunning(true);
            dishStatus.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            dishStatus.setMessage("开始迁移菜品图片...");

            // 查询所有有图片但没有本地图片的菜品
            List<Dish> dishes = dishMapper.selectList(
                    new LambdaQueryWrapper<Dish>()
                            .isNotNull(Dish::getImage)
                            .and(wrapper -> wrapper.isNull(Dish::getLocalImage).or().eq(Dish::getLocalImage, "")));

            dishStatus.setTotal(dishes.size());
            dishStatus.setMigrated(0);
            dishStatus.setFailed(0);
            dishStatus.setSkipped(0);

            log.info("找到{}个菜品需要迁移图片", dishes.size());

            AtomicInteger migrated = new AtomicInteger(0);
            AtomicInteger failed = new AtomicInteger(0);

            for (Dish dish : dishes) {
                try {
                    String ossKey = dish.getImage();
                    String localPath = downloadAndSaveImage(ossKey, "dish");

                    if (localPath != null) {
                        dish.setLocalImage(localPath);
                        dishMapper.updateById(dish);
                        migrated.incrementAndGet();
                        log.info("菜品图片迁移成功: {} -> {}", dish.getName(), localPath);
                    } else {
                        failed.incrementAndGet();
                        log.error("菜品图片迁移失败: {}", dish.getName());
                    }
                } catch (Exception e) {
                    failed.incrementAndGet();
                    log.error("迁移菜品图片失败: {}", dish.getName(), e);
                }

                dishStatus.setMigrated(migrated.get());
                dishStatus.setFailed(failed.get());
            }

            // 迁移Banner图片
            List<Banner> banners = bannerMapper.selectList(
                    new LambdaQueryWrapper<Banner>()
                            .isNotNull(Banner::getImage)
                            .and(wrapper -> wrapper.isNull(Banner::getLocalImage).or().eq(Banner::getLocalImage, "")));

            log.info("找到{}个轮播图需要迁移", banners.size());
            dishStatus.setTotal(dishStatus.getTotal() + banners.size());

            for (Banner banner : banners) {
                try {
                    String ossKey = banner.getImage();
                    String localPath = downloadAndSaveImage(ossKey, "banner");

                    if (localPath != null) {
                        banner.setLocalImage(localPath);
                        bannerMapper.updateById(banner);
                        migrated.incrementAndGet();
                        log.info("轮播图迁移成功: {} -> {}", banner.getTitle(), localPath);
                    } else {
                        failed.incrementAndGet();
                    }
                } catch (Exception e) {
                    failed.incrementAndGet();
                    log.error("迁移轮播图失败", e);
                }

                dishStatus.setMigrated(migrated.get());
                dishStatus.setFailed(failed.get());
            }

            dishStatus.setRunning(false);
            dishStatus.setEndTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            dishStatus.setMessage(String.format("迁移完成: 成功%d, 失败%d", migrated.get(), failed.get()));

            log.info("菜品和轮播图迁移完成: 成功{}, 失败{}", migrated.get(), failed.get());

        } catch (Exception e) {
            log.error("菜品图片迁移异常", e);
            dishStatus.setRunning(false);
            dishStatus.setMessage("迁移失败: " + e.getMessage());
        } finally {
            dishMigrationRunning.set(false);
        }
    }

    @Override
    @Async
    public void migrateAvatars() {
        if (!avatarMigrationRunning.compareAndSet(false, true)) {
            log.warn("头像迁移已在运行中");
            return;
        }

        try {
            avatarStatus = new MigrationStatus();
            avatarStatus.setType("AVATAR");
            avatarStatus.setRunning(true);
            avatarStatus.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            avatarStatus.setMessage("开始迁移用户头像...");

            // 查询所有有头像但没有本地头像的用户
            List<WxUser> users = wxUserMapper.selectList(
                    new LambdaQueryWrapper<WxUser>()
                            .isNotNull(WxUser::getAvatar)
                            .and(wrapper -> wrapper.isNull(WxUser::getLocalAvatar).or().eq(WxUser::getLocalAvatar,
                                    "")));

            avatarStatus.setTotal(users.size());
            avatarStatus.setMigrated(0);
            avatarStatus.setFailed(0);
            avatarStatus.setSkipped(0);

            log.info("找到{}个用户需要迁移头像", users.size());

            AtomicInteger migrated = new AtomicInteger(0);
            AtomicInteger failed = new AtomicInteger(0);
            AtomicInteger skipped = new AtomicInteger(0);

            for (WxUser user : users) {
                try {
                    String avatar = user.getAvatar();

                    // 跳过微信头像URL
                    if (avatar.contains("wx.qlogo.cn") || avatar.contains("thirdwx.qlogo.cn")) {
                        skipped.incrementAndGet();
                        log.debug("跳过微信头像: {}", user.getNickname());
                        continue;
                    }

                    String ossKey = ossStorageStrategy.extractKey(avatar);
                    String localPath = downloadAndSaveImage(ossKey, "avatar");

                    if (localPath != null) {
                        user.setLocalAvatar(localPath);
                        wxUserMapper.updateById(user);
                        migrated.incrementAndGet();
                        log.info("用户头像迁移成功: {} -> {}", user.getNickname(), localPath);
                    } else {
                        failed.incrementAndGet();
                    }
                } catch (Exception e) {
                    failed.incrementAndGet();
                    log.error("迁移用户头像失败: {}", user.getNickname(), e);
                }

                avatarStatus.setMigrated(migrated.get());
                avatarStatus.setFailed(failed.get());
                avatarStatus.setSkipped(skipped.get());
            }

            avatarStatus.setRunning(false);
            avatarStatus.setEndTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            avatarStatus.setMessage(String.format("迁移完成: 成功%d, 失败%d, 跳过%d",
                    migrated.get(), failed.get(), skipped.get()));

            log.info("头像迁移完成: 成功{}, 失败{}, 跳过{}", migrated.get(), failed.get(), skipped.get());

        } catch (Exception e) {
            log.error("头像迁移异常", e);
            avatarStatus.setRunning(false);
            avatarStatus.setMessage("迁移失败: " + e.getMessage());
        } finally {
            avatarMigrationRunning.set(false);
        }
    }

    @Override
    public MigrationStatus getDishMigrationStatus() {
        return dishStatus;
    }

    @Override
    public MigrationStatus getAvatarMigrationStatus() {
        return avatarStatus;
    }

    @Override
    public void stopMigration() {
        dishMigrationRunning.set(false);
        avatarMigrationRunning.set(false);
        log.info("迁移任务已停止");
    }

    /**
     * 下载OSS图片并保存到本地
     */
    private String downloadAndSaveImage(String ossKey, String type) {
        try {
            // 生成OSS URL
            String ossUrl = ossStorageStrategy.generateUrl(ossKey);

            // 确定本地保存路径
            String folder = type.equals("avatar") ? "avatars" : "food-menu";
            String fileName = ossKey.substring(ossKey.lastIndexOf("/") + 1);
            String relativePath = folder + "/" + fileName;

            Path fullPath = Paths.get(localProperties.getBasePath(), relativePath);

            // 创建目录
            Files.createDirectories(fullPath.getParent());

            // 下载文件
            URL url = new URL(ossUrl);
            try (InputStream in = url.openStream();
                    FileOutputStream out = new FileOutputStream(fullPath.toFile())) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            log.debug("图片下载成功: {} -> {}", ossKey, fullPath);
            return relativePath;

        } catch (Exception e) {
            log.error("下载图片失败: {}", ossKey, e);
            return null;
        }
    }
}
