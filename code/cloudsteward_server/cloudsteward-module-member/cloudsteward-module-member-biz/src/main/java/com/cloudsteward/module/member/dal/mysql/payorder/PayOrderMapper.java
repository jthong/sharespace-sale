package com.cloudsteward.module.member.dal.mysql.payorder;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloudsteward.module.member.controller.admin.payorder.vo.PayOrderExportReqVO;
import com.cloudsteward.module.member.controller.admin.payorder.vo.PayOrderPageReqVO;
import com.cloudsteward.module.member.controller.app.callback.vo.PayOrderNotifyReqDTO;
import com.cloudsteward.module.member.controller.app.chart.vo.AppChartDataReqVO;
import com.cloudsteward.module.member.controller.app.manager.vo.AppPayOrderReqVO;
import com.cloudsteward.module.member.controller.app.manager.vo.AppPayOrderRespVO;
import com.cloudsteward.module.member.dal.dataobject.payorder.PayOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付订单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface PayOrderMapper extends BaseMapperX<PayOrderDO> {

    default PageResult<PayOrderDO> selectPage(PayOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PayOrderDO>()
                .likeIfPresent(PayOrderDO::getOrderNo, reqVO.getOrderNo())
                .eqIfPresent(PayOrderDO::getPayStatus, reqVO.getPayStatus())
                .likeIfPresent(PayOrderDO::getPayOrderNo, reqVO.getPayOrderNo())
                .betweenIfPresent(PayOrderDO::getPayTime, reqVO.getPayTime())
                .likeIfPresent(PayOrderDO::getPayRefundNo, reqVO.getPayRefundNo())
                .in(PayOrderDO::getStoreId, reqVO.getStoreIds())
                .betweenIfPresent(PayOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PayOrderDO::getId));
    }

    default List<PayOrderDO> selectList(PayOrderExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PayOrderDO>()
                .likeIfPresent(PayOrderDO::getOrderNo, reqVO.getOrderNo())
                .eqIfPresent(PayOrderDO::getPayStatus, reqVO.getPayStatus())
                .likeIfPresent(PayOrderDO::getPayOrderNo, reqVO.getPayOrderNo())
                .betweenIfPresent(PayOrderDO::getPayTime, reqVO.getPayTime())
                .likeIfPresent(PayOrderDO::getPayRefundNo, reqVO.getPayRefundNo())
                .betweenIfPresent(PayOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PayOrderDO::getId));
    }

    PayOrderDO selectByOrderNoAndPayNo(PayOrderNotifyReqDTO notifyReqDTO);

    PayOrderDO getByPayNo(String payNo);

    PayOrderDO getByOrderNo(String orderNo);

    BigDecimal getMoney(AppChartDataReqVO reqVO);

    List<PayOrderDO> getPreSplit();

    int finishSplit(List<Long> ids);

    List<PayOrderDO> getByOrder(@Param("orderId") Long orderId,@Param("orderNo")  String orderNo);

    IPage<AppPayOrderRespVO> getPayOrderPage(@Param("page") IPage<AppPayOrderRespVO> page, @Param("reqVO") AppPayOrderReqVO reqVO);


}
