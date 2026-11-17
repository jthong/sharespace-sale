package com.cloudsteward.module.member.convert.storepaysplit;

import java.util.*;

import com.cloudsteward.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.cloudsteward.module.member.controller.admin.storepaysplit.vo.*;
import com.cloudsteward.module.member.dal.dataobject.storepaysplit.StorePaySplitDO;

/**
 * 门店支付分账配置 Convert
 *
 * @author 超级管理员
 */
@Mapper
public interface StorePaySplitConvert {

    StorePaySplitConvert INSTANCE = Mappers.getMapper(StorePaySplitConvert.class);

    StorePaySplitDO convert(StorePaySplitCreateReqVO bean);

    StorePaySplitDO convert(StorePaySplitUpdateReqVO bean);

    StorePaySplitRespVO convert(StorePaySplitDO bean);

    List<StorePaySplitRespVO> convertList(List<StorePaySplitDO> list);

    PageResult<StorePaySplitRespVO> convertPage(PageResult<StorePaySplitDO> page);

    List<StorePaySplitExcelVO> convertList02(List<StorePaySplitDO> list);

}
