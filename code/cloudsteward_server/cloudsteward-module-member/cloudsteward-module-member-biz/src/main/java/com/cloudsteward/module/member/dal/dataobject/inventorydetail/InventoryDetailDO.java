package com.cloudsteward.module.member.dal.dataobject.inventorydetail;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.cloudsteward.framework.mybatis.core.dataobject.BaseDO;

/**
 * 商品寄存单详细 DO
 *
 * @author 超级管理员
 */
@TableName("member_inventory_detail")
@KeySequence("member_inventory_detail_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDetailDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;
    /**
     * 寄存单ID
     */
    private Long inventoryId;
    /**
     * 商品ID
     */
    private Long goodsId;
    /**
     * 存入数量
     */
    private Integer saveNum;
    /**
     * 剩余数量
     */
    private Integer stockNum;

}
