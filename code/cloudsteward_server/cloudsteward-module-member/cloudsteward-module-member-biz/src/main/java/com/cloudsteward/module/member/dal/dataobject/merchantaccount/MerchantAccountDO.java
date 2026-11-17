package com.cloudsteward.module.member.dal.dataobject.merchantaccount;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloudsteward.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 收款账户申请信息 DO
 *
 * @author 超级管理员
 */
@TableName("member_merchant_account")
@KeySequence("member_merchant_account_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantAccountDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 申请单编号
     */
    private String requestNo;
    /**
     * 账户状态
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 店铺简称
     */
    private String shopName;
    /**
     * 商户号
     */
    private String mchId;
    /**
     * 实名名称
     */
    private String legalName;
    /**
     * 详细地址
     */
    private String address;
    /**
     * 省市区地址
     */
    private String fullAddress;
    /**
     * 法人身份证号
     */
    private String idCard;
    /**
     * 法人手机号
     */
    private String phone;
    /**
     * 开户银行名称
     */
    private String bankName;
    /**
     * 开户银行编码
     */
    private String bankCode;
    /**
     * 银行卡号
     */
    private String bankCard;
    /**
     * 店铺门头照片 URL
     */
    private String storeFront;
    /**
     * 店铺经营场景照片 URL
     */
    private String scene;
    /**
     * 身份证人像面照片 URL
     */
    private String idCardFront;
    /**
     * 身份证国徽面照片 URL
     */
    private String idCardBack;

}
