package com.cloudsteward.module.member.dal.mysql.merchantaccount;

import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.module.member.dal.dataobject.merchantaccount.MerchantAccountDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 收款账户申请信息 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface MerchantAccountMapper extends BaseMapperX<MerchantAccountDO> {


    MerchantAccountDO getByUserId(Long userId);


    int updateByRequestNo(@Param("requestNo") String requestNo, @Param("remark") String remark, @Param("status") Integer status);

}
