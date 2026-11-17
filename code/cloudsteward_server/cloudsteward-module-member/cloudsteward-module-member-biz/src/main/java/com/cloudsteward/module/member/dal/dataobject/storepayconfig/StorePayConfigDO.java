package com.cloudsteward.module.member.dal.dataobject.storepayconfig;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.cloudsteward.framework.mybatis.core.dataobject.BaseDO;

/**
 * 门店支付收款配置 DO
 *
 * @author 超级管理员
 */
@TableName("member_store_pay_config")
@KeySequence("member_store_pay_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorePayConfigDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 小程序appid
     */
    private String appId;
    /**
     * 商户号
     */
    private String mchId;
    /**
     * 是否分账
     */
    private Boolean split;
    /**
     * 门店ID
     */
    private Long storeId;

}
