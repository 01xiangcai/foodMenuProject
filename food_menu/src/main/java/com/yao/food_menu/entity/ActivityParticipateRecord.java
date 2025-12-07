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
 * 活动参与记录实体类
 */
@Data
public class ActivityParticipateRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 活动ID
    private Long activityId;

    // 参与用户ID
    private Long wxUserId;

    // 参与时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime participateTime;

    // 中奖奖品ID(NULL表示未中奖)
    private Long prizeId;

    // 是否中奖: 0-未中奖, 1-已中奖
    private Integer isWin;

    // 奖品状态: 0-未领取, 1-已领取, 2-已使用, 3-已过期
    private Integer prizeStatus;

    // 领取时间
    private LocalDateTime claimTime;

    // 使用时间
    private LocalDateTime useTime;

    // 过期时间
    private LocalDateTime expireTime;

    // 额外数据(如抽奖结果详情)
    private String extraData;

    // 家庭ID
    private Long familyId;

    // 逻辑删除: 0-未删除, 1-已删除
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
