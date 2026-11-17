package com.cloudsteward.module.member.controller.admin.storepaysplit;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.common.pojo.CommonResult;
import static com.cloudsteward.framework.common.pojo.CommonResult.success;

import com.cloudsteward.framework.excel.core.util.ExcelUtils;

import com.cloudsteward.framework.operatelog.core.annotations.OperateLog;
import static com.cloudsteward.framework.operatelog.core.enums.OperateTypeEnum.*;

import com.cloudsteward.module.member.controller.admin.storepaysplit.vo.*;
import com.cloudsteward.module.member.dal.dataobject.storepaysplit.StorePaySplitDO;
import com.cloudsteward.module.member.convert.storepaysplit.StorePaySplitConvert;
import com.cloudsteward.module.member.service.storepaysplit.StorePaySplitService;

@Tag(name = "管理后台 - 门店支付分账配置")
@RestController
@RequestMapping("/member/store-pay-split")
@Validated
public class StorePaySplitController {

    @Resource
    private StorePaySplitService storePaySplitService;

    @PostMapping("/create")
    @Operation(summary = "创建门店支付分账配置")
    @PreAuthorize("@ss.hasPermission('member:store-pay-split:create')")
    public CommonResult<Long> createStorePaySplit(@Valid @RequestBody StorePaySplitCreateReqVO createReqVO) {
        return success(storePaySplitService.createStorePaySplit(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新门店支付分账配置")
    @PreAuthorize("@ss.hasPermission('member:store-pay-split:update')")
    public CommonResult<Boolean> updateStorePaySplit(@Valid @RequestBody StorePaySplitUpdateReqVO updateReqVO) {
        storePaySplitService.updateStorePaySplit(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除门店支付分账配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('member:store-pay-split:delete')")
    public CommonResult<Boolean> deleteStorePaySplit(@RequestParam("id") Long id) {
        storePaySplitService.deleteStorePaySplit(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得门店支付分账配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:store-pay-split:query')")
    public CommonResult<StorePaySplitRespVO> getStorePaySplit(@RequestParam("id") Long id) {
        StorePaySplitDO storePaySplit = storePaySplitService.getStorePaySplit(id);
        return success(StorePaySplitConvert.INSTANCE.convert(storePaySplit));
    }

    @GetMapping("/list")
    @Operation(summary = "获得门店支付分账配置列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('member:store-pay-split:query')")
    public CommonResult<List<StorePaySplitRespVO>> getStorePaySplitList(@RequestParam("ids") Collection<Long> ids) {
        List<StorePaySplitDO> list = storePaySplitService.getStorePaySplitList(ids);
        return success(StorePaySplitConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得门店支付分账配置分页")
    @PreAuthorize("@ss.hasPermission('member:store-pay-split:query')")
    public CommonResult<PageResult<StorePaySplitRespVO>> getStorePaySplitPage(@Valid StorePaySplitPageReqVO pageVO) {
        return success(storePaySplitService.getStorePaySplitPage(pageVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出门店支付分账配置 Excel")
    @PreAuthorize("@ss.hasPermission('member:store-pay-split:export')")
    @OperateLog(type = EXPORT)
    public void exportStorePaySplitExcel(@Valid StorePaySplitExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<StorePaySplitDO> list = storePaySplitService.getStorePaySplitList(exportReqVO);
        // 导出 Excel
        List<StorePaySplitExcelVO> datas = StorePaySplitConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "门店支付分账配置.xls", "数据", StorePaySplitExcelVO.class, datas);
    }

}
