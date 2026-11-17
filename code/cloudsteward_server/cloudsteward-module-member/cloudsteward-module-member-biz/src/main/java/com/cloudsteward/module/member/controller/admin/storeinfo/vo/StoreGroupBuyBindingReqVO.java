package com.cloudsteward.module.member.controller.admin.storeinfo.vo;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class StoreGroupBuyBindingReqVO {
    @NotNull(message = "请选择门店")
    private Long storeId;
    @NotBlank(message = "请输入团购服务商门店 ID")
    @Pattern(regexp = "[1-9][0-9]{0,63}", message = "门店 ID 应为 1 至 64 位数字，不能以 0 开头")
    private String shopId;
}
