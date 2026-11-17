package com.cloudsteward.module.member.service.payorder;

import com.alibaba.fastjson.JSONObject;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.admin.payorder.vo.PayOrderExportReqVO;
import com.cloudsteward.module.member.controller.admin.payorder.vo.PayOrderPageReqVO;
import com.cloudsteward.module.member.controller.admin.payorder.vo.PayOrderUpdateReqVO;
import com.cloudsteward.module.member.controller.app.order.vo.WxPayOrderRespVO;
import com.cloudsteward.module.member.dal.dataobject.payorder.PayOrderDO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 支付订单 Service 接口
 *
 * @author 芋道源码
 */
public interface PayOrderService {

    /**
     * 获得支付订单
     *
     * @param id 编号
     * @return 支付订单
     */
    PayOrderDO getPayOrder(Long id);


    void refund(PayOrderDO payOrderDO, Integer refundPrice, String desc);
    /**
     * 获得支付订单列表
     *
     * @param ids 编号
     * @return 支付订单列表
     */
    List<PayOrderDO> getPayOrderList(Collection<Long> ids);

    /**
     * 获得支付订单分页
     *
     * @param pageReqVO 分页查询
     * @return 支付订单分页
     */
    PageResult<PayOrderDO> getPayOrderPage(PayOrderPageReqVO pageReqVO);

    /**
     * 获得支付订单列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 支付订单列表
     */
    List<PayOrderDO> getPayOrderList(PayOrderExportReqVO exportReqVO);


    String updateWxOrder(String xmlData);

    String updateWxRefunded(String xmlData);

    void retainPartnerContext(String orderNo);
    boolean completePartnerBusiness(String orderNo,com.cloudsteward.module.member.enums.AppWxPayTypeEnum type);
    void applyPartnerPayment(PayOrderDO payment, com.cloudsteward.module.member.controller.app.order.vo.WxPayOrderInfo context);
    void applyPartnerRefund(PayOrderDO payment, Integer amount);

    WxPayOrderRespVO create(WxPayOrderRespVO respVO,Long userId, String orderNo, Long orderId, Long storeId, Integer payType, String orderDesc, Integer price, String goodsName,String merchantAppUrl);

    PayOrderDO getByOrderNo(String orderNo);

    void checkWxOrder(String orderNo,Long storeId, Integer price);

    void refundDeposit(Long orderId,String orderNo, BigDecimal deposit, Long userId, Long storeId, boolean depositRefund);

    void refundBalance(Long storeId,String orderNo,Long userId);

    void refundByOrder(Long orderId, String orderNo,Long storeId);
    void refundByOrder(Long orderId, String orderNo,Long storeId, int price);

    void update(PayOrderUpdateReqVO reqVO);

    String updateYeepayOrder(JSONObject data);

}
