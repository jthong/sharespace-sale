package com.cloudsteward.module.member.dal.mysql.bannerinfo;

import java.util.*;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.module.member.controller.app.index.vo.AppBannerInfoRespVO;
import com.cloudsteward.module.member.dal.dataobject.bannerinfo.BannerInfoDO;
import org.apache.ibatis.annotations.Mapper;
import com.cloudsteward.module.member.controller.admin.bannerinfo.vo.*;

/**
 * 广告管理 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface BannerInfoMapper extends BaseMapperX<BannerInfoDO> {

    default PageResult<BannerInfoDO> selectPage(BannerInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BannerInfoDO>()
                .eqIfPresent(BannerInfoDO::getTitle, reqVO.getTitle())
                .eqIfPresent(BannerInfoDO::getType, reqVO.getType())
                .betweenIfPresent(BannerInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BannerInfoDO::getId));
    }

    default List<BannerInfoDO> selectList(BannerInfoExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<BannerInfoDO>()
                .eqIfPresent(BannerInfoDO::getTitle, reqVO.getTitle())
                .eqIfPresent(BannerInfoDO::getType, reqVO.getType())
                .betweenIfPresent(BannerInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BannerInfoDO::getId));
    }

    List<AppBannerInfoRespVO> getBannerList(Integer type);
}
