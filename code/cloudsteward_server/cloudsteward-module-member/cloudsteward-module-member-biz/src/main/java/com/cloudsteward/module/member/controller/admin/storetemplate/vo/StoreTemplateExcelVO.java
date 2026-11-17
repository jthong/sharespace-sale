package com.cloudsteward.module.member.controller.admin.storetemplate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 门店模板 Excel VO
 *
 * @author 超级管理员
 */
@Data
public class StoreTemplateExcelVO {

    @ExcelProperty("模板ID")
    private Long templateId;

    @ExcelProperty("模板名称")
    private String templateName;

    @ExcelProperty("模板Key")
    private String templateKey;

    @ExcelProperty("模板预览图")
    private String templateImg;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
