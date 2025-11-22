package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.entity.Banner;
import com.yao.food_menu.service.BannerService;
import com.yao.food_menu.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "轮播图管理", description = "轮播图的增删改查")
@RestController
@RequestMapping("/banner")
@Slf4j
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private OssService ossService;

    private static final String DEFAULT_IMAGE = "https://dummyimage.com/800x400/0f172a/ffffff&text=banner";

    @Operation(summary = "添加轮播图", description = "添加轮播图")
    @PostMapping
    public Result<String> save(@RequestBody Banner banner) {
        log.info("banner:{}", banner);
        ensureImage(banner);
        bannerService.save(banner);
        return Result.success("Banner added successfully");
    }

    @Operation(summary = "分页查询轮播图", description = "分页查询轮播图列表")
    @GetMapping("/page")
    public Result<Page<Banner>> page(int page, int pageSize) {
        Page<Banner> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Banner::getSort).orderByDesc(Banner::getUpdateTime);
        bannerService.page(pageInfo, queryWrapper);
        // Convert OSS object keys to presigned URLs
        pageInfo.getRecords().forEach(this::convertBannerImageToPresignedUrl);
        return Result.success(pageInfo);
    }

    @Operation(summary = "删除轮播图", description = "根据ID删除轮播图")
    @DeleteMapping
    public Result<String> delete(Long id) {
        log.info("Delete banner, id: {}", id);
        bannerService.removeById(id);
        return Result.success("Banner deleted successfully");
    }

    @Operation(summary = "更新轮播图", description = "更新轮播图信息")
    @PutMapping
    public Result<String> update(@RequestBody Banner banner) {
        log.info("Update banner: {}", banner);
        ensureImage(banner);
        bannerService.updateById(banner);
        return Result.success("Banner updated successfully");
    }

    @Operation(summary = "查询启用的轮播图列表", description = "查询所有启用的轮播图，用于小程序端展示")
    @GetMapping("/list")
    public Result<List<Banner>> list() {
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Banner::getStatus, 1); // Only enabled banners
        queryWrapper.orderByAsc(Banner::getSort).orderByDesc(Banner::getUpdateTime);
        List<Banner> list = bannerService.list(queryWrapper);
        // Convert OSS object keys to presigned URLs
        list.forEach(this::convertBannerImageToPresignedUrl);
        return Result.success(list);
    }

    @Operation(summary = "根据ID查询轮播图", description = "根据ID查询轮播图详情")
    @GetMapping("/{id}")
    public Result<Banner> get(@PathVariable Long id) {
        Banner banner = bannerService.getById(id);
        if (banner != null) {
            convertBannerImageToPresignedUrl(banner);
        }
        return Result.success(banner);
    }

    private void ensureImage(Banner banner) {
        if (!StringUtils.hasText(banner.getImage())) {
            banner.setImage(DEFAULT_IMAGE);
        }
    }

    /**
     * Convert OSS object key to presigned URL for Banner entity
     */
    private void convertBannerImageToPresignedUrl(Banner banner) {
        if (banner == null || !StringUtils.hasText(banner.getImage())) {
            return;
        }
        String image = banner.getImage();
        // If image is not a full URL (doesn't start with http:// or https://),
        // treat it as OSS object key and convert to presigned URL
        if (!image.startsWith("http://") && !image.startsWith("https://")) {
            try {
                String presignedUrl = ossService.generatePresignedUrl(image);
                banner.setImage(presignedUrl);
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for object key: {}, using default image", image, e);
                banner.setImage(DEFAULT_IMAGE);
            }
        }
    }
}

