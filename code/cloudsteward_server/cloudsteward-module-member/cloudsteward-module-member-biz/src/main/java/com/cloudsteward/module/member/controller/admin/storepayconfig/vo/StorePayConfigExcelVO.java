package com.cloudsteward.module.member.controller.admin.storepayconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 门店支付收款配置 Excel VO
 *
 * @author 超级管理员
 */
@Data
public class StorePayConfigExcelVO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("商户号")
    private String mchId;

    @ExcelProperty("是否分账")
    private Boolean split;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("门店ID")
    private Long storeId;

}
