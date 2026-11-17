package com.cloudsteward.module.member.dal.mysql.couponactive;

import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.module.member.controller.app.coupon.vo.AppCouponActiveInfoRespVO;
import com.cloudsteward.module.member.controller.app.coupon.vo.AppCouponActiveRespVO;
import com.cloudsteward.module.member.dal.dataobject.couponactive.CouponActiveDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券活动 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface CouponActiveMapper extends BaseMapperX<CouponActiveDO> {

    AppCouponActiveRespVO getAdminByCouponId(Long couponId);

    CouponActiveDO getByCouponId(Long couponId);

    AppCouponActiveInfoRespVO getByInfoId(Long id);

}
