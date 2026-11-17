package com.cloudsteward.module.member.service.groupPay;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.controller.admin.groupPay.vo.GroupPayInfoExportReqVO;
import com.cloudsteward.module.member.controller.admin.groupPay.vo.GroupPayInfoPageReqVO;
import com.cloudsteward.module.member.controller.admin.groupPay.vo.GroupPayInfoRespVO;
import com.cloudsteward.module.member.controller.admin.groupPay.vo.GroupPayInfoUpdateReqVO;
import com.cloudsteward.module.member.dal.dataobject.groupPay.GroupPayInfoDO;
import com.cloudsteward.module.member.dal.mysql.groupPay.GroupPayInfoMapper;
import com.cloudsteward.module.member.service.iot.IotGroupPayService;
import com.cloudsteward.module.member.service.iot.groupPay.IotGroupPayConsumeReqVO;
import com.cloudsteward.module.member.service.iot.groupPay.IotGroupPayPrepareReqVO;
import com.cloudsteward.module.member.service.iot.groupPay.IotGroupPayPrepareRespVO;
import com.cloudsteward.module.member.service.wx.WorkWxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.GROUP_NO_CHECK_ERROR;

/**
 * 团购支付信息 Service 实现类
 *
 * @author MrGuan
 */
@Service
@Validated
@Slf4j
public class GroupPayInfoServiceImpl implements GroupPayInfoService {

    @Resource
    private GroupPayInfoMapper groupPayInfoMapper;

    @Resource
    private IotGroupPayService iotGroupPayService;

    @Resource
    private GroupBuyStoreBindingService groupBuyBindings;


    @Resource
    private WorkWxService workWxService;

    @Override
    public GroupPayInfoDO getGroupPayInfo(Long id) {
        return groupPayInfoMapper.selectById(id);
    }

    @Override
    public List<GroupPayInfoDO> getGroupPayInfoList(Collection<Long> ids) {
        return groupPayInfoMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<GroupPayInfoRespVO> getGroupPayInfoPage(GroupPayInfoPageReqVO pageReqVO) {
        IPage<GroupPayInfoRespVO> page = new Page(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        groupPayInfoMapper.getPage(page, pageReqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<GroupPayInfoDO> getGroupPayInfoList(GroupPayInfoExportReqVO exportReqVO) {
        return groupPayInfoMapper.selectList(exportReqVO);
    }

    @Override
    public IotGroupPayPrepareRespVO prepare(Long storeId, Integer platform, String ticketNo) {
        ticketNo = GroupBuyPlatform.normalize(ticketNo);
        platform = GroupBuyPlatform.detect(ticketNo);
        String shopId = groupBuyBindings.requireShopId(storeId);
        return iotGroupPayService.prepare(new IotGroupPayPrepareReqVO()
                .setPlatform(platform)
                .setShopId(shopId)
                .setCode(ticketNo));
    }

    @Override
    public GroupPayInfoDO consume(Long storeId, Integer platform, String ticketNo, IotGroupPayPrepareRespVO vo) {
        ticketNo = GroupBuyPlatform.normalize(ticketNo);
        platform = GroupBuyPlatform.detect(ticketNo);
        String shopId = groupBuyBindings.requireShopId(storeId);
        //保存验券记录
        GroupPayInfoDO groupPayInfoDO = new GroupPayInfoDO();
        groupPayInfoDO.setTicketInfo(vo.getTicketInfo());
        iotGroupPayService.consume(new IotGroupPayConsumeReqVO()
                .setPlatform(platform)
                .setShopId(shopId)
                .setCode(ticketNo)
                .setTicketInfo(vo.getTicketInfo()));
        groupPayInfoDO.setGroupNo(ticketNo);
        groupPayInfoDO.setStoreId(storeId);
        groupPayInfoDO.setGroupPayType(platform);
        groupPayInfoDO.setGroupName(vo.getTicketName());
        groupPayInfoDO.setGroupShopId(vo.getShopId());
        //把单位从分 转化成元
        groupPayInfoDO.setGroupPayPrice(new BigDecimal(vo.getPayAmount() / 100.0));
        groupPayInfoMapper.insert(groupPayInfoDO);
        return groupPayInfoDO;
    }

    @Override
    public void revoke(Long storeId, Integer platform, String ticketNo, String ticketInfo) {
        GroupBuyPlatform.require(platform);
        String shopId = groupBuyBindings.requireShopId(storeId);
        iotGroupPayService.revoke(new IotGroupPayConsumeReqVO()
                .setPlatform(platform)
                .setShopId(shopId)
                .setCode(ticketNo)
                .setTicketInfo(ticketInfo));
    }

    @Override
    @Transactional
    public void update(GroupPayInfoUpdateReqVO reqVO) {
        log.info("管理员：{}，修改团购：{}", getLoginUserId(), reqVO);
        GroupPayInfoDO groupPayInfoDO = groupPayInfoMapper.selectById(reqVO.getId());
        if (!ObjectUtils.isEmpty(groupPayInfoDO)) {
            groupPayInfoDO.setGroupPayPrice(new BigDecimal(reqVO.getGroupPayPrice() / 100.0));
            groupPayInfoMapper.updateById(groupPayInfoDO);
        }
    }

}
