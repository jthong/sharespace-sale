package com.cloudsteward.module.member.service.productorder;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.app.order.vo.WxPayOrderRespVO;
import com.cloudsteward.module.member.controller.app.productorder.vo.*;
import com.cloudsteward.module.member.dal.dataobject.payorder.PayOrderDO;

import java.util.List;

public interface ProductOrderService {

    /**
     * 创建订单
     * @param reqVo reqVo
     * @return map
     */
    WxPayOrderRespVO createOrder(AppSaveOrderReqVO reqVo);

    void cancelPay(AppCancelPayReqVO reqVo, boolean isAdmin);

    PageResult<AppUserOrderPageRespVO> userOrderByPage(AppUserOrderPageReqVO reqVo);

    List<AppHaveOrderStoreRespVO> selectHaveOrderStore();

    WxPayOrderRespVO pay(Long orderId);

    // 商家经营管理调用
    PageResult<AppUserOrderPageRespVO> managerProductOrder(AppUserOrderPageReqVO reqVo);

    // 商家完成订单
    void finishOrder(Long id);

    AppUserOrderPageRespVO orderInfo(Long orderId);

    String getPhone(Long orderId);

    void paymentOrder(PayOrderDO payOrderDO);

}
