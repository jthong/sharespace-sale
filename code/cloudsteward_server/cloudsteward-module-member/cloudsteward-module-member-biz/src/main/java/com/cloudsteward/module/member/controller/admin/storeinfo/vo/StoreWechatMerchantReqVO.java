package com.cloudsteward.module.member.controller.admin.storeinfo.vo;
import lombok.Data;
import javax.validation.constraints.*;
@Data
public class StoreWechatMerchantReqVO {
    @NotNull(message="请选择门店") private Long storeId;
    @NotBlank(message="请输入微信特约商户号")
    @Pattern(regexp="[0-9]{8,32}",message="微信特约商户号须为8至32位数字") private String mchId;
}
