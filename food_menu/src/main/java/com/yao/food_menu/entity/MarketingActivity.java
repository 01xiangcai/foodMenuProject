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
 * 营销活动实体类
 */
@Data
public class MarketingActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 家庭ID
    private Long familyId;

    // 活动名称
    private String activityName;

    // 活动类型: LOTTERY(抽奖), WHEEL(大转盘), COUPON(优惠券), POINTS_EXCHANGE(积分兑换),
    // SIGN_IN(签到), GROUP_BUY(拼团)
    private String activityType;

    // 活动描述
    private String activityDesc;

    // 活动横幅图片
    private String bannerImage;

    // 本地横幅图片路径
    private String localBannerImage;

    // 开始时间
    private LocalDateTime startTime;

    // 结束时间
    private LocalDateTime endTime;

    // 状态: 0-未开始, 1-进行中, 2-已结束, 3-已暂停
    private Integer status;

    // 参与次数限制(0表示不限制)
    private Integer participateLimit;

    // 限制类型: DAILY(每日), TOTAL(总计), UNLIMITED(不限)
    private String limitType;

    // 参与条件配置(JSON格式)
    private String participateCondition;

    // 活动配置(JSON格式,不同活动类型配置不同)
    private String activityConfig;

    // 排序权重
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 创建人
    private Long createBy;

    // 更新人
    private Long updateBy;

    // 逻辑删除: 0-未删除, 1-已删除
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
