package com.cloudsteward.module.member.service.storepayconfig;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.tenant.core.context.TenantContextHolder;
import com.cloudsteward.module.member.controller.admin.storepayconfig.vo.*;
import com.cloudsteward.module.member.convert.storepayconfig.StorePayConfigConvert;
import com.cloudsteward.module.member.dal.dataobject.storepayconfig.StorePayConfigDO;
import com.cloudsteward.module.member.dal.mysql.member.StoreWxpayConfigMapper;
import com.cloudsteward.module.member.dal.mysql.storepayconfig.StorePayConfigMapper;
import com.cloudsteward.module.member.service.wx.MiniappConfigVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.DATA_NOT_EXISTS;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.STORE_PAY_CONFIG_EXISTS;

/**
 * 门店支付收款配置 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class StorePayConfigServiceImpl implements StorePayConfigService {

    @Resource
    private StorePayConfigMapper storePayConfigMapper;

    @Resource
    private StoreWxpayConfigMapper storeWxpayConfigMapper;

    @Override
    public Long createStorePayConfig(StorePayConfigCreateReqVO createReqVO) {
        //如果门店已经存在了 就不允许重复添加
        StorePayConfigDO storePayConfig = storePayConfigMapper.getByStoreId(createReqVO.getStoreId());
        if (!ObjectUtils.isEmpty(storePayConfig)) {
            throw exception(STORE_PAY_CONFIG_EXISTS);
        }
        createReqVO.setMchId(createReqVO.getMchId().trim());
        // 插入
        storePayConfig = StorePayConfigConvert.INSTANCE.convert(createReqVO);
        //设置APPID
        MiniappConfigVO miniappConfig = storeWxpayConfigMapper.getMiniappConfig(TenantContextHolder.getTenantId());
        storePayConfig.setAppId(miniappConfig.getMiniappId());
        storePayConfigMapper.insert(storePayConfig);
        // 返回
        return storePayConfig.getId();
    }

    @Override
    @Transactional
    public void updateStorePayConfig(StorePayConfigUpdateReqVO updateReqVO) {
        // 校验存在
        validateStorePayConfigExists(updateReqVO.getId());
        updateReqVO.setMchId(updateReqVO.getMchId().trim());
        // 更新
        StorePayConfigDO updateObj = StorePayConfigConvert.INSTANCE.convert(updateReqVO);
        storePayConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteStorePayConfig(Long id) {
        // 校验存在
        validateStorePayConfigExists(id);
        // 删除
        storePayConfigMapper.deleteById(id);
    }

    private void validateStorePayConfigExists(Long id) {
        if (storePayConfigMapper.selectById(id) == null) {
            throw exception(DATA_NOT_EXISTS);
        }
    }

    @Override
    public StorePayConfigDO getStorePayConfig(Long id) {
        return storePayConfigMapper.selectById(id);
    }

    @Override
    public List<StorePayConfigDO> getStorePayConfigList(Collection<Long> ids) {
        return storePayConfigMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<StorePayConfigRespVO> getStorePayConfigPage(StorePayConfigPageReqVO reqVO) {
        IPage<StorePayConfigRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        storePayConfigMapper.getStorePayConfigPage(page, reqVO);
        return new PageResult(page.getRecords(), page.getTotal());
    }

    @Override
    public List<StorePayConfigDO> getStorePayConfigList(StorePayConfigExportReqVO exportReqVO) {
        return storePayConfigMapper.selectList(exportReqVO);
    }

}
