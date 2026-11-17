package com.cloudsteward.module.member.dal.mysql.storepayconfig;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloudsteward.module.member.controller.admin.storepayconfig.vo.StorePayConfigExportReqVO;
import com.cloudsteward.module.member.controller.admin.storepayconfig.vo.StorePayConfigPageReqVO;
import com.cloudsteward.module.member.controller.admin.storepayconfig.vo.StorePayConfigRespVO;
import com.cloudsteward.module.member.dal.dataobject.storepayconfig.StorePayConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 门店支付收款配置 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface StorePayConfigMapper extends BaseMapperX<StorePayConfigDO> {

    default PageResult<StorePayConfigDO> selectPage(StorePayConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StorePayConfigDO>()
                .eqIfPresent(StorePayConfigDO::getMchId, reqVO.getMchId())
                .eqIfPresent(StorePayConfigDO::getSplit, reqVO.getSplit())
                .betweenIfPresent(StorePayConfigDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(StorePayConfigDO::getId));
    }

    default List<StorePayConfigDO> selectList(StorePayConfigExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<StorePayConfigDO>()
                .eqIfPresent(StorePayConfigDO::getMchId, reqVO.getMchId())
                .eqIfPresent(StorePayConfigDO::getSplit, reqVO.getSplit())
                .betweenIfPresent(StorePayConfigDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(StorePayConfigDO::getStoreId, reqVO.getStoreId())
                .orderByDesc(StorePayConfigDO::getId));
    }

     StorePayConfigDO getByStoreId(Long storeId);
     StorePayConfigDO getByMchId(String mchId);

    IPage<StorePayConfigRespVO> getStorePayConfigPage(@Param("page") IPage<StorePayConfigRespVO> page, @Param("reqVO") StorePayConfigPageReqVO reqVO);

}
