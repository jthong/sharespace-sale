package com.cloudsteward.module.member.controller.app.coupon;

import com.cloudsteward.framework.common.pojo.CommonResult;
import com.cloudsteward.framework.idempotent.core.annotation.Idempotent;
import com.cloudsteward.framework.security.core.annotations.PreAuthenticated;
import com.cloudsteward.module.member.controller.app.coupon.vo.AppCouponActiveInfoRespVO;
import com.cloudsteward.module.member.controller.app.coupon.vo.AppCouponActiveReqVO;
import com.cloudsteward.module.member.controller.app.coupon.vo.AppCouponActiveRespVO;
import com.cloudsteward.module.member.service.couponactive.CouponActiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static com.cloudsteward.framework.common.pojo.CommonResult.success;

@Tag(name = "miniapp - 优惠券活动管理")
@RestController
@RequestMapping("/member/couponActive")
@Validated
@Slf4j
public class AppCouponActiveController {


    @Resource
    private CouponActiveService couponActiveService;

    @PostMapping("/getAdminByCouponId")
    @Operation(summary = "获取优惠券活动信息", description = "")
    @PreAuthenticated
    public CommonResult<AppCouponActiveRespVO> getAdminByCouponId(@RequestParam("couponId") Long couponId) {
        return success(couponActiveService.getAdminByCouponId(couponId));
    }

    @PostMapping("/saveAdminByCouponId")
    @Operation(summary = "保存优惠券活动信息", description = "")
    @PreAuthenticated
    public CommonResult<Boolean> saveAdminByCouponId(@RequestBody @Valid AppCouponActiveReqVO reqVO) {
        couponActiveService.saveAdminByCouponId(reqVO);
        return success(true);
    }

    @PostMapping("/stopActive")
    @Operation(summary = "结束领券活动", description = "")
    @PreAuthenticated
    public CommonResult<Boolean> stopActive(@RequestParam("couponId") Long couponId) {
        couponActiveService.stopActive(couponId);
        return success(true);
    }


    @PostMapping("/getById")
    @Operation(summary = "获取活动信息", description = "")
//    @PreAuthenticated
    public CommonResult<AppCouponActiveInfoRespVO> getById(@RequestParam("couponId") Long couponId) {
        return success(couponActiveService.getById(couponId));
    }


    @PostMapping("/putCoupon")
    @Operation(summary = "领取优惠券", description = "")
    @PreAuthenticated
    @Idempotent(timeout = 1, message = "你的点击太快了~")
    public CommonResult<Boolean> putCoupon(@RequestParam("id") Long id) {
        couponActiveService.putCoupon(id);
        return success(true);
    }


}
