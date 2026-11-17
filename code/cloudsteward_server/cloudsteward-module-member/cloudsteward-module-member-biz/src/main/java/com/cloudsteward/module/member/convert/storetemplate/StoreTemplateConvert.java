package com.cloudsteward.module.member.convert.storetemplate;

import java.util.*;

import com.cloudsteward.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.cloudsteward.module.member.controller.admin.storetemplate.vo.*;
import com.cloudsteward.module.member.dal.dataobject.storetemplate.StoreTemplateDO;

/**
 * 门店模板 Convert
 *
 * @author 超级管理员
 */
@Mapper
public interface StoreTemplateConvert {

    StoreTemplateConvert INSTANCE = Mappers.getMapper(StoreTemplateConvert.class);

    StoreTemplateDO convert(StoreTemplateCreateReqVO bean);

    StoreTemplateDO convert(StoreTemplateUpdateReqVO bean);

    StoreTemplateRespVO convert(StoreTemplateDO bean);

    List<StoreTemplateRespVO> convertList(List<StoreTemplateDO> list);

    PageResult<StoreTemplateRespVO> convertPage(PageResult<StoreTemplateDO> page);

    List<StoreTemplateExcelVO> convertList02(List<StoreTemplateDO> list);

}
