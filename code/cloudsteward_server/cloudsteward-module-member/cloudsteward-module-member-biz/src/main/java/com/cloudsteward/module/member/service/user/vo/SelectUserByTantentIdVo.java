package com.cloudsteward.module.member.service.user.vo;

import lombok.Data;

@Data
public class SelectUserByTantentIdVo {

    private String mobile;

    private Long tenantId;

}
