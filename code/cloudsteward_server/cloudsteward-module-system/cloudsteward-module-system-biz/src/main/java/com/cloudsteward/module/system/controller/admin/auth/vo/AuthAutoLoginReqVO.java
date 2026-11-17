package com.cloudsteward.module.system.controller.admin.auth.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthAutoLoginReqVO extends AuthLoginReqVO {
    /** Only required when verified credentials match more than one organization. */
    @Length(max = 100)
    private String organizationName;
}
