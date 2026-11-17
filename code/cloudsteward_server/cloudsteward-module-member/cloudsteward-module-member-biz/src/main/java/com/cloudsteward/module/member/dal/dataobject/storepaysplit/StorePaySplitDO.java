package com.cloudsteward.module.member.dal.dataobject.storepaysplit;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.cloudsteward.framework.mybatis.core.dataobject.BaseDO;

/**
 * 门店支付分账配置 DO
 *
 * @author 超级管理员
 */
@TableName("member_store_pay_split")
@KeySequence("member_store_pay_split_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorePaySplitDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 门店名称
     */
    private Long storeId;
    /**
     * 易宝商户号
     */
    private String mchId;
    /**
     * 分账比例
     */
    private Integer rate;
    /**
     * 备注
     */
    private String remark;

}
