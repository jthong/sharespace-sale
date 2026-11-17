package com.cloudsteward.module.member.service.manager;

import com.cloudsteward.framework.common.core.KeyValue;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.admin.user.vo.AppUserRechargeReqVO;
import com.cloudsteward.module.member.controller.app.chart.vo.*;
import com.cloudsteward.module.member.controller.app.clear.vo.AppClearPageReqVO;
import com.cloudsteward.module.member.controller.app.clear.vo.AppClearPageRespVO;
import com.cloudsteward.module.member.controller.app.manager.vo.*;
import com.cloudsteward.module.member.controller.app.order.vo.OrderListRespVO;
import com.cloudsteward.module.member.controller.app.order.vo.OrderPageReqVO;
import com.cloudsteward.module.member.controller.app.order.vo.OrderRenewalReqVO;
import com.cloudsteward.module.member.controller.app.manager.vo.AppPayOrderReqVO;
import com.cloudsteward.module.member.controller.app.manager.vo.AppPayOrderRespVO;
import com.cloudsteward.module.member.controller.app.manager.vo.AppPayRefundReqVO;
import com.cloudsteward.module.member.controller.app.user.vo.*;

import java.math.BigDecimal;
import java.util.List;

public interface AppMangerService {
    PageResult<OrderListRespVO> getOrderPage(OrderPageReqVO reqVO);

    PageResult<AppMemberPageRespVO> getMemberPage(AppMemberPageReqVO reqVO);

    PageResult<AppCouponPageRespVO> getPresentCouponPage(AppPresentCouponPageReqVO reqVO);

    PageResult<AppCouponPageRespVO> getCouponPage(AppManagerCouponPageReqVO reqVO);

    AppCouponDetailRespVO getCouponDetail(Long couponId);

    void saveCouponDetail(AppCouponDetailReqVO reqVO);

    PageResult<AppClearUserPageRespVO> getClearUserPage(AppClearUserPageReqVO reqVO);

    void deleteClearUser(Long storeId,Long userId);

    void saveClearUser(AppClearUserDetailReqVO reqVO);

    void settlementClearUser(AppSettlementClearUserReqVO reqVO);

    void complaintClearInfo(AppComplaintClearInfoReqVO reqVO);

    void applyWithdrawal();

    PageResult<AppWithdrawalPageRespVO> getWithdrawalPage(AppWithdrawalPageReqVO reqVO);

    AppRevenueChartRespVO getRevenueChart(AppChartDataReqVO reqVO);

    AppBusinessStatisticsRespVO getBusinessStatistics(AppChartDataReqVO reqVO);

    List<KeyValue<String, BigDecimal>> getRevenueStatistics(AppChartDataReqVO reqVO);

    List<KeyValue<String, Integer>> getOrderStatistics(AppChartDataReqVO reqVO);

    List<KeyValue<String, Integer>> getMemberStatistics(AppChartDataReqVO reqVO);

    List<KeyValue<String, String>> getRoomUseStatistics(AppChartDataReqVO reqVO);

    List<KeyValue<String, String>> getRoomUseHourStatistics(AppChartDataReqVO reqVO);

    List<AppRoomUseHourVO> getRoomUseHour(AppChartDataReqVO reqVO);

    void giftCoupon(AppGiftCouponReqVO reqVO);

    PageResult<AppAdminUserPageRespVO> getAdminUserPage(AppClearUserPageReqVO reqVO);

    void deleteAdminUser(Long storeId, Long userId);

    void saveAdminUser(AppClearUserDetailReqVO reqVO);

    void renew(OrderRenewalReqVO reqVO);

    PageResult<AppClearPageRespVO> getClearManagerPage(AppClearPageReqVO reqVO);

    void cancelOrder(Long orderId,boolean refund);

    void useGroupNo(AppUseGroupNoReqVO reqVO);

    void changeOrderUser(AppChangeOrderUserReqVO reqVO);

    void changeOrder(OrderChangeReqVO reqVO);

    List<AppIncomeStatisticsRespVO> getIncomeStatistics(AppChartDataReqVO reqVO);

    List<AppRechargeStatisticsRespVO> getRechargeStatistics(AppChartDataReqVO reqVO);

    Long submitOrder(OrderSubmitReqVO reqVO);

    void recharge(AppUserRechargeReqVO reqVO);

    void cancelClear(Long clearId);

    List<AppCouponPageRespVO> getUserCouponByAdmin(AppManagerUserCouponListReqVO reqVO);

    void revokeCoupon(Long id);




    PageResult<AppVipPageRespVO> getVipPage(AppVipPageReqVO reqVO);

    void depositRefund(Long orderId);


    void deleteVip(AppVipDeleteReqVO reqVO);


    void deleteCoupon(Long couponId);

    PageResult<AppPayOrderRespVO> getPayOrderPage(AppPayOrderReqVO reqVO);

    void refundPayOrder(AppPayRefundReqVO reqVO);

}
