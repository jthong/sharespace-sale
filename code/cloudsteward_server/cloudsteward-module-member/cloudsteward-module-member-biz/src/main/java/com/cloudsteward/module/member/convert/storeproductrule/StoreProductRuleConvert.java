package com.cloudsteward.module.member.convert.storeproductrule;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.app.storeproductrule.vo.StoreProductRuleCreateReqVO;
import com.cloudsteward.module.member.controller.app.storeproductrule.vo.StoreProductRuleExcelVO;
import com.cloudsteward.module.member.controller.app.storeproductrule.vo.StoreProductRuleRespVO;
import com.cloudsteward.module.member.controller.app.storeproductrule.vo.StoreProductRuleUpdateReqVO;
import com.cloudsteward.module.member.dal.dataobject.storeproductrule.StoreProductRuleDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 商品规则值(规格) Convert
 *
 * @author yshop
 */
@Mapper
public interface StoreProductRuleConvert {

    StoreProductRuleConvert INSTANCE = Mappers.getMapper(StoreProductRuleConvert.class);

    StoreProductRuleDO convert(StoreProductRuleCreateReqVO bean);

    StoreProductRuleDO convert(StoreProductRuleUpdateReqVO bean);

    StoreProductRuleRespVO convert(StoreProductRuleDO bean);

    List<StoreProductRuleRespVO> convertList(List<StoreProductRuleDO> list);

    PageResult<StoreProductRuleRespVO> convertPage(PageResult<StoreProductRuleDO> page);

    List<StoreProductRuleExcelVO> convertList02(List<StoreProductRuleDO> list);

}
