package com.cloudsteward.module.member.dal.dataobject.inventoryrecord;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.cloudsteward.framework.mybatis.core.dataobject.BaseDO;

/**
 * 寄存商品存取记录 DO
 *
 * @author 超级管理员
 */
@TableName("member_inventory_record")
@KeySequence("member_inventory_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRecordDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 寄存单ID
     */
    private Long inventoryId;

    /**
     * 详情ID
     */
    private Long detailId;
    /**
     * 门店ID
     */
    private Long storeId;
    /**
     * 数量
     */
    private Integer num;

}
