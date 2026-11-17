package com.cloudsteward.module.member.dal.mysql.franchiseinfo;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloudsteward.module.member.controller.admin.franchiseinfo.vo.FranchiseInfoExportReqVO;
import com.cloudsteward.module.member.controller.admin.franchiseinfo.vo.FranchiseInfoPageReqVO;
import com.cloudsteward.module.member.dal.dataobject.franchiseinfo.FranchiseInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 加盟信息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FranchiseInfoMapper extends BaseMapperX<FranchiseInfoDO> {

    default PageResult<FranchiseInfoDO> selectPage(FranchiseInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FranchiseInfoDO>()
                .likeIfPresent(FranchiseInfoDO::getCity, reqVO.getCity())
                .likeIfPresent(FranchiseInfoDO::getContactName, reqVO.getContactName())
                .eqIfPresent(FranchiseInfoDO::getMessage, reqVO.getMessage())
                .eqIfPresent(FranchiseInfoDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(FranchiseInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(FranchiseInfoDO::getId));
    }

    default List<FranchiseInfoDO> selectList(FranchiseInfoExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<FranchiseInfoDO>()
                .likeIfPresent(FranchiseInfoDO::getCity, reqVO.getCity())
                .likeIfPresent(FranchiseInfoDO::getContactName, reqVO.getContactName())
                .eqIfPresent(FranchiseInfoDO::getMessage, reqVO.getMessage())
                .eqIfPresent(FranchiseInfoDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(FranchiseInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(FranchiseInfoDO::getId));
    }
    FranchiseInfoDO getByUserId(Long userId);
}
