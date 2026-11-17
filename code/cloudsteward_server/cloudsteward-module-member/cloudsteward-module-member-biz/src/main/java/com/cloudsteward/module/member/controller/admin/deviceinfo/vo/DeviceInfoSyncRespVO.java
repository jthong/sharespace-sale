package com.cloudsteward.module.member.controller.admin.deviceinfo.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DeviceInfoSyncRespVO {
    private int discovered;
    private int added;
    private int existing;
    private int skipped;
    private int conflicts;
    private List<String> unsupportedUiids = new ArrayList<>();
}
