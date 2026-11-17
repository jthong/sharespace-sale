package com.cloudsteward.module.member.convert.storepayconfig;

import java.util.*;

import com.cloudsteward.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.cloudsteward.module.member.controller.admin.storepayconfig.vo.*;
import com.cloudsteward.module.member.dal.dataobject.storepayconfig.StorePayConfigDO;

/**
 * 门店支付收款配置 Convert
 *
 * @author 超级管理员
 */
@Mapper
public interface StorePayConfigConvert {

    StorePayConfigConvert INSTANCE = Mappers.getMapper(StorePayConfigConvert.class);

    StorePayConfigDO convert(StorePayConfigCreateReqVO bean);

    StorePayConfigDO convert(StorePayConfigUpdateReqVO bean);

    StorePayConfigRespVO convert(StorePayConfigDO bean);

    List<StorePayConfigRespVO> convertList(List<StorePayConfigDO> list);

    PageResult<StorePayConfigRespVO> convertPage(PageResult<StorePayConfigDO> page);

    List<StorePayConfigExcelVO> convertList02(List<StorePayConfigDO> list);

}
