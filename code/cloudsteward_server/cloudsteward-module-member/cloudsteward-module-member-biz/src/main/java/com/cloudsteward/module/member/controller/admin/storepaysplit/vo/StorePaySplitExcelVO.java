package com.cloudsteward.module.member.controller.admin.storepaysplit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 门店支付分账配置 Excel VO
 *
 * @author 超级管理员
 */
@Data
public class StorePaySplitExcelVO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("门店名称")
    private Long storeId;

    @ExcelProperty("易宝商户号")
    private String mchId;

    @ExcelProperty("分账比例")
    private Integer rate;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
