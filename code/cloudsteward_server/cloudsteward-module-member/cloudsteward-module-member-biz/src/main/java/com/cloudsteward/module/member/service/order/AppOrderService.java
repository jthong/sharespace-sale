package com.cloudsteward.module.member.service.order;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.app.order.vo.*;
import com.cloudsteward.module.member.controller.app.manager.vo.AppPayOrderReqVO;
import com.cloudsteward.module.member.controller.app.manager.vo.AppPayOrderRespVO;
import com.cloudsteward.module.member.controller.app.manager.vo.AppPayRefundReqVO;
import com.cloudsteward.module.member.dal.dataobject.couponinfo.CouponInfoDO;
import com.cloudsteward.module.member.dal.dataobject.orderinfo.OrderInfoDO;
import com.cloudsteward.module.member.dal.dataobject.pkginfo.PkgInfoDO;
import com.cloudsteward.module.member.dal.dataobject.roominfo.RoomInfoDO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface AppOrderService {

    WxPayOrderRespVO preOrder(String orderNo, Long userId, Integer payType, Long roomId, Date startTime, Date endTime,
                              CouponInfoDO couponInfoDO, PkgInfoDO pkgInfoDO, Long ignoreOrderId, boolean tongxiao, boolean preSubmit, boolean wxpay, Integer timeIndex);

    boolean checkOrderTime(Integer clearTime, RoomInfoDO roomInfoDO, Date startTime, Date endTime, Long ignoreOrderId);

    BigDecimal mathPrice(int discount, Long storeId, Long userId, BigDecimal price, BigDecimal deposit, BigDecimal tongxiaoPrice, Integer txHour,
                         Date startTime, Date endTime, Boolean nightLong, CouponInfoDO couponInfoDO, PkgInfoDO pkgInfoDO);

    OrderInfoDO save(OrderSaveReqVO reqVO);

    void renew(OrderRenewalReqVO reqVO);

    PageResult<OrderListRespVO> getOrderPage(OrderPageReqVO reqVO);

    OrderInfoAppRespVO getOrderInfo(Long orderId, String orderKey);

    String getRoomImgs(Long roomId);

    void changeRoom(Long orderId, Long roomId);

    void checkGroupNo(String title, Date startTime, Date endTime, Integer roomType, boolean nightLong, Integer txStartHour, Integer txHour);

    void checkPkgUse(PkgInfoDO pkgInfoDO, boolean nightLong, Integer roomType, Long storeId, Date
            startTime, Date endTime, long orderMinutes, Integer roomId);

    void cancelOrder(Long orderId);

    void startOrder(Long orderId);

    void executeOrderJob();

    boolean queryWxOrder(String orderNo);

    List<AppDiscountRulesRespVO> getDiscountRules(Long storeId);

    void executeMeituanRefreshTokenJob();

    void openRoomDoor(String orderKey);

    void openStoreDoor(String orderKey);

    int countByUserAndStoreId(Long userId, Long storeId);

    void lockWxOrder(OrderLockReqVO reqVO);


    void flushRoomStatus(Long roomId, Long ignoreOrderId);

    int countNewUserByStoreId(Long userId, Long storeId);

    OrderInfoAppRespVO getOrderByRoomId(Long roomId);

    void closeOrder(Long orderId);

    AppGroupNoInfoRespVO preGroupNo(PreGroupNoReqVO reqVO);

    // controlKT 和 getLockPwd 已下线（新IoT平台不支持）
    // void controlKT(ControlKTReqVO reqVO);
    // String getLockPwd(String orderKey);

    OrderInfoDO getByOrderId(Long orderId);

    OrderInfoAppRespVO getOrderInfoByNo(String orderKey, String orderNo);

    PageResult<AppPayOrderRespVO> getPage(AppPayOrderReqVO reqVO);

    void refundByApp(AppPayRefundReqVO reqVO);

    OrderInfoAppRespVO getOrderInfoByRoomId(Long roomId);

}
