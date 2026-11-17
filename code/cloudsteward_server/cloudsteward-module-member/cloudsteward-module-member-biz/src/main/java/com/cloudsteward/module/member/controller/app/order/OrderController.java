package com.cloudsteward.module.member.controller.app.order;

import com.cloudsteward.framework.common.pojo.CommonResult;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.idempotent.core.annotation.Idempotent;
import com.cloudsteward.framework.security.core.annotations.PreAuthenticated;
import com.cloudsteward.module.member.controller.app.order.vo.*;
import com.cloudsteward.module.member.dal.dataobject.couponinfo.CouponInfoDO;
import com.cloudsteward.module.member.dal.dataobject.orderinfo.OrderInfoDO;
import com.cloudsteward.module.member.dal.dataobject.pkginfo.PkgInfoDO;
import com.cloudsteward.module.member.dal.mysql.couponinfo.CouponInfoMapper;
import com.cloudsteward.module.member.dal.mysql.pkginfo.PkgInfoMapper;
import com.cloudsteward.module.member.service.order.AppOrderService;
import com.cloudsteward.module.member.service.order.vo.GroupPayTimeReqVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.framework.common.pojo.CommonResult.success;
import static com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.OPRATION_ERROR;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.ORDER_PAGE_PARAM_ERROR;

/**
 * @PACKAGE_NAME: com.cloudsteward.module.member.controller.app.order
 * @DESCRIPTION:
 * @USER: MrGuan  mrguan@aliyun.com
 * @DATE: 2023/7/26 14:25
 */
@Tag(name = "miniapp - 我的订单/下单/续费")
@RestController
@RequestMapping("/member/order")
@Validated
@Slf4j
public class OrderController {

    @Resource
    private AppOrderService appOrderService;

    @Resource
    private CouponInfoMapper couponInfoMapper;

    @Resource
    private PkgInfoMapper pkgInfoMapper;

    @PostMapping("/preOrder")
    @Operation(summary = "预下单,预订和续费前需调用此接口，会返回需要支付的价格以及微信支付需要的参数", description = "下单使用")
    @PreAuthenticated
    public CommonResult<WxPayOrderRespVO> preOrder(@RequestBody @Valid OrderPreReqVO reqVO) {
        CouponInfoDO couponInfoDO = null;
        if (!ObjectUtils.isEmpty(reqVO.getCouponId())) {
            couponInfoDO = couponInfoMapper.selectById(reqVO.getCouponId());
        }
        PkgInfoDO pkgInfoDO = null;
        if (!ObjectUtils.isEmpty(reqVO.getPkgId())) {
            pkgInfoDO = pkgInfoMapper.selectById(reqVO.getPkgId());
        }
        return success(appOrderService.preOrder(null, getLoginUserId(), reqVO.getPayType(), reqVO.getRoomId(), reqVO.getStartTime(), reqVO.getEndTime(),
                couponInfoDO, pkgInfoDO, reqVO.getOrderId(), reqVO.isNightLong(), reqVO.isPreSubmit(), reqVO.isWxPay(), reqVO.getTimeIndex()));
    }


    @PostMapping("/lockWxOrder")
    @Operation(summary = "锁定微信支付订单,避免房间被占用时无法下单成功", description = "下单使用")
    @PreAuthenticated
    public CommonResult<Boolean> lockWxOrder(@RequestBody @Valid OrderLockReqVO reqVO) {
        appOrderService.lockWxOrder(reqVO);
        return success(true);
    }

    @PostMapping("/preGroupNo")
    @Operation(summary = "查询团购券信息", description = "下单使用")
    @PreAuthenticated
    public CommonResult<AppGroupNoInfoRespVO> preGroupNo(@RequestBody @Valid PreGroupNoReqVO reqVO) {
        return success(appOrderService.preGroupNo(reqVO));
    }


    @PostMapping("/save")
    @Operation(summary = "提交订单", description = "下单使用")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Long> save(@RequestBody @Valid OrderSaveReqVO reqVO) {
        //注意！如果是接口提交订单，不允许提交支付类型=0和5的  不然会有安全隐患
        if (reqVO.getPayType().compareTo(0) == 0 || reqVO.getPayType().compareTo(5) == 0) {
            throw exception(OPRATION_ERROR);
        }
        OrderInfoDO orderInfo = appOrderService.save(reqVO);
        return success(orderInfo.getOrderId());
    }

    @PostMapping("/renew")
    @Operation(summary = "订单续费", description = "订单详情使用")
    @PreAuthenticated
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> renew(@RequestBody @Valid OrderRenewalReqVO reqVO) {
        appOrderService.renew(reqVO);
        return success(true);
    }


    @PostMapping("/getOrderByRoomId/{roomId}")
    @Operation(summary = "房间码续费获取订单详情", description = "订单详情使用")
    @PermitAll
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    @Parameter(name = "roomId")
    public CommonResult<OrderInfoAppRespVO> getOrderByRoomId(@PathVariable("roomId") Long roomId) {
        return success(appOrderService.getOrderByRoomId(roomId));
    }

    @PostMapping("/getOrderPage")
    @Operation(summary = "获取订单列表分页", description = "我的订单使用")
    @PreAuthenticated
    public CommonResult<PageResult<OrderListRespVO>> getOrderPage(@RequestBody @Valid OrderPageReqVO reqVO) {
        //参数检查
        if (!ObjectUtils.isEmpty(reqVO.getOrderColumn())) {
            if (reqVO.getOrderColumn().equals("createTime") || reqVO.getOrderColumn().equals("startTime")) {
                //正确

            } else {
                throw exception(ORDER_PAGE_PARAM_ERROR);
            }
        }
        reqVO.setUserId(getLoginUserId());
        return success(appOrderService.getOrderPage(reqVO));
    }

    @GetMapping("/getOrderInfo")
    @Operation(summary = "获取订单详情", description = "我的订单使用")
//    @PreAuthenticated
    @PermitAll//因为有分享订单功能 所以取消权限校验 通过参数校验
    @Parameter(name = "orderId", required = false)
    @Parameter(name = "orderKey", required = false)
    @Deprecated
    public CommonResult<OrderInfoAppRespVO> getOrderInfo(@RequestParam(name = "orderId", required = false) Long orderId
            , @RequestParam(value = "orderKey", required = false) String orderKey) {
        return success(appOrderService.getOrderInfo(orderId, orderKey));
    }

    @GetMapping("/getOrderInfoByNo")
    @Operation(summary = "获取订单详情", description = "我的订单使用")
//    @PreAuthenticated
    @PermitAll//因为有分享订单功能 所以取消权限校验 通过参数校验
    @Parameter(name = "orderNo", required = false)
    @Parameter(name = "orderKey", required = false)
    public CommonResult<OrderInfoAppRespVO> getOrderInfoByNo(@RequestParam(value = "orderKey", required = false) String orderKey
            , @RequestParam(value = "orderNo", required = false) String orderNo) {
        return success(appOrderService.getOrderInfoByNo(orderKey, orderNo));
    }

    @GetMapping("/getOrderInfoByRoomId")
    @Operation(summary = "获取用户在当前房间的订单详情", description = "我的订单使用")
    @PreAuthenticated
//    @PermitAll//因为有分享订单功能 所以取消权限校验 通过参数校验
    public CommonResult<OrderInfoAppRespVO> getOrderInfoByRoomId(@RequestParam(value = "roomId", required = false) Long roomId) {
        return success(appOrderService.getOrderInfoByRoomId(roomId));
    }


    @PostMapping("/cancelOrder/{orderId}")
    @Operation(summary = "取消订单 ", description = "我的订单使用")
    @PreAuthenticated
    @Parameter(name = "orderId")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> cancelOrder(@PathVariable("orderId") Long orderId) {
        appOrderService.cancelOrder(orderId);
        return success(true);
    }

    @PostMapping("/closeOrder/{orderId}")
    @Operation(summary = "提前结束订单 ", description = "我的订单使用")
    @PreAuthenticated
    @Parameter(name = "orderId")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> closeOrder(@PathVariable("orderId") Long orderId) {
        appOrderService.closeOrder(orderId);
        return success(true);
    }

    @PostMapping("/openStoreDoor")
    @Operation(summary = "(开关)门店的大门", description = "我的订单使用")
    @PermitAll//因为有分享订单功能 所以取消权限校验
    @Parameter(name = "orderKey", required = false)
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> openStoreDoor(@RequestParam(value = "orderKey", required = true) String orderKey) {
        appOrderService.openStoreDoor(orderKey);
        return success(true);
    }

    @PostMapping("/openRoomDoor")
    @Operation(summary = "(开)房间电源及灯光", description = "我的订单使用")
    @PermitAll//因为有分享订单功能 所以取消权限校验
    @Parameter(name = "orderKey", required = false)
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> openRoomDoor(@RequestParam(value = "orderKey", required = true) String orderKey) {
        appOrderService.openRoomDoor(orderKey);
        return success(true);
    }


    // controlKT 接口已下线（新IoT平台不支持）
    // @PostMapping("/controlKT")
    // @Operation(summary = "控制空调", description = "我的订单使用")
    // @PermitAll
    // @Idempotent(timeout = 100, timeUnit = TimeUnit.MILLISECONDS, message = "你的点击太快啦~")
    // public CommonResult<Boolean> controlKT(@RequestBody @Valid ControlKTReqVO reqVO) {
    //     appOrderService.controlKT(reqVO);
    //     return success(true);
    // }

    @GetMapping("/getRoomImgs/{roomId}")
    @Operation(summary = "获取房间的图片组 逗号分隔", description = "我的订单使用")
    @PreAuthenticated
    @Parameter(name = "roomId")
    public CommonResult<String> getRoomImgs(@PathVariable("roomId") Long roomId) {
        return success(appOrderService.getRoomImgs(roomId));
    }

    @PostMapping("/changeRoom/{orderId}/{roomId}")
    @Operation(summary = "更换房间 - 提交更换", description = "我的订单使用")
    @PreAuthenticated
    @Parameter(name = "orderId")
    @Parameter(name = "roomId")
    @Idempotent(timeout = 3, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    public CommonResult<Boolean> changeRoom(@PathVariable("orderId") Long orderId, @PathVariable("roomId") Long roomId) {
        appOrderService.changeRoom(orderId, roomId);
        return success(true);
    }

    @GetMapping("/getDiscountRules/{storeId}")
    @Operation(summary = "获取指定门店的充值优惠信息列表")
    @PermitAll
    @Parameter(name = "storeId")
    public CommonResult<List<AppDiscountRulesRespVO>> getDiscountRules(@PathVariable("storeId") Long storeId) {
        return success(appOrderService.getDiscountRules(storeId));
    }

    // getLockPwd 接口已下线（新IoT平台不支持）
    // @PostMapping("/getLockPwd")
    // @Operation(summary = "获取智能锁随机密码", description = "我的订单")
    // @PermitAll
    // @Idempotent(timeout = 5, timeUnit = TimeUnit.SECONDS, message = "你的点击太快啦~")
    // public CommonResult<String> getLockPwd(@RequestParam(value = "orderKey", required = true) String orderKey) {
    //     return success(appOrderService.getLockPwd(orderKey));
    // }


}
