package com.cloudsteward.module.member.controller.admin.deviceinfo;

import com.cloudsteward.framework.common.pojo.CommonResult;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.excel.core.util.ExcelUtils;
import com.cloudsteward.framework.idempotent.core.annotation.Idempotent;
import com.cloudsteward.framework.operatelog.core.annotations.OperateLog;
import com.cloudsteward.module.member.controller.admin.deviceinfo.vo.*;
import com.cloudsteward.module.member.convert.deviceinfo.DeviceInfoConvert;
import com.cloudsteward.module.member.dal.dataobject.deviceinfo.DeviceInfoDO;
import com.cloudsteward.module.member.service.deviceinfo.DeviceInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.cloudsteward.framework.common.pojo.CommonResult.success;
import static com.cloudsteward.framework.operatelog.core.enums.OperateTypeEnum.*;

@Tag(name = "管理后台 - 设备管理")
@RestController
@RequestMapping("/member/device-info")
@Validated
public class DeviceInfoController {

    @Resource
    private DeviceInfoService deviceInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建设备管理")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    @PreAuthorize("@ss.hasPermission('member:device-info:create')")
    @OperateLog(type = CREATE)
    public CommonResult<Boolean> createDeviceInfo(@Valid @RequestBody DeviceInfoCreateReqVO createReqVO) {
        deviceInfoService.createDeviceInfo(createReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('member:device-info:delete')")
    @OperateLog(type = DELETE)
    public CommonResult<Boolean> deleteDeviceInfo(@RequestParam("id") Long id) {
        deviceInfoService.deleteDeviceInfo(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备管理")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:device-info:query')")
    public CommonResult<DeviceInfoRespVO> getDeviceInfo(@RequestParam("id") Long id) {
        DeviceInfoDO deviceInfo = deviceInfoService.getDeviceInfo(id);
        return success(DeviceInfoConvert.INSTANCE.convert(deviceInfo));
    }

    @GetMapping("/list")
    @Operation(summary = "获得设备管理列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('member:device-info:query')")
    public CommonResult<List<DeviceInfoRespVO>> getDeviceInfoList(@RequestParam("ids") Collection<Long> ids) {
        List<DeviceInfoDO> list = deviceInfoService.getDeviceInfoList(ids);
        return success(DeviceInfoConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备管理分页")
    @PreAuthorize("@ss.hasPermission('member:device-info:query')")
    public CommonResult<PageResult<DeviceInfoRespVO>> getDeviceInfoPage(@Valid DeviceInfoPageReqVO pageVO) {
        return success(deviceInfoService.getDeviceInfoPage(pageVO,true));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出设备管理 Excel")
    @PreAuthorize("@ss.hasPermission('member:device-info:export')")
    @OperateLog(type = EXPORT)
    public void exportDeviceInfoExcel(@Valid DeviceInfoExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<DeviceInfoDO> list = deviceInfoService.getDeviceInfoList(exportReqVO);
        // 导出 Excel
        List<DeviceInfoExcelVO> datas = DeviceInfoConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "设备管理.xls", "数据", DeviceInfoExcelVO.class, datas);
    }

    @PostMapping("/bind")
    @Operation(summary = "设备绑定门店/房间")
    @PreAuthorize("@ss.hasPermission('member:device-info:update')")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    @OperateLog(type = UPDATE)
    public CommonResult<Boolean> bind(@RequestBody @Valid DeviceInfoBindReqVO reqVO) {
        deviceInfoService.bind(reqVO);
        return success(true);
    }

    @PostMapping("/configWifi")
    @Operation(summary = "重置设备wifi")
    @PreAuthorize("@ss.hasPermission('member:device-info:update')")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> configWifi(@RequestBody @Valid DeviceInfoConfigWifiReqVO reqVO) {
        deviceInfoService.syncSubDevices(reqVO.getDeviceId());
        return success(true);
    }

    @PostMapping("/syncSubDevices")
    @Operation(summary = "获取网关子设备并同步到同一门店，不修改网络配置")
    @PreAuthorize("@ss.hasPermission('member:device-info:update')")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "正在同步，请稍后再试")
    @OperateLog(type = UPDATE)
    public CommonResult<DeviceInfoSyncRespVO> syncSubDevices(@RequestBody @Valid DeviceInfoSyncReqVO reqVO) {
        return success(deviceInfoService.syncSubDevices(reqVO.getDeviceId()));
    }

    @PostMapping("/pairGateway")
    @Operation(summary = "发送网关配网指令，开启子设备入网")
    @PreAuthorize("@ss.hasPermission('member:device-info:update')")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "配网指令正在发送，请稍后再试")
    @OperateLog(type = UPDATE)
    public CommonResult<Boolean> pairGateway(@RequestBody @Valid DeviceInfoSyncReqVO reqVO) {
        deviceInfoService.pairGateway(reqVO.getDeviceId());
        return success(true);
    }


    @PostMapping("/setAutoLock")
    @Operation(summary = "设置门锁自动关锁时间")
    @PreAuthorize("@ss.hasPermission('member:device-info:update')")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> setAutoLock(@RequestBody @Valid DeviceInfoSetAutoLockReqVO reqVO) {
        deviceInfoService.setLockAutoLock(reqVO);
        return success(true);
    }


    @PostMapping("/control")
    @Operation(summary = "设备控制")
    @PreAuthorize("@ss.hasPermission('member:device-info:update')")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> control(@RequestBody @Valid DeviceControlReqVO reqVO) {
        deviceInfoService.control(reqVO);
        return success(true);
    }
}
