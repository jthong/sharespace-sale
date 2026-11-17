package com.cloudsteward.module.member.dal.mysql.storeproductrule;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloudsteward.module.member.controller.app.storeproductrule.vo.StoreProductRuleExportReqVO;
import com.cloudsteward.module.member.controller.app.storeproductrule.vo.StoreProductRulePageReqVO;
import com.cloudsteward.module.member.dal.dataobject.storeproductrule.StoreProductRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品规则值(规格) Mapper
 *
 * @author yshop
 */
@Mapper
public interface StoreProductRuleMapper extends BaseMapperX<StoreProductRuleDO> {

    default PageResult<StoreProductRuleDO> selectPage(StoreProductRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StoreProductRuleDO>()
                .likeIfPresent(StoreProductRuleDO::getRuleName, reqVO.getRuleName())
                .orderByDesc(StoreProductRuleDO::getId));
    }

    default List<StoreProductRuleDO> selectList(StoreProductRuleExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<StoreProductRuleDO>()
                .likeIfPresent(StoreProductRuleDO::getRuleName, reqVO.getRuleName())
                .orderByDesc(StoreProductRuleDO::getId));
    }



}
