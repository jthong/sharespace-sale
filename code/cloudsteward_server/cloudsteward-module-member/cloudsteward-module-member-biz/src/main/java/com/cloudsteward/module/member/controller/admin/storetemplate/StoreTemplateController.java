package com.cloudsteward.module.member.controller.admin.storetemplate;

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

import com.cloudsteward.module.member.controller.admin.storetemplate.vo.*;
import com.cloudsteward.module.member.dal.dataobject.storetemplate.StoreTemplateDO;
import com.cloudsteward.module.member.convert.storetemplate.StoreTemplateConvert;
import com.cloudsteward.module.member.service.storetemplate.StoreTemplateService;

@Tag(name = "管理后台 - 门店模板")
@RestController
@RequestMapping("/member/store-template")
@Validated
public class StoreTemplateController {

    @Resource
    private StoreTemplateService storeTemplateService;

//    @PostMapping("/create")
//    @Operation(summary = "创建门店模板")
//    @PreAuthorize("@ss.hasPermission('member:store-template:create')")
//    public CommonResult<Long> createStoreTemplate(@Valid @RequestBody StoreTemplateCreateReqVO createReqVO) {
//        return success(storeTemplateService.createStoreTemplate(createReqVO));
//    }
//
//    @PutMapping("/update")
//    @Operation(summary = "更新门店模板")
//    @PreAuthorize("@ss.hasPermission('member:store-template:update')")
//    public CommonResult<Boolean> updateStoreTemplate(@Valid @RequestBody StoreTemplateUpdateReqVO updateReqVO) {
//        storeTemplateService.updateStoreTemplate(updateReqVO);
//        return success(true);
//    }
//
//    @DeleteMapping("/delete")
//    @Operation(summary = "删除门店模板")
//    @Parameter(name = "id", description = "编号", required = true)
//    @PreAuthorize("@ss.hasPermission('member:store-template:delete')")
//    public CommonResult<Boolean> deleteStoreTemplate(@RequestParam("id") Long id) {
//        storeTemplateService.deleteStoreTemplate(id);
//        return success(true);
//    }

    @GetMapping("/get")
    @Operation(summary = "获得门店模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:store-template:query')")
    public CommonResult<StoreTemplateRespVO> getStoreTemplate(@RequestParam("id") Long id) {
        StoreTemplateDO storeTemplate = storeTemplateService.getStoreTemplate(id);
        return success(StoreTemplateConvert.INSTANCE.convert(storeTemplate));
    }

    @GetMapping("/list")
    @Operation(summary = "获得门店模板列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('member:store-template:query')")
    public CommonResult<List<StoreTemplateRespVO>> getStoreTemplateList(@RequestParam("ids") Collection<Long> ids) {
        List<StoreTemplateDO> list = storeTemplateService.getStoreTemplateList(ids);
        return success(StoreTemplateConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得门店模板分页")
    @PreAuthorize("@ss.hasPermission('member:store-template:query')")
    public CommonResult<PageResult<StoreTemplateRespVO>> getStoreTemplatePage(@Valid StoreTemplatePageReqVO pageVO) {
        PageResult<StoreTemplateDO> pageResult = storeTemplateService.getStoreTemplatePage(pageVO);
        return success(StoreTemplateConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出门店模板 Excel")
    @PreAuthorize("@ss.hasPermission('member:store-template:export')")
    @OperateLog(type = EXPORT)
    public void exportStoreTemplateExcel(@Valid StoreTemplateExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<StoreTemplateDO> list = storeTemplateService.getStoreTemplateList(exportReqVO);
        // 导出 Excel
        List<StoreTemplateExcelVO> datas = StoreTemplateConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "门店模板.xls", "数据", StoreTemplateExcelVO.class, datas);
    }

}
