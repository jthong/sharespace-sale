package com.cloudsteward.module.member.dal.dataobject.storetemplate;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.cloudsteward.framework.mybatis.core.dataobject.BaseDO;

/**
 * 门店模板 DO
 *
 * @author 超级管理员
 */
@TableName("member_store_template")
@KeySequence("member_store_template_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreTemplateDO extends BaseDO {

    /**
     * 模板ID
     */
    @TableId
    private Long templateId;
    /**
     * 模板名称
     */
    private String templateName;
    /**
     * 模板Key
     */
    private String templateKey;
    /**
     * 模板预览图
     */
    private String templateImg;

    /**
     * 逗号分隔的模板图片
     */
    private String imgList;

}
