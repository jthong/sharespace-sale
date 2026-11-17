package com.cloudsteward.module.member.convert.faceblacklist;

import java.util.*;

import com.cloudsteward.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.cloudsteward.module.member.controller.admin.faceblacklist.vo.*;
import com.cloudsteward.module.member.dal.dataobject.faceblacklist.FaceBlacklistDO;

/**
 * 人脸黑名单 Convert
 *
 * @author 超级管理员
 */
@Mapper
public interface FaceBlacklistConvert {

    FaceBlacklistConvert INSTANCE = Mappers.getMapper(FaceBlacklistConvert.class);

    FaceBlacklistRespVO convert(FaceBlacklistDO bean);

    List<FaceBlacklistRespVO> convertList(List<FaceBlacklistDO> list);

    PageResult<FaceBlacklistRespVO> convertPage(PageResult<FaceBlacklistDO> page);

    List<FaceBlacklistExcelVO> convertList02(List<FaceBlacklistDO> list);

}
