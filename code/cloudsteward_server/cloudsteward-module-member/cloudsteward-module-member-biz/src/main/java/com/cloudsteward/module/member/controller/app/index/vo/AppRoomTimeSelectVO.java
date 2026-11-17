package com.cloudsteward.module.member.controller.app.index.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppRoomTimeSelectVO {

    private String time;

    private LocalDateTime date;

    private boolean available;

    private String remark;

    //高亮显示
    private boolean hightLight;

}
