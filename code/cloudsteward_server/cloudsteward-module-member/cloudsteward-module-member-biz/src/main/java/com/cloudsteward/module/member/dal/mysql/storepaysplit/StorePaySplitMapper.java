package com.cloudsteward.module.member.dal.mysql.storepaysplit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloudsteward.module.member.controller.admin.storepaysplit.vo.StorePaySplitExportReqVO;
import com.cloudsteward.module.member.controller.admin.storepaysplit.vo.StorePaySplitPageReqVO;
import com.cloudsteward.module.member.controller.admin.storepaysplit.vo.StorePaySplitRespVO;
import com.cloudsteward.module.member.dal.dataobject.storepaysplit.StorePaySplitDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 门店支付分账配置 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface StorePaySplitMapper extends BaseMapperX<StorePaySplitDO> {

    default PageResult<StorePaySplitDO> selectPage(StorePaySplitPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StorePaySplitDO>()
                .eqIfPresent(StorePaySplitDO::getMchId, reqVO.getMchId())
                .eqIfPresent(StorePaySplitDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(StorePaySplitDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(StorePaySplitDO::getId));
    }

    default List<StorePaySplitDO> selectList(StorePaySplitExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<StorePaySplitDO>()
                .eqIfPresent(StorePaySplitDO::getStoreId, reqVO.getStoreId())
                .eqIfPresent(StorePaySplitDO::getMchId, reqVO.getMchId())
                .eqIfPresent(StorePaySplitDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(StorePaySplitDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(StorePaySplitDO::getId));
    }

    List<StorePaySplitDO> getByStoreId(Long storeId);

    IPage<StorePaySplitRespVO> getStorePaySplitPage(@Param("page") IPage<StorePaySplitRespVO> page, @Param("reqVO") StorePaySplitPageReqVO reqVO);





}
