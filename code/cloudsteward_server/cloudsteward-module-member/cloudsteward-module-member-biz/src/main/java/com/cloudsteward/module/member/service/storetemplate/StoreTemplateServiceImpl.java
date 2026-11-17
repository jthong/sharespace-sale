package com.cloudsteward.module.member.service.storetemplate;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import com.cloudsteward.module.member.controller.admin.storetemplate.vo.*;
import com.cloudsteward.module.member.dal.dataobject.storetemplate.StoreTemplateDO;
import com.cloudsteward.framework.common.pojo.PageResult;

import com.cloudsteward.module.member.convert.storetemplate.StoreTemplateConvert;
import com.cloudsteward.module.member.dal.mysql.storetemplate.StoreTemplateMapper;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.*;

/**
 * 门店模板 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class StoreTemplateServiceImpl implements StoreTemplateService {

    @Resource
    private StoreTemplateMapper storeTemplateMapper;

    @Override
    public Long createStoreTemplate(StoreTemplateCreateReqVO createReqVO) {
        // 插入
        StoreTemplateDO storeTemplate = StoreTemplateConvert.INSTANCE.convert(createReqVO);
        storeTemplateMapper.insert(storeTemplate);
        // 返回
        return storeTemplate.getTemplateId();
    }

    @Override
    public void updateStoreTemplate(StoreTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        validateStoreTemplateExists(updateReqVO.getTemplateId());
        // 更新
        StoreTemplateDO updateObj = StoreTemplateConvert.INSTANCE.convert(updateReqVO);
        storeTemplateMapper.updateById(updateObj);
    }

    @Override
    public void deleteStoreTemplate(Long id) {
        // 校验存在
        validateStoreTemplateExists(id);
        // 删除
        storeTemplateMapper.deleteById(id);
    }

    private void validateStoreTemplateExists(Long id) {
        if (storeTemplateMapper.selectById(id) == null) {
            throw exception(DATA_NOT_EXISTS);
        }
    }

    @Override
    public StoreTemplateDO getStoreTemplate(Long id) {
        return storeTemplateMapper.selectById(id);
    }

    @Override
    public List<StoreTemplateDO> getStoreTemplateList(Collection<Long> ids) {
        return storeTemplateMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<StoreTemplateDO> getStoreTemplatePage(StoreTemplatePageReqVO pageReqVO) {
        return storeTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<StoreTemplateDO> getStoreTemplateList(StoreTemplateExportReqVO exportReqVO) {
        return storeTemplateMapper.selectList(exportReqVO);
    }

}
