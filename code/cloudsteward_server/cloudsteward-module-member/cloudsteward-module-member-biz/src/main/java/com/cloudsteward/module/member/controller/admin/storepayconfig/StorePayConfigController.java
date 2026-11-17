package com.cloudsteward.module.member.controller.admin.storepayconfig;

import com.cloudsteward.framework.common.pojo.CommonResult;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.admin.storepayconfig.vo.StorePayConfigCreateReqVO;
import com.cloudsteward.module.member.controller.admin.storepayconfig.vo.StorePayConfigPageReqVO;
import com.cloudsteward.module.member.controller.admin.storepayconfig.vo.StorePayConfigRespVO;
import com.cloudsteward.module.member.controller.admin.storepayconfig.vo.StorePayConfigUpdateReqVO;
import com.cloudsteward.module.member.convert.storepayconfig.StorePayConfigConvert;
import com.cloudsteward.module.member.dal.dataobject.storepayconfig.StorePayConfigDO;
import com.cloudsteward.module.member.service.storepayconfig.StorePayConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static com.cloudsteward.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 门店支付收款配置")
@RestController
@RequestMapping("/member/store-pay-config")
@Validated
public class StorePayConfigController {

    @Resource
    private StorePayConfigService storePayConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建门店支付收款配置")
    @PreAuthorize("@ss.hasPermission('member:store-pay-config:create')")
    public CommonResult<Long> createStorePayConfig(@Valid @RequestBody StorePayConfigCreateReqVO createReqVO) {
        return success(storePayConfigService.createStorePayConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新门店支付收款配置")
    @PreAuthorize("@ss.hasPermission('member:store-pay-config:update')")
    public CommonResult<Boolean> updateStorePayConfig(@Valid @RequestBody StorePayConfigUpdateReqVO updateReqVO) {
        storePayConfigService.updateStorePayConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除门店支付收款配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('member:store-pay-config:delete')")
    public CommonResult<Boolean> deleteStorePayConfig(@RequestParam("id") Long id) {
        storePayConfigService.deleteStorePayConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得门店支付收款配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:store-pay-config:query')")
    public CommonResult<StorePayConfigRespVO> getStorePayConfig(@RequestParam("id") Long id) {
        StorePayConfigDO storePayConfig = storePayConfigService.getStorePayConfig(id);
        return success(StorePayConfigConvert.INSTANCE.convert(storePayConfig));
    }

    @GetMapping("/page")
    @Operation(summary = "获得门店支付收款配置分页")
    @PreAuthorize("@ss.hasPermission('member:store-pay-config:query')")
    public CommonResult<PageResult<StorePayConfigRespVO>> getStorePayConfigPage(@Valid StorePayConfigPageReqVO pageVO) {
        return success(storePayConfigService.getStorePayConfigPage(pageVO));
    }



}
