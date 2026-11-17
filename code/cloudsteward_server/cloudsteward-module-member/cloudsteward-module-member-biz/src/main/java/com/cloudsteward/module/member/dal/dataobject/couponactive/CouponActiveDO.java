package com.cloudsteward.module.member.dal.dataobject.couponactive;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloudsteward.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.util.Date;

/**
 * 优惠券活动 DO
 *
 * @author 超级管理员
 */
@TableName("member_coupon_active")
@KeySequence("member_coupon_active_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponActiveDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 优惠券ID
     */
    private Long couponId;
    /**
     * 门店ID
     */
    private Long storeId;
    /**
     * 活动名称
     */
    private String activeName;
    /**
     * 发放数量
     */
    private Integer num;
    /**
     * 剩余数量
     */
    private Integer balanceNum;
    /**
     * 截止时间
     */
    private Date endTime;

}
