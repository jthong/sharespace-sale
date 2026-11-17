package com.cloudsteward.module.member.service.storetemplate;

import java.util.*;
import javax.validation.*;
import com.cloudsteward.module.member.controller.admin.storetemplate.vo.*;
import com.cloudsteward.module.member.dal.dataobject.storetemplate.StoreTemplateDO;
import com.cloudsteward.framework.common.pojo.PageResult;

/**
 * 门店模板 Service 接口
 *
 * @author 超级管理员
 */
public interface StoreTemplateService {

    /**
     * 创建门店模板
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStoreTemplate(@Valid StoreTemplateCreateReqVO createReqVO);

    /**
     * 更新门店模板
     *
     * @param updateReqVO 更新信息
     */
    void updateStoreTemplate(@Valid StoreTemplateUpdateReqVO updateReqVO);

    /**
     * 删除门店模板
     *
     * @param id 编号
     */
    void deleteStoreTemplate(Long id);

    /**
     * 获得门店模板
     *
     * @param id 编号
     * @return 门店模板
     */
    StoreTemplateDO getStoreTemplate(Long id);

    /**
     * 获得门店模板列表
     *
     * @param ids 编号
     * @return 门店模板列表
     */
    List<StoreTemplateDO> getStoreTemplateList(Collection<Long> ids);

    /**
     * 获得门店模板分页
     *
     * @param pageReqVO 分页查询
     * @return 门店模板分页
     */
    PageResult<StoreTemplateDO> getStoreTemplatePage(StoreTemplatePageReqVO pageReqVO);

    /**
     * 获得门店模板列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 门店模板列表
     */
    List<StoreTemplateDO> getStoreTemplateList(StoreTemplateExportReqVO exportReqVO);

}
