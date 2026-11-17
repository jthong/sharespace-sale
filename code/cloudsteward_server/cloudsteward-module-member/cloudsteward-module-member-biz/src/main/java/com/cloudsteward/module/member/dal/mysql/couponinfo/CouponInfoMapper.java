package com.cloudsteward.module.member.dal.mysql.couponinfo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.module.member.controller.app.manager.vo.AppCouponDetailRespVO;
import com.cloudsteward.module.member.controller.app.manager.vo.AppManagerCouponPageReqVO;
import com.cloudsteward.module.member.controller.app.user.vo.AppCouponPageReqVO;
import com.cloudsteward.module.member.controller.app.user.vo.AppCouponPageRespVO;
import com.cloudsteward.module.member.dal.dataobject.couponinfo.CouponInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 优惠券管理 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CouponInfoMapper extends BaseMapperX<CouponInfoDO> {


    Integer countByUserId(Long userId);

    IPage<AppCouponPageRespVO> getCouponPage(@Param("page") IPage<AppCouponPageRespVO> page, @Param("reqVO") AppCouponPageReqVO reqVO);

    CouponInfoDO getByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);

    IPage<AppCouponPageRespVO> getCouponPageByAdmin(@Param("page") IPage<AppCouponPageRespVO> page, @Param("reqVO") AppManagerCouponPageReqVO reqVO, @Param("storeIds") String storeIds);

    AppCouponDetailRespVO getCouponDetail(Long couponId);

    CouponInfoDO getByIdAndAdmin(Long couponId);

    int executeCouponExpire();

    int countNewUserByStoreId(@Param("userId") Long userId, @Param("storeId") Long storeId);

    List<AppCouponPageRespVO> getUserCouponByAdmin(@Param("userId") Long userId, @Param("storeIds") String storeIds);

    int countByUserIdAndKey(@Param("userId") Long userId, @Param("couponKey") String couponKey);

}
