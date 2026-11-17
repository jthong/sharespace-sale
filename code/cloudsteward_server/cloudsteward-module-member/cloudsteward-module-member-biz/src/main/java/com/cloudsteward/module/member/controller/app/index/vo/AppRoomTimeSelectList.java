package com.cloudsteward.module.member.controller.app.index.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AppRoomTimeSelectList {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "选项列表")
    private List<AppRoomTimeSelectVO> selectList;

}
