package com.cloudsteward.module.member.controller.app.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppUpRoomLockReqVO {

//    @Schema(description = "房间编号")
//    @NotNull(message = "房间编号不能为空")
//    private Long roomId;

    @Schema(description = "门锁编号")
    private String  sn;

    @Schema(description = "更新数据")
    private String upData;
}
