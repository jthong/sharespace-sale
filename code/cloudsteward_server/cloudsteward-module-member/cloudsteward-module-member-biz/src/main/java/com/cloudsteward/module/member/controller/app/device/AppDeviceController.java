package com.cloudsteward.module.member.controller.app.device;

import com.cloudsteward.framework.common.pojo.CommonResult;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.security.core.annotations.PreAuthenticated;
import com.cloudsteward.module.member.controller.admin.deviceinfo.vo.DeviceInfoPageReqVO;
import com.cloudsteward.module.member.controller.admin.deviceinfo.vo.DeviceInfoRespVO;
import com.cloudsteward.module.member.controller.app.order.vo.OrderListRespVO;
import com.cloudsteward.module.member.controller.app.order.vo.OrderPageReqVO;
import com.cloudsteward.module.member.service.device.DeviceService;
import com.cloudsteward.module.member.service.deviceinfo.DeviceInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.framework.common.pojo.CommonResult.success;
import static com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.ORDER_PAGE_PARAM_ERROR;

@Tag(name = "miniapp - 设备管理")
@RestController
@RequestMapping("/member/device")
@Validated
@Slf4j
public class AppDeviceController {

    @Resource
    private DeviceInfoService deviceInfoService;

    @PostMapping("/getDevicePage")
    @Operation(summary = "获取设备分页", description = "")
    @PreAuthenticated
    public CommonResult<PageResult<DeviceInfoRespVO>> getDevicePage(@RequestBody @Valid DeviceInfoPageReqVO reqVO) {
        return success(deviceInfoService.getDeviceInfoPage(reqVO,false));
    }



}
