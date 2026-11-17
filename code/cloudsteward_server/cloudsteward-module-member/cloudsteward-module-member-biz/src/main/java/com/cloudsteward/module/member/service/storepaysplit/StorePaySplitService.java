package com.cloudsteward.module.member.service.storepaysplit;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.admin.storepaysplit.vo.*;
import com.cloudsteward.module.member.dal.dataobject.storepaysplit.StorePaySplitDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 门店支付分账配置 Service 接口
 *
 * @author 超级管理员
 */
public interface StorePaySplitService {

    /**
     * 创建门店支付分账配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStorePaySplit(@Valid StorePaySplitCreateReqVO createReqVO);

    /**
     * 更新门店支付分账配置
     *
     * @param updateReqVO 更新信息
     */
    void updateStorePaySplit(@Valid StorePaySplitUpdateReqVO updateReqVO);

    /**
     * 删除门店支付分账配置
     *
     * @param id 编号
     */
    void deleteStorePaySplit(Long id);

    /**
     * 获得门店支付分账配置
     *
     * @param id 编号
     * @return 门店支付分账配置
     */
    StorePaySplitDO getStorePaySplit(Long id);

    /**
     * 获得门店支付分账配置列表
     *
     * @param ids 编号
     * @return 门店支付分账配置列表
     */
    List<StorePaySplitDO> getStorePaySplitList(Collection<Long> ids);

    /**
     * 获得门店支付分账配置分页
     *
     * @param pageReqVO 分页查询
     * @return 门店支付分账配置分页
     */
    PageResult<StorePaySplitRespVO> getStorePaySplitPage(StorePaySplitPageReqVO pageReqVO);

    /**
     * 获得门店支付分账配置列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 门店支付分账配置列表
     */
    List<StorePaySplitDO> getStorePaySplitList(StorePaySplitExportReqVO exportReqVO);

}
