package com.cloudsteward.module.member.convert.discountrules;

import java.util.*;

import com.cloudsteward.framework.common.pojo.PageResult;

import com.cloudsteward.module.member.controller.app.store.vo.AppDiscountRulesDetailReqVO;
import com.cloudsteward.module.member.controller.app.store.vo.AppDiscountRulesDetailRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.cloudsteward.module.member.dal.dataobject.discountrules.DiscountRulesDO;

/**
 * 充值优惠规则管理 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface DiscountRulesConvert {

    DiscountRulesConvert INSTANCE = Mappers.getMapper(DiscountRulesConvert.class);

    AppDiscountRulesDetailRespVO convert2(DiscountRulesDO bean);
    DiscountRulesDO convert2(AppDiscountRulesDetailReqVO bean);


}
