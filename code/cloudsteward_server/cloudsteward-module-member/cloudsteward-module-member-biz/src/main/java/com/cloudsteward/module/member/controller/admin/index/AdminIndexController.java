package com.cloudsteward.module.member.controller.admin.index;

import com.cloudsteward.framework.common.core.KeyValue;
import com.cloudsteward.framework.common.pojo.CommonResult;
import com.cloudsteward.framework.security.core.annotations.PreAuthenticated;
import com.cloudsteward.module.member.controller.admin.index.vo.AppConfigReqVO;
import com.cloudsteward.module.member.service.index.IndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static com.cloudsteward.framework.common.pojo.CommonResult.success;

/**
 * @PACKAGE_NAME: com.cloudsteward.module.member.controller.admin.index
 * @DESCRIPTION:
 * @USER: MrGuan  mrguan@aliyun.com
 * @DATE: 2023/8/8 11:48
 */
@Tag(name = "管理后台 - 首页公共接口")
@RestController
@RequestMapping("/index")
@Validated
public class AdminIndexController {

    @Resource
    private IndexService indexService;
    @GetMapping("/getStoreList")
    @Operation(summary = "获取门店下拉选择列表")
    @PreAuthenticated
    public CommonResult<List<KeyValue<String, Long>>> getStoreList(@RequestParam(value = "name", required = false) String name
            , @RequestParam(value = "cityName", required = false) String cityName) {
        return success(indexService.getStoreList(name,cityName));
    }

    @GetMapping("/getRoomList/{storeId}")
    @Operation(summary = "获取房间下拉选择列表")
    @PreAuthenticated
    @Parameter(name = "storeId")
    public CommonResult<List<KeyValue<String, Long>>> getRoomList(@PathVariable("storeId") Long storeId) {
        return success(indexService.getRoomListByAdmin(storeId));
    }

    @PostMapping("/getAppConfig")
    @Operation(summary = "获取系统配置")
    @PreAuthorize("@ss.hasPermission('infra:config:update')")
    public CommonResult<AppConfigReqVO> getAppConfig() {
        return success(indexService.getAppConfig());
    }

    @PostMapping("/saveAppConfig")
    @Operation(summary = "保存系统配置")
    @PreAuthorize("@ss.hasPermission('infra:config:update')")
    public CommonResult<Boolean> saveAppConfig(@Valid @RequestBody AppConfigReqVO reqVO) {
        indexService.saveAppConfig(reqVO);
        return success(true);
    }


}
