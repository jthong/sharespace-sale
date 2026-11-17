package com.cloudsteward.module.member.dal.dataobject.deviceinfo;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloudsteward.framework.tenant.core.db.TenantBaseDO;
import lombok.*;

/**
 * 设备管理 DO
 *
 * @author 芋道源码
 */
@TableName("member_device_info")
@KeySequence("member_device_info_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfoDO extends TenantBaseDO {

    /**
     * 设备id
     */
    @TableId
    private Long deviceId;
    /**
     * 设备sn
     */
    private String deviceSn;

    /** Gateway that actually reported this child; never infer it from the first store gateway. */
    private String gatewaySn;

    /**
     * 设备数据
     */
    private String deviceData;

    /**
     * 共享设备
     */

    private Boolean share;
    /**
     * 设备类型
     *
     */
    private Integer type;
    /**
     * 房间id
     */
    private Long storeId;


    private Long roomId;
    /**
     * 状态
     */
    private Integer status;

    /**
     * 信号强度
     */
    private Integer rssi;

    /**
     * 版本号
     */
    private String cversion;


}
