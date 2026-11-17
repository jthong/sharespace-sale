package com.cloudsteward.module.member.service.storepayconfig;

import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.admin.storepayconfig.vo.*;
import com.cloudsteward.module.member.dal.dataobject.storepayconfig.StorePayConfigDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 门店支付收款配置 Service 接口
 *
 * @author 超级管理员
 */
public interface StorePayConfigService {

    /**
     * 创建门店支付收款配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStorePayConfig(@Valid StorePayConfigCreateReqVO createReqVO);

    /**
     * 更新门店支付收款配置
     *
     * @param updateReqVO 更新信息
     */
    void updateStorePayConfig(@Valid StorePayConfigUpdateReqVO updateReqVO);

    /**
     * 删除门店支付收款配置
     *
     * @param id 编号
     */
    void deleteStorePayConfig(Long id);

    /**
     * 获得门店支付收款配置
     *
     * @param id 编号
     * @return 门店支付收款配置
     */
    StorePayConfigDO getStorePayConfig(Long id);

    /**
     * 获得门店支付收款配置列表
     *
     * @param ids 编号
     * @return 门店支付收款配置列表
     */
    List<StorePayConfigDO> getStorePayConfigList(Collection<Long> ids);

    /**
     * 获得门店支付收款配置分页
     *
     * @param pageReqVO 分页查询
     * @return 门店支付收款配置分页
     */
    PageResult<StorePayConfigRespVO> getStorePayConfigPage(StorePayConfigPageReqVO pageReqVO);

    /**
     * 获得门店支付收款配置列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 门店支付收款配置列表
     */
    List<StorePayConfigDO> getStorePayConfigList(StorePayConfigExportReqVO exportReqVO);

}
