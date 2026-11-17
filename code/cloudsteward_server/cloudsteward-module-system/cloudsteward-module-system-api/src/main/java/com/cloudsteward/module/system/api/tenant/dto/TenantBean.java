package com.cloudsteward.module.system.api.tenant.dto;

import com.cloudsteward.framework.common.enums.CommonStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class TenantBean {

    /**
     * 租户编号，自增
     */
    private Long id;
    /**
     * 租户名，唯一
     */
    private String name;
    /**
     * 联系人的用户编号
     *
     */
    private Long contactUserId;
    /**
     * 联系人
     */
    private String contactName;
    /**
     * 联系手机
     */
    private String contactMobile;
    /**
     * 租户状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
}
