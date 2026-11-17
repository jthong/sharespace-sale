package com.cloudsteward.module.member.service.wx;

import lombok.Data;

/**
 * @PACKAGE_NAME: com.cloudsteward.framework.tenant.core.context
 * @DESCRIPTION:
 * @USER: MrGuan  mrguan@aliyun.com
 * @DATE: 2024/3/6 12:14
 */
@Data
public class MiniappConfigVO {

    private String miniappId;


    private String miniappSecret;

    private String nfcModelId;

}
