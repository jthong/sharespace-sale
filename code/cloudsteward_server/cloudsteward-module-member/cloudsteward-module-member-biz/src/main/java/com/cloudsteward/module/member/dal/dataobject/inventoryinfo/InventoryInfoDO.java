package com.cloudsteward.module.member.dal.dataobject.inventoryinfo;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.cloudsteward.framework.mybatis.core.dataobject.BaseDO;

/**
 * 商品寄存单 DO
 *
 * @author 超级管理员
 */
@TableName("member_inventory_info")
@KeySequence("member_inventory_info_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryInfoDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 门店ID
     */
    private Long storeId;
    /**
     * 订单号
     */
    private String orderNo;
    /**
     * 过期时间
     */
    private Date expireTime;
    /**
     * 备注
     */
    private String remark;

}
