package com.cloudsteward.module.member.dal.dataobject.store;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloudsteward.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 门店-店铺映射 DO
 *
 * @author MrGuan
 */
@TableName("member_store_shop_mapping")
@KeySequence("member_store_shop_mapping_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreShopMappingDO extends BaseDO {

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 门店ID
     */
    private Long storeId;
    /**
     * 平台：1=美团, 2=抖音
     */
    private Integer platform;
    /**
     * ISP分配的店铺ID
     */
    private String shopId;

}
