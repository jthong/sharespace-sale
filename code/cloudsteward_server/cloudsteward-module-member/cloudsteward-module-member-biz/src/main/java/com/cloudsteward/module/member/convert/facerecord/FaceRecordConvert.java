package com.cloudsteward.module.member.convert.facerecord;

import java.util.*;

import com.cloudsteward.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.cloudsteward.module.member.controller.admin.facerecord.vo.*;
import com.cloudsteward.module.member.dal.dataobject.facerecord.FaceRecordDO;

/**
 * 人脸识别记录 Convert
 *
 * @author 超级管理员
 */
@Mapper
public interface FaceRecordConvert {

    FaceRecordConvert INSTANCE = Mappers.getMapper(FaceRecordConvert.class);

    FaceRecordRespVO convert(FaceRecordDO bean);

    List<FaceRecordRespVO> convertList(List<FaceRecordDO> list);

    PageResult<FaceRecordRespVO> convertPage(PageResult<FaceRecordDO> page);

    List<FaceRecordExcelVO> convertList02(List<FaceRecordDO> list);

}
