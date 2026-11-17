package com.cloudsteward.module.member.dal.dataobject.inventorygoods;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.cloudsteward.framework.mybatis.core.dataobject.BaseDO;

/**
 * 寄存商品信息 DO
 *
 * @author 超级管理员
 */
@TableName("member_inventory_goods")
@KeySequence("member_inventory_goods_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryGoodsDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 门店ID
     */
    private Long storeId;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 单位
     */
    private String units;
    /**
     * 是否启用
     */
    private Boolean enable;

}
