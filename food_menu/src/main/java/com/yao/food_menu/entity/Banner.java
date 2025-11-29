package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 轮播图实体类
 */
@Data
public class Banner implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 轮播图图片URL(OSS对象键)
    private String image;

    // 本地图片路径
    private String localImage;

    // 轮播图标题
    private String title;

    // 轮播图描述
    private String description;

    // 链接URL(可选,用于导航)
    private String linkUrl;

    // 排序顺序
    private Integer sort;

    // 状态: 0-禁用, 1-启用
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    // 家庭ID
    private Long familyId;

    // 逻辑删除: 0-未删除, 1-已删除
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
