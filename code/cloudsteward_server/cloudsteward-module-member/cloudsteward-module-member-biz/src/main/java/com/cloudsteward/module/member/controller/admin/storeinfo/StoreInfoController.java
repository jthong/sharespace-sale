package com.cloudsteward.module.member.controller.admin.storeinfo;

import com.cloudsteward.framework.common.pojo.CommonResult;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.admin.storeinfo.vo.*;
import com.cloudsteward.module.member.convert.storeinfo.StoreInfoConvert;
import com.cloudsteward.module.member.dal.dataobject.storeinfo.StoreInfoDO;
import com.cloudsteward.module.member.service.clear.StoreCleaningNoticeSettings;
import com.cloudsteward.module.member.service.storeinfo.StoreInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

import static com.cloudsteward.framework.common.pojo.CommonResult.success;

import com.cloudsteward.framework.operatelog.core.annotations.OperateLog;
import static com.cloudsteward.framework.operatelog.core.enums.OperateTypeEnum.*;

@Tag(name = "管理后台 - 门店管理")
@RestController
@RequestMapping("/member/store-info")
@Validated
public class StoreInfoController {

    @Resource
    private StoreInfoService storeInfoService;

    @Resource
    private StoreCleaningNoticeSettings cleaningNoticeSettings;

    @GetMapping("/cleaning-notice")
    @Operation(summary = "查看门店保洁通知配置")
    @PreAuthorize("@ss.hasPermission('member:store-info:update') or @ss.hasPermission('member:workspace:manage')")
    public CommonResult<StoreCleaningNoticeSettings.Settings> getCleaningNotice(@RequestParam("storeId") Long storeId) {
        return success(cleaningNoticeSettings.get(storeId));
    }

    @PutMapping("/cleaning-notice")
    @Operation(summary = "设置门店保洁通知渠道")
    @PreAuthorize("@ss.hasPermission('member:store-info:update') or @ss.hasPermission('member:workspace:manage')")
    @OperateLog(type = UPDATE)
    public CommonResult<Boolean> saveCleaningNoticeChannel(@Valid @RequestBody CleaningChannelRequest request) {
        cleaningNoticeSettings.saveChannel(request.storeId, request.channel);
        return success(true);
    }

    @PutMapping("/cleaning-notice-template")
    @Operation(summary = "设置小程序保洁通知模板")
    @PreAuthorize("@ss.hasRole('super_admin') and (@ss.hasPermission('member:store-info:update') or @ss.hasPermission('member:workspace:manage'))")
    @OperateLog(type = UPDATE)
    public CommonResult<Boolean> saveCleaningNoticeTemplate(@Valid @RequestBody CleaningTemplateRequest request) {
        cleaningNoticeSettings.saveTemplate(request.storeId, request.templateId, request.fields);
        return success(true);
    }

    @lombok.Data
    public static class CleaningChannelRequest {
        @NotNull private Long storeId;
        @NotBlank private String channel;
    }

    @lombok.Data
    public static class CleaningTemplateRequest {
        @NotNull private Long storeId;
        @NotBlank private String templateId;
        @NotNull private Map<String, String> fields;
    }

    @Resource
    private com.cloudsteward.module.member.service.wx.partner.StorePartnerMerchantService partnerMerchants;

    @Resource private com.cloudsteward.module.member.service.wx.partner.WechatPartnerClient partnerClient;
    @Resource private com.cloudsteward.module.member.service.wx.partner.WechatPartnerProperties partnerProperties;

    @GetMapping("/wechat-merchant-status")
    @PreAuthorize("@ss.hasPermission('member:store-info:query') or @ss.hasPermission('member:workspace:query')")
    public CommonResult<java.util.Map<String,Object>> getWechatMerchantStatus(@RequestParam("storeId") Long storeId) {
        String merchant=partnerMerchants.require(storeId);
        partnerClient.verifyPlatformCertificates();
        java.util.Map<String,Object> result=new java.util.LinkedHashMap<>();
        result.put("subMchId",merchant);result.put("spMchId",partnerProperties.getSpMchId());result.put("miniAppId",partnerProperties.getMiniAppId());result.put("certificateVerified",true);
        return success(result);
    }

    @GetMapping("/wechat-merchant")
    @PreAuthorize("@ss.hasPermission('member:store-info:query') or @ss.hasPermission('member:workspace:query')")
    public CommonResult<String> getWechatMerchant(@RequestParam("storeId") Long storeId) {
        return success(partnerMerchants.get(storeId));
    }

    @PutMapping("/wechat-merchant")
    @PreAuthorize("@ss.hasPermission('member:store-info:update') or @ss.hasPermission('member:workspace:manage')")
    @OperateLog(type=UPDATE)
    public CommonResult<Boolean> bindWechatMerchant(@Valid @RequestBody StoreWechatMerchantReqVO reqVO) {
        partnerMerchants.bind(reqVO.getStoreId(),reqVO.getMchId());return success(true);
    }

    @Resource
    private com.cloudsteward.module.member.service.groupPay.GroupBuyStoreBindingService groupBuyBindings;

    @GetMapping("/group-buy-binding")
    @Operation(summary = "查看门店绑定的团购服务商门店 ID")
    @PreAuthorize("@ss.hasPermission('member:store-info:query') or @ss.hasPermission('member:workspace:query')")
    public CommonResult<String> getGroupBuyBinding(@RequestParam("storeId") Long storeId) {
        return success(groupBuyBindings.getShopId(storeId));
    }

    @PutMapping("/group-buy-binding")
    @Operation(summary = "绑定团购服务商门店 ID")
    @PreAuthorize("@ss.hasPermission('member:store-info:update') or @ss.hasPermission('member:workspace:manage')")
    @OperateLog(type = UPDATE)
    public CommonResult<Boolean> bindGroupBuyStore(@Valid @RequestBody StoreGroupBuyBindingReqVO reqVO) {
        groupBuyBindings.bind(reqVO.getStoreId(), reqVO.getShopId());
        return success(true);
    }

    @PostMapping("/group-buy-auth-url")
    @Operation(summary = "从门店管理发起美团或抖音团购授权")
    @PreAuthorize("@ss.hasPermission('member:store-info:update') or @ss.hasPermission('member:workspace:manage')")
    public CommonResult<String> getGroupBuyAuthUrl(@Valid @RequestBody com.cloudsteward.module.member.controller.app.store.vo.GroupPayAuthUrlReqVO reqVO) {
        if (!Integer.valueOf(1).equals(reqVO.getGroupPayType()) && !Integer.valueOf(2).equals(reqVO.getGroupPayType())) {
            throw com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception(
                    new com.cloudsteward.framework.common.exception.ErrorCode(1004999002, "{}"), "请选择美团或抖音团购授权");
        }
        return success(storeInfoService.getGroupPayAuthUrl(reqVO));
    }

    @PostMapping("/create")
    @Operation(summary = "创建门店管理")
    @PreAuthorize("@ss.hasPermission('member:store-info:create')")
    @OperateLog(type = CREATE)
    public CommonResult<Long> createStoreInfo(@Valid @RequestBody StoreInfoCreateReqVO createReqVO) {
        return success(storeInfoService.createStoreInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新门店管理")
    @PreAuthorize("@ss.hasPermission('member:store-info:update')")
    @OperateLog(type = UPDATE)
    public CommonResult<Boolean> updateStoreInfo(@Valid @RequestBody StoreInfoUpdateReqVO updateReqVO) {
        storeInfoService.updateStoreInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除门店管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('member:store-info:delete')")
    @OperateLog(type = DELETE)
    public CommonResult<Boolean> deleteStoreInfo(@RequestParam("id") Long id) {
        storeInfoService.deleteStoreInfo(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得门店管理")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:store-info:query')")
    public CommonResult<StoreInfoRespVO> getStoreInfo(@RequestParam("id") Long id) {
        return success(storeInfoService.getStoreInfo(id));
    }

//    @GetMapping("/meituanScope/{storeId}")
//    @Operation(summary = "美团授权链接")
//    @PreAuthorize("@ss.hasPermission('member:device-info:create')")
//    public CommonResult<String> meituanScope(@PathVariable("storeId")Long storeId) {
//        return success(storeInfoService.meituanScope(storeId));
//    }

//    @GetMapping("/list")
//    @Operation(summary = "获得门店管理列表")
//    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
//    @PreAuthorize("@ss.hasPermission('member:store-info:query')")
//    public CommonResult<List<StoreInfoRespVO>> getStoreInfoList(@RequestParam("ids") Collection<Long> ids) {
//        List<StoreInfoDO> list = storeInfoService.getStoreInfoList(ids);
//        return success(StoreInfoConvert.INSTANCE.convertList(list));
//    }

    @GetMapping("/page")
    @Operation(summary = "获得门店管理分页")
    @PreAuthorize("@ss.hasPermission('member:store-info:query')")
    public CommonResult<PageResult<StoreInfoRespVO>> getStoreInfoPage(@Valid StoreInfoPageReqVO pageVO) {
        PageResult<StoreInfoDO> pageResult = storeInfoService.getStoreInfoPage(pageVO);
        return success(StoreInfoConvert.INSTANCE.convertPage(pageResult));
    }

    @PostMapping("/renew")
    @Operation(summary = "续费门店")
    @PreAuthorize("@ss.hasPermission('member:store-info:update')")
    public CommonResult<Boolean> renew(@Valid @RequestBody StoreRenewReqVO reqVO) {
        storeInfoService.renew(reqVO);
        return success(true);
    }

    @PostMapping("/setTemplate")
    @Operation(summary = "设置门店模板")
    @PreAuthorize("@ss.hasPermission('member:store-info:update')")
    public CommonResult<Boolean> setTemplate(@Valid @RequestBody StoreSetTemplateReqVO reqVO) {
        storeInfoService.setTemplate(reqVO, true);
        return success(true);
    }


//    @GetMapping("/export-excel")
//    @Operation(summary = "导出门店管理 Excel")
//    @PreAuthorize("@ss.hasPermission('member:store-info:export')")
//    @OperateLog(type = EXPORT)
//    public void exportStoreInfoExcel(@Valid StoreInfoExportReqVO exportReqVO,
//              HttpServletResponse response) throws IOException {
//        List<StoreInfoDO> list = storeInfoService.getStoreInfoList(exportReqVO);
//        // 导出 Excel
//        List<StoreInfoExcelVO> datas = StoreInfoConvert.INSTANCE.convertList02(list);
//        ExcelUtils.write(response, "门店管理.xls", "数据", StoreInfoExcelVO.class, datas);
//    }

}
