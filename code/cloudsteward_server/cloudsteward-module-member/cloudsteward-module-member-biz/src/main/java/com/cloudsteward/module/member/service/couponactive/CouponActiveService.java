package com.cloudsteward.module.member.service.couponactive;

import com.cloudsteward.module.member.controller.app.coupon.vo.AppCouponActiveInfoRespVO;
import com.cloudsteward.module.member.controller.app.coupon.vo.AppCouponActiveReqVO;
import com.cloudsteward.module.member.controller.app.coupon.vo.AppCouponActiveRespVO;

/**
 * 优惠券活动 Service 接口
 *
 * @author 超级管理员
 */
public interface CouponActiveService {

    AppCouponActiveRespVO getAdminByCouponId(Long couponId);

    void saveAdminByCouponId(AppCouponActiveReqVO reqVO);

    void stopActive(Long couponId);

    AppCouponActiveInfoRespVO getById(Long couponId);

    void putCoupon(Long id);

}
