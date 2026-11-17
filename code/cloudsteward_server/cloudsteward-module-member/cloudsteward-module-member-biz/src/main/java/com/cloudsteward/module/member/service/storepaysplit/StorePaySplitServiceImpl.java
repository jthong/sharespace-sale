package com.cloudsteward.module.member.service.storepaysplit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.admin.storepaysplit.vo.*;
import com.cloudsteward.module.member.convert.storepaysplit.StorePaySplitConvert;
import com.cloudsteward.module.member.dal.dataobject.storepayconfig.StorePayConfigDO;
import com.cloudsteward.module.member.dal.dataobject.storepaysplit.StorePaySplitDO;
import com.cloudsteward.module.member.dal.mysql.storepayconfig.StorePayConfigMapper;
import com.cloudsteward.module.member.dal.mysql.storepaysplit.StorePaySplitMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.*;

/**
 * 门店支付分账配置 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class StorePaySplitServiceImpl implements StorePaySplitService {

    @Resource
    private StorePaySplitMapper storePaySplitMapper;

    @Resource
    private StorePayConfigMapper storePayConfigMapper;


    @Override
    @Transactional
    public Long createStorePaySplit(StorePaySplitCreateReqVO createReqVO) {
        StorePayConfigDO storePayConfigDO = storePayConfigMapper.getByStoreId(createReqVO.getStoreId());
        if (ObjectUtils.isEmpty(storePayConfigDO)) {
            throw exception(STORE_PAY_CONFIG_NOT_EXISTS);
        }
        if (!storePayConfigDO.getSplit()) {
            throw exception(STORE_PAY_CONFIG_SPLIT_NOT_OPEN);
        }
        createReqVO.setMchId(createReqVO.getMchId().trim());
        List<StorePaySplitDO> storePaySplitDOList = storePaySplitMapper.getByStoreId(createReqVO.getStoreId());
        //先检查重复
        if (storePaySplitDOList.stream().filter(x -> x.getMchId().equals(createReqVO.getMchId())).count() > 0) {
            throw exception(STORE_PAY_SPLIT_MCHID_EXISTS);
        }
        //算出已经分账的比例
        int sum = storePaySplitDOList.stream().mapToInt(x -> x.getRate()).sum();
        if (sum + createReqVO.getRate() > 100) {
            throw exception(STORE_PAY_SPLIT_RATE_CONFIG_EXISTS, 100 - sum);
        }
        // 插入
        StorePaySplitDO storePaySplit = StorePaySplitConvert.INSTANCE.convert(createReqVO);
        storePaySplitMapper.insert(storePaySplit);
        // 返回
        return storePaySplit.getId();
    }

    @Override
    @Transactional
    public void updateStorePaySplit(StorePaySplitUpdateReqVO updateReqVO) {
        StorePayConfigDO storePayConfigDO = storePayConfigMapper.getByStoreId(updateReqVO.getStoreId());
        if (ObjectUtils.isEmpty(storePayConfigDO)) {
            throw exception(STORE_PAY_CONFIG_NOT_EXISTS);
        }
        if (!storePayConfigDO.getSplit()) {
            throw exception(STORE_PAY_CONFIG_SPLIT_NOT_OPEN);
        }
        updateReqVO.setMchId(updateReqVO.getMchId().trim());
        StorePaySplitDO storePaySplitDO = storePaySplitMapper.selectById(updateReqVO.getId());
        if (!ObjectUtils.isEmpty(storePaySplitDO)) {
            //先直接修改 然后检查比例是否合法
            StorePaySplitDO updateObj = StorePaySplitConvert.INSTANCE.convert(updateReqVO);
            storePaySplitMapper.updateById(updateObj);
            List<StorePaySplitDO> storePaySplitDOList = storePaySplitMapper.getByStoreId(updateReqVO.getStoreId());
            //算出已经分账的比例
            int sum = storePaySplitDOList.stream().mapToInt(x -> x.getRate()).sum();
            if (sum + updateReqVO.getRate() > 100) {
                throw exception(STORE_PAY_SPLIT_RATE_CONFIG_EXISTS, 100 - sum);
            }
        } else {
            throw exception(DATA_NOT_EXISTS);
        }
    }

    @Override
    @Transactional
    public void deleteStorePaySplit(Long id) {
        StorePaySplitDO storePaySplitDO = storePaySplitMapper.selectById(id);
        if(!ObjectUtils.isEmpty(storePaySplitDO)){
            // 删除
            storePaySplitMapper.deleteById(id);
        }
    }

    private void validateStorePaySplitExists(Long id) {
        if (storePaySplitMapper.selectById(id) == null) {
            throw exception(DATA_EXISTS_ERROR);
        }
    }

    @Override
    public StorePaySplitDO getStorePaySplit(Long id) {
        return storePaySplitMapper.selectById(id);
    }

    @Override
    public List<StorePaySplitDO> getStorePaySplitList(Collection<Long> ids) {
        return storePaySplitMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<StorePaySplitRespVO> getStorePaySplitPage(StorePaySplitPageReqVO reqVO) {
        IPage<StorePaySplitRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        storePaySplitMapper.getStorePaySplitPage(page, reqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<StorePaySplitDO> getStorePaySplitList(StorePaySplitExportReqVO exportReqVO) {
        return storePaySplitMapper.selectList(exportReqVO);
    }

}
