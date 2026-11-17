package com.cloudsteward.module.member.service.storeinfo;

import com.cloudsteward.module.member.service.clear.CleaningOrderFinished;
import org.springframework.context.ApplicationEventPublisher;

import cn.binarywang.wx.miniapp.api.WxMaQrcodeService;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.scheme.WxMaGenerateNfcSchemeRequest;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudsteward.framework.common.core.KeyValue;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.common.util.date.DateUtils;
import com.cloudsteward.framework.web.core.util.WebFrameworkUtils;
import com.cloudsteward.module.infra.api.config.ConfigApi;
import com.cloudsteward.module.infra.api.file.FileApi;
import com.cloudsteward.module.member.controller.admin.storeinfo.vo.*;
import com.cloudsteward.module.member.controller.admin.user.vo.AppUserCreateReqVO;
import com.cloudsteward.module.member.controller.app.manager.vo.AppVipBlacklistRespVO;
import com.cloudsteward.module.member.controller.app.store.vo.*;
import com.cloudsteward.module.member.convert.discountrules.DiscountRulesConvert;
import com.cloudsteward.module.member.convert.roominfo.RoomInfoConvert;
import com.cloudsteward.module.member.convert.storeinfo.StoreInfoConvert;
import com.cloudsteward.module.member.dal.dataobject.clearinfo.ClearInfoDO;
import com.cloudsteward.module.member.dal.dataobject.deviceinfo.DeviceInfoDO;
import com.cloudsteward.module.member.dal.dataobject.discountrules.DiscountRulesDO;
import com.cloudsteward.module.member.dal.dataobject.orderinfo.OrderInfoDO;
import com.cloudsteward.module.member.dal.dataobject.roominfo.RoomInfoDO;
import com.cloudsteward.module.member.dal.dataobject.storeinfo.StoreInfoDO;
import com.cloudsteward.module.member.dal.dataobject.storemeituaninfo.StoreMeituanInfoDO;
import com.cloudsteward.module.member.dal.dataobject.storepayconfig.StorePayConfigDO;
import com.cloudsteward.module.member.dal.dataobject.storepaysplit.StorePaySplitDO;
import com.cloudsteward.module.member.dal.dataobject.storesound.StoreSoundInfoDO;
import com.cloudsteward.module.member.dal.dataobject.storetemplate.StoreTemplateDO;
import com.cloudsteward.module.member.dal.dataobject.storeuser.StoreUserDO;
import com.cloudsteward.module.member.dal.dataobject.user.MemberUserDO;
import com.cloudsteward.module.member.dal.mysql.clearinfo.ClearInfoMapper;
import com.cloudsteward.module.member.dal.mysql.deviceinfo.DeviceInfoMapper;
import com.cloudsteward.module.member.dal.mysql.discountrules.DiscountRulesMapper;
import com.cloudsteward.module.member.dal.mysql.orderinfo.OrderInfoMapper;
import com.cloudsteward.module.member.dal.mysql.roominfo.RoomInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storemeituaninfo.StoreMeituanInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storepayconfig.StorePayConfigMapper;
import com.cloudsteward.module.member.dal.mysql.storepaysplit.StorePaySplitMapper;
import com.cloudsteward.module.member.dal.mysql.storesound.StoreSoundInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storetemplate.StoreTemplateMapper;
import com.cloudsteward.module.member.dal.mysql.storeuser.StoreUserMapper;
import com.cloudsteward.module.member.dal.mysql.user.MemberUserMapper;
import com.cloudsteward.module.member.enums.AppEnum;
import com.cloudsteward.module.member.service.device.DeviceService;
import com.cloudsteward.module.member.service.iot.IotGroupPayService;
import com.cloudsteward.module.member.service.iot.groupPay.IotGroupPayScopeUrlReqVO;
import com.cloudsteward.module.member.service.iot.IotService;
import com.cloudsteward.module.member.service.order.AppOrderService;
import com.cloudsteward.module.member.service.user.AppUserService;
import com.cloudsteward.module.member.service.user.MemberUserService;
import com.cloudsteward.module.member.service.wx.MiniappConfigVO;
import com.cloudsteward.module.member.service.wx.MyWxService;
import com.cloudsteward.module.member.service.wx.WorkWxService;
import com.cloudsteward.module.system.api.tenant.TenantApi;
import com.cloudsteward.module.system.api.tenant.dto.TenantBean;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserType;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getTenantId;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.*;

/**
 * 门店管理 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class StoreInfoServiceImpl implements StoreInfoService {
    @Resource
    private com.cloudsteward.framework.security.core.service.SecurityFrameworkService pcSecurity;

    @Resource
    private StoreInfoMapper storeInfoMapper;

    @Resource
    private StoreUserMapper storeUserMapper;

    @Resource
    private RoomInfoMapper roomInfoMapper;

    @Resource
    private DiscountRulesMapper discountRulesMapper;

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private DeviceService deviceService;

    @Resource
    private AppUserService appUserService;
    @Resource
    private ClearInfoMapper clearInfoMapper;

    @Resource
    private StoreMeituanInfoMapper storeMeituanInfoMapper;

    @Resource
    private StoreSoundInfoMapper storeSoundInfoMapper;

    @Resource
    private DeviceInfoMapper deviceInfoMapper;
    @Resource
    private AppOrderService appOrderService;
    @Resource
    private MemberUserMapper memberUserMapper;
    @Resource
    private MemberUserService memberUserService;

    @Resource
    private StoreTemplateMapper storeTemplateMapper;
    @Resource
    private FileApi fileApi;

    @Resource
    private TenantApi tenantApi;

    @Resource
    private WorkWxService workWxService;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Resource
    private MyWxService myWxService;

    @Resource
    private ConfigApi configApi;

    @Resource
    private StorePayConfigMapper storePayConfigMapper;

    @Resource
    private StorePaySplitMapper storePaySplitMapper;

    private String hostname = "app.hostname";

    private String getRedirectUrl() {
        return String.format("https://%s/app-api/callback/wxpay/update", configApi.getConfigValueByKey(hostname));
    }


    private String getAppKey() {
        String value = configApi.getConfigValueByKey("app.meituan");
        JSONObject jsonObject = JSONObject.parseObject(value);
        return jsonObject.getString("appKey");
    }

    private String getSecret() {
        String value = configApi.getConfigValueByKey("app.meituan");
        JSONObject jsonObject = JSONObject.parseObject(value);
        return jsonObject.getString("secret");
    }

    @Resource
    private IotGroupPayService iotGroupPayService;


    @Resource
    private com.cloudsteward.module.member.service.groupPay.GroupBuyStoreBindingService groupBuyBindings;

    @Resource
    private IotService iotService;


    @Override
    public PageResult<AppStoreAdminRespVO> getPageList(AppStoreAdminReqVO reqVO) {
        reqVO.setUserId(getLoginUserId());
        IPage<AppStoreAdminRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        storeInfoMapper.getPageList(page, reqVO);
        return new PageResult(page.getRecords(), page.getTotal());
    }

    @Override
    public AppStoreInfoRespVO getDetail(Long storeId) {
        StoreInfoDO storeInfoDO = storeInfoMapper.selectById(storeId);
        AppStoreInfoRespVO appStoreInfoRespVO = StoreInfoConvert.INSTANCE.convert2(storeInfoDO);
        return appStoreInfoRespVO;
    }

    @Override
    @Transactional
    public void save(AppStoreInfoReqVO reqVO) {
        if (ObjectUtils.isEmpty(reqVO.getStoreId())) {
            // 校验用户类型
            checkPermission(null, null, getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());
            //新增
            StoreInfoDO storeInfoDO = StoreInfoConvert.INSTANCE.convert3(reqVO);
            storeInfoDO.setStatus(1);//默认禁用
            storeInfoMapper.insert(storeInfoDO);
            //还要保存一个门店关系
            StoreUserDO storeUserDO = new StoreUserDO();
            storeUserDO.setStoreId(storeInfoDO.getStoreId());
            storeUserDO.setUserId(getLoginUserId());
            storeUserDO.setType(AppEnum.member_user_type.BOSS.getValue());
            storeUserDO.setGiftBalance(BigDecimal.valueOf(999999));
            storeUserMapper.insert(storeUserDO);
            //生成小程序码
            WxMaService wxMaService = myWxService.initWxMa();
            // 获取小程序二维码生成实例
            try {
                String path = "pages/index/index?storeId=" + storeInfoDO.getStoreId();
                WxMaQrcodeService wxMaQrcodeService = wxMaService.getQrcodeService();
                byte[] bytes = generateMiniQrCode(path, wxMaQrcodeService);
                String file = fileApi.createFile(bytes);
                storeInfoMapper.updateById(new StoreInfoDO().setStoreId(storeInfoDO.getStoreId()).setQrCode(file));
            } catch (WxErrorException e) {
                log.error("生成小程序码异常", e);
//                throw new RuntimeException(e);
            }
        } else {
            //修改
            //校验门店权限
            checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());
            StoreInfoDO storeInfoDO = StoreInfoConvert.INSTANCE.convert3(reqVO);
            StoreInfoDO infoDO = storeInfoMapper.selectById(reqVO.getStoreId());
            if (ObjectUtils.isEmpty(infoDO.getQrCode())) {
                //生成小程序码
                WxMaService wxMaService = myWxService.initWxMa();
                // 获取小程序二维码生成实例
                try {
                    String path = "pages/index/index?storeId=" + storeInfoDO.getStoreId();
                    WxMaQrcodeService wxMaQrcodeService = wxMaService.getQrcodeService();
                    byte[] bytes = generateMiniQrCode(path, wxMaQrcodeService);
                    String file = fileApi.createFile(bytes);
                    storeInfoDO.setQrCode(file);
                } catch (WxErrorException e) {
                    log.error("生成小程序码异常", e);
                    //                throw new RuntimeException(e);
                }
            }
            storeInfoMapper.updateById(storeInfoDO);
        }
    }

    @Override
    public List<AppRoomListRespVO> getRoomInfoList(Long storeId) {
        List<AppRoomListRespVO> roomInfoList = roomInfoMapper.getRoomInfoList(storeId, getLoginUserId());
        if (!CollectionUtils.isEmpty(roomInfoList)) {
            if (deviceService.countGateway(storeId) > 0) {
                roomInfoList.forEach(x -> x.setGatewayId(1L));
            }
        }
        return roomInfoList;
    }

    @Override
    public AppRoomDetailRespVO getRoomDetail(Long roomId) {
        RoomInfoDO roomInfoDO = roomInfoMapper.selectById(roomId);
        return RoomInfoConvert.INSTANCE.convert2(roomInfoDO);
    }

    @Override
    @Transactional
    public void saveRoomDetail(AppRoomDetailReqVO reqVO) {
        if (ObjectUtils.isEmpty(reqVO.getTongxiaoPrice())) {
            //没有填通宵场价格  那么默认设置为单价*6个小时
            reqVO.setTongxiaoPrice(reqVO.getPrice().multiply(BigDecimal.valueOf(6)));
        }
        if (ObjectUtils.isEmpty(reqVO.getWorkPrice())) {
            reqVO.setWorkPrice(reqVO.getPrice());
        }
        if (ObjectUtils.isEmpty(reqVO.getYunlabaSound())) {
            //默认设置音量为2
            reqVO.setYunlabaSound(2);
        }
        if (!StringUtils.isEmpty(reqVO.getBanTimeStart()) && StringUtils.isEmpty(reqVO.getBanTimeEnd())) {
            //任意一个为空都不行
            throw exception(ROOM_BAN_TIME_ERROR);

        } else if (!StringUtils.isEmpty(reqVO.getBanTimeEnd()) && StringUtils.isEmpty(reqVO.getBanTimeStart())) {
            //任意一个为空都不行
            throw exception(ROOM_BAN_TIME_ERROR);
        }
        reqVO.setPreUnit(1);//单位先默认为1分钟
        if (!ObjectUtils.isEmpty(reqVO.getPrePrice())) {
            if (ObjectUtils.isEmpty(reqVO.getMinCharge())) {
                throw exception(ROOM_PRE_CONFIG_ERROR);
            }
        }
        if (!ObjectUtils.isEmpty(reqVO.getMinCharge())) {
            if (ObjectUtils.isEmpty(reqVO.getPrePrice())) {
                throw exception(ROOM_PRE_CONFIG_ERROR);
            }
        }

        if (ObjectUtils.isEmpty(reqVO.getRoomId())) {
            //新增
            RoomInfoDO roomInfoDO = RoomInfoConvert.INSTANCE.convert3(reqVO);
            roomInfoDO.setReserve(reqVO.getReserve()); // 转换属性丢失
            roomInfoMapper.insert(roomInfoDO);
            //门店的房间数量+1
            StoreInfoDO storeInfoDO = storeInfoMapper.selectById(reqVO.getStoreId());
            storeInfoDO.setRoomNum(storeInfoDO.getRoomNum() + 1);
            storeInfoMapper.updateById(storeInfoDO);
            //生成小程序码
            WxMaService wxMaService = myWxService.initWxMa();
            // 获取小程序二维码生成实例
            try {
                String path = "pages/orderSubmit/orderSubmit?storeId=" + reqVO.getStoreId() + "&roomId=" + roomInfoDO.getRoomId();
                WxMaQrcodeService wxMaQrcodeService = wxMaService.getQrcodeService();
                byte[] bytes = generateMiniQrCode(path, wxMaQrcodeService);
                String file = fileApi.createFile(bytes);
                roomInfoMapper.updateById(new RoomInfoDO().setRoomId(roomInfoDO.getRoomId()).setQrCode(file));
            } catch (WxErrorException e) {
                log.error("生成小程序码异常", e);
//                throw new RuntimeException(e);
            }
            //生成续费码
            try {
                String path = "pages/roomRenew/roomRenew?storeId=" + storeInfoDO.getStoreId() + "&roomId=" + roomInfoDO.getRoomId();
                WxMaQrcodeService wxMaQrcodeService = wxMaService.getQrcodeService();
                byte[] bytes = generateMiniQrCode(path, wxMaQrcodeService);
                String file = fileApi.createFile(bytes);
                roomInfoMapper.updateById(new RoomInfoDO().setRoomId(roomInfoDO.getRoomId()).setRenewCode(file));
            } catch (WxErrorException e) {
                log.error("生成小程序码异常", e);
//                throw new RuntimeException(e);
            }
        } else {
            //校验门店权限
            checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());
            RoomInfoDO roomInfoDO = RoomInfoConvert.INSTANCE.convert3(reqVO);
            roomInfoDO.setReserve(reqVO.getReserve()); // 转换属性丢失
            WxMaService wxMaService = myWxService.initWxMa();
            if (ObjectUtils.isEmpty(roomInfoDO.getRenewCode())) {
                //生成续费码
                try {
                    WxMaQrcodeService wxMaQrcodeService = wxMaService.getQrcodeService();
                    String path = "pages/roomRenew/roomRenew?storeId=" + roomInfoDO.getStoreId() + "&roomId=" + roomInfoDO.getRoomId();
                    byte[] bytes = generateMiniQrCode(path, wxMaQrcodeService);
                    String file = fileApi.createFile(bytes);
                    roomInfoMapper.updateById(new RoomInfoDO().setRoomId(roomInfoDO.getRoomId()).setRenewCode(file));
                } catch (WxErrorException e) {
                    log.error("生成小程序码异常", e);
//                throw new RuntimeException(e);
                }
            }
            //生成小程序码
            if (ObjectUtils.isEmpty(roomInfoDO.getQrCode())) {
                // 获取小程序二维码生成实例
                try {
                    String path = "pages/orderSubmit/orderSubmit?storeId=" + reqVO.getStoreId() + "&roomId=" + roomInfoDO.getRoomId();
                    WxMaQrcodeService wxMaQrcodeService = wxMaService.getQrcodeService();
                    byte[] bytes = generateMiniQrCode(path, wxMaQrcodeService);
                    String file = fileApi.createFile(bytes);
                    roomInfoDO.setQrCode(file);
                } catch (WxErrorException e) {
                    log.error("生成小程序码异常", e);
//                throw new RuntimeException(e);
                }
            }
            roomInfoMapper.updateById(roomInfoDO);
        }
    }


    @Override
    public PageResult<AppDiscountRulesPageRespVO> getDiscountRulesPage(AppDiscountRulesPageReqVO reqVO) {
        reqVO.setUserId(getLoginUserId());
        IPage<AppDiscountRulesPageRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        discountRulesMapper.getDiscountRulesPage(page, reqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional
    public void changeDiscountRulesStatus(Long id) {
        //检查修改的权限 只有创建者才可以修改充值优惠规则
        DiscountRulesDO discountRulesDO = discountRulesMapper.selectById(id);
        //校验门店权限
        checkPermission(discountRulesDO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());
        if (discountRulesDO.getStatus().compareTo(AppEnum.discount_rules_status.ENABLE.getValue()) == 0) {
            //改成禁用
            discountRulesMapper.changeDiscountRulesStatus(id, 0);
        } else if (discountRulesDO.getStatus().compareTo(AppEnum.discount_rules_status.DISABLE.getValue()) == 0) {
            //改成启用
            discountRulesMapper.changeDiscountRulesStatus(id, 1);
        } else if (discountRulesDO.getStatus().compareTo(AppEnum.discount_rules_status.EXPIRE.getValue()) == 0) {
            //过期  不允许修改
            throw exception(OPRATION_ERROR);
        }
    }

    @Override
    public AppDiscountRulesDetailRespVO getDiscountRuleDetail(Long id) {
        DiscountRulesDO discountRulesDO = discountRulesMapper.selectById(id);
        return DiscountRulesConvert.INSTANCE.convert2(discountRulesDO);
    }

    @Override
    @Transactional
    public void saveDiscountRuleDetail(AppDiscountRulesDetailReqVO reqVO) {
        //校验门店权限
        checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());
        if (ObjectUtils.isEmpty(reqVO.getDiscountId())) {
            //新增
            //如果本门店已有相同的充值支付金额，则不允许再添加
            int count = discountRulesMapper.countByStoreIdAndPayMoney(reqVO.getStoreId(), reqVO.getPayMoney(), null);
            if (count > 0) {
                throw exception(DISCOUNTRULE_REPETITION_ERROR);
            }
            DiscountRulesDO discountRulesDO = DiscountRulesConvert.INSTANCE.convert2(reqVO);
            discountRulesMapper.insert(discountRulesDO);
        } else {
            //修改 校验金额重复时要排除当前门店
            int count = discountRulesMapper.countByStoreIdAndPayMoney(reqVO.getStoreId(), reqVO.getPayMoney(), reqVO.getDiscountId());
            if (count > 0) {
                throw exception(DISCOUNTRULE_REPETITION_ERROR);
            }
            DiscountRulesDO discountRulesDO = DiscountRulesConvert.INSTANCE.convert2(reqVO);
            discountRulesDO.setStoreId(reqVO.getStoreId());
            discountRulesDO.setPayMoney(reqVO.getPayMoney());
            discountRulesDO.setGiftMoney(reqVO.getGiftMoney());
            //如果过期时间是正常的 那么就改为正常
            if (discountRulesDO.getExpriceTime().isAfter(LocalDateTime.now())) {
                discountRulesDO.setStatus(AppEnum.discount_rules_status.ENABLE.getValue());
            }
            discountRulesMapper.updateById(discountRulesDO);
        }
    }

    @Override
    public String uploadImg(InputStream inputStream) {
        // 创建文件
        return fileApi.createFile(IoUtil.readBytes(inputStream));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStoreInfo(StoreInfoCreateReqVO createReqVO) {
        createReqVO.setMobile(createReqVO.getMobile().trim());
        //根据手机号 查询出用户
        MemberUserDO user = appUserService.getUserByMobile(createReqVO.getMobile());
        if (ObjectUtils.isEmpty(user)) {
//            throw exception(USER_NOT_EXISTS);
            //用户不存在则自动创建
            user = appUserService.createUserIfAbsent(createReqVO.getMobile(), getClientIP());
        }
        if (user.getUserType().compareTo(AppEnum.member_user_type.BOSS.getValue()) != 0) {
            appUserService.updateUserType(user.getId(), AppEnum.member_user_type.BOSS.getValue());
        }
        // 插入
        StoreInfoDO storeInfo = StoreInfoConvert.INSTANCE.convert(createReqVO);
        //后台添加的门店 默认都是简洁模式
        storeInfo.setSimpleModel(true);
        //默认1年过期
        storeInfo.setExpireTime(LocalDateTime.now().plusYears(1));
        storeInfoMapper.insert(storeInfo);
        //建立用户关系
        StoreUserDO storeUserDO = new StoreUserDO();
        storeUserDO.setStoreId(storeInfo.getStoreId());
        storeUserDO.setUserId(user.getId());
        storeUserDO.setType(AppEnum.member_user_type.BOSS.getValue());
        storeUserDO.setGiftBalance(new BigDecimal(999999));
        storeUserMapper.insert(storeUserDO);
        //生成默认房间
        List<RoomInfoDO> roomInfoDOList = new ArrayList<>(createReqVO.getRoomNum());
        for (int i = 1; i <= createReqVO.getRoomNum(); i++) {
            RoomInfoDO roomInfoDO = new RoomInfoDO();
            roomInfoDO.setRoomName("房间" + i);
            roomInfoDO.setStoreId(storeInfo.getStoreId());
            roomInfoDO.setType(AppEnum.room_type.DA.getValue());
            roomInfoDO.setPrice(BigDecimal.ONE);
            roomInfoDO.setWorkPrice(BigDecimal.ONE);
            roomInfoDO.setTongxiaoPrice(BigDecimal.TEN);
            roomInfoDOList.add(roomInfoDO);
        }
        roomInfoMapper.insertBatch(roomInfoDOList);
        // 返回
        return storeInfo.getStoreId();
    }

    @Override
    public void updateStoreInfo(StoreInfoUpdateReqVO updateReqVO) {
        // 校验存在
        validateStoreInfoExists(updateReqVO.getStoreId());
        // 更新
        StoreInfoDO bean = BeanUtil.toBean(updateReqVO, StoreInfoDO.class);
        StoreInfoDO updateObj = StoreInfoConvert.INSTANCE.convert(updateReqVO);
        storeInfoMapper.updateById(bean);
        //更新美团uuid
        if (!StringUtils.isEmpty(updateReqVO.getMeituanOpenShopUuid())) {
            storeMeituanInfoMapper.update(new StoreMeituanInfoDO().setOpenShopUuid(updateReqVO.getMeituanOpenShopUuid()), new LambdaUpdateWrapper<StoreMeituanInfoDO>().eq(StoreMeituanInfoDO::getStoreId, updateReqVO.getStoreId()));
        }
    }

    @Override
    public void deleteStoreInfo(Long id) {
        // 校验存在
        validateStoreInfoExists(id);
        // 删除
        storeInfoMapper.deleteById(id);
        //清除该门店绑定的设备
        deviceInfoMapper.deleteStoreId(id);
    }

    private void validateStoreInfoExists(Long id) {
        if (storeInfoMapper.selectById(id) == null) {
            throw exception(DATA_NOT_EXISTS);
        }
    }

    @Override
    public StoreInfoRespVO getStoreInfo(Long id) {
        StoreInfoDO storeInfoDO = storeInfoMapper.selectById(id);
        StoreInfoRespVO bean = BeanUtil.toBean(storeInfoDO, StoreInfoRespVO.class);
//        StoreInfoRespVO convert = StoreInfoConvert.INSTANCE.convert(storeInfoDO);
        StoreMeituanInfoDO meituanInfoDO = storeMeituanInfoMapper.getByStoreId(id);
        if (!ObjectUtils.isEmpty(meituanInfoDO)) {
            bean.setMeituanOpenShopUuid(meituanInfoDO.getOpenShopUuid());
        }
        return bean;
    }

    @Override
    public List<StoreInfoDO> getStoreInfoList(Collection<Long> ids) {
        return storeInfoMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<StoreInfoDO> getStoreInfoPage(StoreInfoPageReqVO pageReqVO) {
        return storeInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<StoreInfoDO> getStoreInfoList(StoreInfoExportReqVO exportReqVO) {
        return storeInfoMapper.selectList(exportReqVO);
    }

    @Override
    public List<KeyValue<String, Long>> getStoreList(String name, String cityName) {
        return storeInfoMapper.getStoreList(name, cityName, WebFrameworkUtils.getLoginUserId());
    }

    @Override
    public List<KeyValue<String, Long>> getRoomList(Long storeId) {
        return roomInfoMapper.getRoomList(storeId, WebFrameworkUtils.getLoginUserId());
    }

    @Override
    public void checkPermission(Long storeId, Long userId, Integer userType, Integer checkType) {
        if (com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils.getLoginUser() != null
                && Integer.valueOf(2).equals(com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils.getLoginUser().getUserType()) && storeId != null) {
            com.cloudsteward.framework.security.core.LoginUser login = com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils.getLoginUser();
            StoreInfoDO store = storeInfoMapper.selectById(storeId);
            if (!pcSecurity.hasPermission("member:workspace:manage") || store == null
                    || (!pcSecurity.hasRole("super_admin") && !Objects.equals(store.getTenantId(), login.getTenantId()))) {
                throw exception(AUTH_PROMISSION_ERROR);
            }
            return;
        }
        // 12加盟商 13管理员 14保洁员
        if (ObjectUtils.isEmpty(storeId)) {
            //不存在门店Id  仅判断用户类型
            if (userType > checkType) {
                throw exception(AUTH_PROMISSION_ERROR);
            }
        } else {
            //存在门店id  需要根据门店来校验
            String type = "";
            switch (checkType) {
                case 12:
                    type = "12";
                    break;
                case 13:
                    type = "12,13";
                    break;
                case 14:
                    type = "12,13,14";
                    break;
            }
            Integer count = storeUserMapper.checkStorePromission(storeId, userId, type);
            if (count == 0) {
                throw exception(AUTH_PROMISSION_ERROR);
            }
        }
    }

    @Override
    public List<KeyValue<Long, String>> getNameMapByIds(Set<String> storeIdSet) {
        return storeInfoMapper.getNameMapByIds(storeIdSet);
    }

    @Override
    @Transactional
    public void clearAndFinish(Long roomId) {
        //检查权限
        RoomInfoDO roomInfoDO = roomInfoMapper.selectById(roomId);
        checkPermission(roomInfoDO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.CLEAR.getValue());
        //只有状态为进行中 或 待清洁，才能处理
        if (roomInfoDO.getStatus().compareTo(AppEnum.room_status.USED.getValue()) == 0) {
            //使用中  订单结束时间改为当前  房间状态改为空闲
            OrderInfoDO orderInfoDO = orderInfoMapper.getByRoomCurrent(roomId);
            if (!ObjectUtils.isEmpty(orderInfoDO)) {
                Date now = new Date();
                orderInfoDO.setEndTime(now);
                orderInfoDO.setStatus(AppEnum.order_status.FINISH.getValue());
                orderInfoMapper.updateById(orderInfoDO);
            }
            //orderCount=0,因为这里关电的时候，订单的状态已完成了
            deviceService.closeRoomDoor(getLoginUserId(), roomInfoDO.getStoreId(), roomId, 2, 0);
            deviceService.closeLightByRoomId(getLoginUserId(), roomInfoDO.getStoreId(), roomId, 2, 0);
        } else if (roomInfoDO.getStatus().compareTo(AppEnum.room_status.CLEAR.getValue()) == 0) {
            //待清洁  房间状态改为空闲
            //关电 关灯
            //orderCount=0,因为这里关电的时候，订单的状态已完成了
            deviceService.closeRoomDoor(getLoginUserId(), roomInfoDO.getStoreId(), roomId, 2, 0);
            deviceService.closeLightByRoomId(getLoginUserId(), roomInfoDO.getStoreId(), roomId, 2, 0);
        } else {
            throw exception(CLEAR_AND_FINISH_ROOM_STATUS_ERROR);
        }
        //取消掉该房间 未完成的所有保洁订单
        clearInfoMapper.cancelByRoomId(roomId);
        //刷新房间状态
        appOrderService.flushRoomStatus(roomId, null);
        //发通知
        workWxService.sendClearRoomMsg(roomInfoDO.getStoreId(), roomInfoDO.getRoomId(), getLoginUserId(), "设置房间空闲");
    }

    @Override
    @Transactional
    public void finishRoomOrder(Long roomId) {
        //查询出该房间正在进行中的订单
        OrderInfoDO orderInfoDO = orderInfoMapper.getByRoomCurrent(roomId);
        if (!ObjectUtils.isEmpty(orderInfoDO)) {
            Date endTime = new Date();
            endTime.setSeconds(0);
            orderInfoDO.setEndTime(endTime);
            //结束
            orderInfoDO.setStatus(AppEnum.order_status.FINISH.getValue());
            orderInfoMapper.updateById(orderInfoDO);
            //新增保洁订单
            RoomInfoDO roomInfoDO = roomInfoMapper.selectById(roomId);
            //判断此房间是否跳过清洁
            if (!roomInfoDO.getJumpClear()) {
                //先删除该房间之前的保洁订单
                clearInfoMapper.cancelByRoomId(roomId);
                //然后再新增本次的保洁订单
                ClearInfoDO clearInfoDO = new ClearInfoDO().setOrderId(orderInfoDO.getOrderId()).setStoreId(orderInfoDO.getStoreId()).setOrderNo(orderInfoDO.getOrderNo()).setRoomId(orderInfoDO.getRoomId());
                clearInfoMapper.insert(clearInfoDO);
                applicationEventPublisher.publishEvent(new CleaningOrderFinished(clearInfoDO, endTime.toInstant()));
            }
            //关电 关灯 orderCount=0,因为这里关电的时候，订单的状态已完成了
            deviceService.closeRoomDoor(getLoginUserId(), orderInfoDO.getStoreId(), orderInfoDO.getRoomId(), 2, 0);
            deviceService.closeLightByRoomId(getLoginUserId(), orderInfoDO.getStoreId(), orderInfoDO.getRoomId(), 2, 0);
            //刷新房间状态
            appOrderService.flushRoomStatus(orderInfoDO.getRoomId(), null);
            //发通知
            workWxService.sendClearRoomMsg(orderInfoDO.getStoreId(), orderInfoDO.getRoomId(), getLoginUserId(), "结束订单");
        }
    }

    @Override
    public String meituanScope(Long storeId) {
        String url = "https://e.dianping.com/dz-open/merchant/auth?app_key=" + getAppKey() + "&redirect_url=" + getRedirectUrl() + "&state=storeId-" + storeId;
        return url;
    }

    @Override
    @Transactional
    public void disableRoom(Long roomId) {
        //检查权限
        RoomInfoDO roomInfoDO = roomInfoMapper.selectById(roomId);
        checkPermission(roomInfoDO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.ADMIN.getValue());
        if (roomInfoDO.getStatus().compareTo(AppEnum.room_status.DISABLE.getValue()) == 0) {
            //改成正常
            roomInfoMapper.updateStatusById(AppEnum.room_status.ENABLE.getValue(), roomId);
        } else {
            //改成禁用
            roomInfoMapper.updateStatusById(AppEnum.room_status.DISABLE.getValue(), roomId);
        }
        appOrderService.flushRoomStatus(roomId, null);
    }

    @Override
    @Transactional
    public void syncPrice(Long storeId) {
        //校验门店权限
        checkPermission(storeId, getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        StoreInfoDO storeInfoDO = storeInfoMapper.selectById(storeId);
        storeInfoMapper.updateById(new StoreInfoDO().setStoreId(storeId).setWorkPrice(!storeInfoDO.getWorkPrice()));
    }

    @Override
    @Transactional
    public void deleteRoomInfo(Long roomId) {
        RoomInfoDO roomInfoDO = roomInfoMapper.selectById(roomId);
        StoreInfoDO storeInfoDO = storeInfoMapper.selectById(roomInfoDO.getStoreId());
        //校验门店权限
        checkPermission(storeInfoDO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());
        //删除房间  房间不能有未完成的订单
        int i = orderInfoMapper.countByRoomId(roomId, null);
        if (i < 0) {
            throw exception(DELETE_ROOM_ERROR);
        }
        //开始删除
        roomInfoMapper.deleteById(roomId);
        //房间数量-1
        storeInfoMapper.updateById(new StoreInfoDO().setStoreId(storeInfoDO.getStoreId()).setRoomNum(storeInfoDO.getRoomNum() - 1));
        //清除该房间的设备
        deviceService.clearByRoomId(roomId);
    }

    @Override
    public List<AppRoomInfoListRespVO> getRoomInfoList2(Long storeId) {
        List<String> storeIds = storeUserMapper.getIdsByEmploy(getLoginUserId());
        if (CollectionUtils.isEmpty(storeIds)) {
            return new ArrayList<>();
        }

        List<AppRoomInfoListRespVO> list = roomInfoMapper.getRoomInfoList2(storeIds, storeId);
        if (!CollectionUtils.isEmpty(list)) {
            //找出所有房间的订单
            List<OrderInfoDO> orderList = orderInfoMapper.getByRoomIds(list.stream().map(x -> x.getRoomId()).collect(Collectors.toList()));
            //把订单按照房间id分组
            Map<String, List<OrderInfoDO>> orederMap;
            if (!CollectionUtils.isEmpty(orderList)) {
                orederMap = orderList.stream().collect(Collectors.groupingBy(x -> String.valueOf(x.getRoomId())));
            } else {
                orederMap = new HashMap<>();
            }
            //是否存在网关
            if (deviceService.countGateway(storeId) > 0) {
                list.forEach(x -> x.setGatewayId(1L));
            }
            list.forEach(x -> {
                //如果有空调控制器，设置一下
                if (deviceService.countKongtiao(x.getRoomId()) > 0) {
                    x.setKongtiaoCount(1);
                }
                //找出该房间所有订单
                if (orederMap.containsKey(x.getRoomId().toString())) {
                    List<OrderInfoDO> sortOrder = orederMap.get(x.getRoomId().toString()).stream().sorted(Comparator.comparing(OrderInfoDO::getStartTime)).collect(Collectors.toList());
                    //把第一个订单的开始和结束时间 设置给房间
                    x.setStartTime(sortOrder.get(0).getStartTime());
                    x.setEndTime(sortOrder.get(0).getEndTime());
                }
            });
        }
        return list;

    }

    @Override
    public String getGroupPayAuthUrl(GroupPayAuthUrlReqVO reqVO) {
        if (iotService.groupPay()) {
            String shopId = groupBuyBindings.requireShopId(reqVO.getStoreId());
            return iotGroupPayService.getScopeUrl(new IotGroupPayScopeUrlReqVO().setPlatform(reqVO.getGroupPayType()).setShopId(shopId));
        } else {
            if (reqVO.getGroupPayType().intValue() == 1) {
                return "https://e.dianping.com/dz-open/merchant/auth?app_key=" + getAppKey() + "&redirect_url=" + getRedirectUrl() + "&state=storeId-" + reqVO.getStoreId();
            } else {
                return "暂不支持此平台授权";
            }
        }
    }

    @Override
    public AppStoreSoundInfoRespVO getStoreSoundInfo(Long storeId, Integer roomClass) {
        AppStoreSoundInfoRespVO storeSoundInfo = storeSoundInfoMapper.getStoreSoundInfo(storeId, roomClass);
        if (ObjectUtils.isEmpty(storeSoundInfo)) {
            storeSoundInfo = new AppStoreSoundInfoRespVO();
        }
        return storeSoundInfo;
    }

    @Override
    @Transactional
    public void saveStoreSoundInfo(AppStoreSoundInfoReqVO reqVO) {
        //校验门店权限
        checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //如果已经有了，就更新  没有就新增
        StoreSoundInfoDO soundInfoDO = storeSoundInfoMapper.getByStoreId(reqVO.getStoreId(), reqVO.getRoomClass());
        if (!ObjectUtils.isEmpty(soundInfoDO)) {
            BeanUtils.copyProperties(reqVO, soundInfoDO);
            storeSoundInfoMapper.updateById(soundInfoDO);
        } else {
            soundInfoDO = new StoreSoundInfoDO();
            BeanUtils.copyProperties(reqVO, soundInfoDO);
            storeSoundInfoMapper.insert(soundInfoDO);
        }
    }

    @Override
    @Transactional
    public void addDevice(AppAddDeviceReqVO reqVO) {
        reqVO.setDeviceSn(reqVO.getDeviceSn().toUpperCase());
        //校验门店权限
        checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //如果不是共用设备  那新增的设备不能存在
        if (!reqVO.getShareDevice()) {
            int i = deviceInfoMapper.countBySN(reqVO.getDeviceSn());
            if (i > 0) {
                throw exception(DEVICE_DATA_EXISTS_ERROR);
            }
        }
        //有的设备每个房间只能存在一个
        //门禁=1  空开=2  云喇叭=3  灯具=4 密码锁=5 网关=6  插座=7
        //锁球器控制器（12V)=8 人脸门禁机=9 智能语音喇叭=10 二维码识别器=11
        //红外控制器=12 三路控制器=13 AI锁球器=14 计时器=16
        if (ObjectUtils.isEmpty(reqVO.getRoomId())) {
            //绑定到门店的
            switch (reqVO.getDeviceType()) {
                case 5:
                    int c = deviceInfoMapper.countByTypeAndRoomId(reqVO.getDeviceType(), reqVO.getStoreId(), reqVO.getRoomId());
                    if (c > 0) {
                        throw exception(DEVICE_ADD_MAX_NUM_ERROR);
                    }
                    break;
            }
        } else {
            //绑定到房间的
            switch (reqVO.getDeviceType()) {
                case 5:
                case 8:
                case 11:
                case 13:
                case 14:
                case 16:
                    int c = deviceInfoMapper.countByTypeAndRoomId(reqVO.getDeviceType(), reqVO.getStoreId(), reqVO.getRoomId());
                    if (c > 0) {
                        throw exception(DEVICE_ADD_MAX_NUM_ERROR);
                    }
                    break;
            }
        }
        //设备已在新平台预绑定
        String data = reqVO.getDeviceSn();
        // 插入
        DeviceInfoDO deviceInfo = new DeviceInfoDO().setDeviceSn(reqVO.getDeviceSn()).setType(reqVO.getDeviceType()).setStoreId(reqVO.getStoreId()).setRoomId(reqVO.getRoomId()).setShare(reqVO.getShareDevice()).setDeviceData(data);
        deviceInfoMapper.insert(deviceInfo);
    }

    @Override
    @Transactional
    public void delDevice(Long deviceId) {
        DeviceInfoDO deviceInfoDO = deviceInfoMapper.selectById(deviceId);
        if (!ObjectUtils.isEmpty(deviceInfoDO)) {
            //避免操作失误 仅允许超管删除
            checkPermission(deviceInfoDO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());
            //设备已在新平台预绑定，解绑由平台处理，本地直接删除
            //删除
            deviceInfoMapper.deleteById(deviceId);
            log.info("用户:{}，删除设备:{}", getLoginUserId(), deviceId);
        }
    }

    @Override
    @Transactional
    public void renew(StoreRenewReqVO reqVO) {
        StoreInfoDO storeInfoDO = storeInfoMapper.selectById(reqVO.getStoreId());
        if (!ObjectUtils.isEmpty(storeInfoDO)) {
            LocalDateTime newDate = null;
            if (ObjectUtils.isEmpty(storeInfoDO.getExpireTime())) {
                //没有到期时间  那么取创建时间+1年作为初始的到期时间
                newDate = storeInfoDO.getCreateTime().plusYears(1);
            } else {
                //判断与当前时间是否超过一个月  超过一个月不允许续费
                if (storeInfoDO.getExpireTime().isBefore(LocalDateTime.now().plusMonths(1))) {
                    newDate = storeInfoDO.getExpireTime().plusYears(1);
                } else {
                    throw exception(STORE_RENEW_TIME_ERROR);
                }
            }
            storeInfoMapper.renew(reqVO.getStoreId(), reqVO.getStatus(), newDate);
        }
    }

    @Override
    public PageResult<AppVipBlacklistRespVO> getVipBlacklist(AppGetVipBlackListVO pageVo) {

        IPage<AppVipBlacklistRespVO> page = new Page<>(pageVo.getPageNo(), pageVo.getPageSize());
        storeUserMapper.getVipBlacklist(page, pageVo.getStoreId());
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public void addBlackList(AppAddBlackListVO addBlackList) {
        // 先查询是否member_user
        MemberUserDO memberUserDO = memberUserMapper.selectOne(MemberUserDO::getMobile, addBlackList.getPhone().trim());
        //判断是否存在该用户
        Long user_id = null;
        if (ObjectUtils.isEmpty(memberUserDO)) {
            AppUserCreateReqVO appUserCreateReqVO = new AppUserCreateReqVO();
            appUserCreateReqVO.setStatus(0);
            appUserCreateReqVO.setMobile(addBlackList.getPhone().trim());
            appUserCreateReqVO.setUserType((byte) 11);
            user_id = memberUserService.createAppUser(appUserCreateReqVO);

            // 用户都不存在 则直接向 store_user中插入数据
            StoreUserDO storeUserDO = new StoreUserDO();
            storeUserDO.setStoreId(addBlackList.getStoreId());
            storeUserDO.setUserId(user_id);
            storeUserDO.setType(AppEnum.member_user_type.MEMBER.getValue());
            // 1表示是该门店的黑名单用户
            storeUserDO.setVipBlacklist(1);
            storeUserMapper.insert(storeUserDO);
        } else {
            user_id = memberUserDO.getId();
        }
        // 该用户存在 先查询store-user中是否包含此关系
        StoreUserDO storeUserDO = storeUserMapper.selectOne(StoreUserDO::getUserId, user_id, StoreUserDO::getStoreId, addBlackList.getStoreId());

        // 用户与该门店没存在关联 直接新增关联
        if (ObjectUtils.isEmpty(storeUserDO)) {
            // 用户都不存在 则直接向 store_user中插入数据
            StoreUserDO insertStoreUser = new StoreUserDO();
            insertStoreUser.setStoreId(addBlackList.getStoreId());
            insertStoreUser.setUserId(user_id);
            insertStoreUser.setType(AppEnum.member_user_type.MEMBER.getValue());
            // 1表示是该门店的黑名单用户
            insertStoreUser.setVipBlacklist(1);
            storeUserMapper.insert(insertStoreUser);
        } // 有关联 直接修改是否黑名单
        else {
            storeUserDO.setVipBlacklist(1);
            storeUserDO.setAddTime(DateUtils.of(LocalDateTime.now()));
            storeUserMapper.updateById(storeUserDO);
        }

    }

    @Override
    public void removeBlackList(Long id) {
        StoreUserDO storeUserDO = new StoreUserDO();
        storeUserDO.setId(id);
        storeUserDO.setVipBlacklist(0);
        storeUserMapper.updateById(storeUserDO);
    }

    @Override
    @Transactional
    public void updateRoomLock(AppUpRoomLockReqVO reqVO) {
        //查出这个房间的密码锁编号
//        String sn = deviceInfoMapper.getSnByRoomIdAndType(reqVO.getRoomId(), AppEnum.device_type.LOCK.getValue());
        if (!ObjectUtils.isEmpty(reqVO.getSn())) {
            reqVO.setSn(reqVO.getSn().toUpperCase());
            //先更新数据库自己的  有可能被绑定到多个房间
            List<DeviceInfoDO> deviceInfoDOList = deviceInfoMapper.getBySn(reqVO.getSn());
            if (!CollectionUtils.isEmpty(deviceInfoDOList)) {
                deviceInfoDOList.forEach(x -> {
                    x.setDeviceData(reqVO.getUpData());
                    x.setDeviceSn(reqVO.getSn());
                    deviceInfoMapper.updateById(x);
                });
            }
            //再更新物联网服务器的 - 新平台已无addLock接口，跳过


        }
    }

    @Override
    public AppRoomPrePayConfigRespVO getPrePayConfig(Long roomId) {
        AppRoomListVO appRoomListVO = roomInfoMapper.getInfoById(roomId);
        if (ObjectUtils.isEmpty(appRoomListVO)) {
            throw exception(DATA_NOT_EXISTS);
        }
        //校验门店权限
        checkPermission(appRoomListVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());
        AppRoomPrePayConfigRespVO respVO = new AppRoomPrePayConfigRespVO();
        BeanUtils.copyProperties(appRoomListVO, respVO);
        return respVO;
    }

    @Override
    @Transactional
    public void setPrePayConfig(AppRoomPrePayConfigRespVO reqVO) {
        AppRoomListVO appRoomListVO = roomInfoMapper.getInfoById(reqVO.getRoomId());
        if (ObjectUtils.isEmpty(appRoomListVO)) {
            throw exception(DATA_NOT_EXISTS);
        }
        //校验门店权限
        checkPermission(appRoomListVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());
        RoomInfoDO updateDO = new RoomInfoDO().setRoomId(reqVO.getRoomId()).setPrePrice(reqVO.getPrePrice()).setPreUnit(reqVO.getPreUnit()).setMinCharge(reqVO.getMinCharge());
        roomInfoMapper.updateById(updateDO);
    }

    @Override
    @Transactional
    public void setTemplate(StoreSetTemplateReqVO reqVO, boolean isAdmin) {
        if (!isAdmin) {//判断后台还是小程序请求的
            //检查权限
            checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());
        }
        StoreInfoDO storeInfoDO = storeInfoMapper.selectById(reqVO.getStoreId());
        if (!ObjectUtils.isEmpty(storeInfoDO)) {
            //如果是空，则是简洁模式
            if (ObjectUtils.isEmpty(reqVO.getTemplateKey())) {
                storeInfoMapper.updateById(new StoreInfoDO().setStoreId(storeInfoDO.getStoreId()).setSimpleModel(true).setTemplateKey(""));
            } else {
                //非简洁模式  安全起见 检查一下模板是否存在
                reqVO.setTemplateKey(reqVO.getTemplateKey().trim());
                if (!reqVO.getTemplateKey().equals("custom")) {
                    StoreTemplateDO storeTemplateDO = storeTemplateMapper.selectByKey(reqVO.getTemplateKey());
                    if (ObjectUtils.isEmpty(storeTemplateDO)) {
                        throw exception(ADMIN_STORE_TEMPLATE_KEY_ERROR);
                    }
                }
                storeInfoMapper.updateById(new StoreInfoDO().setStoreId(storeInfoDO.getStoreId()).setSimpleModel(false).setTemplateKey(reqVO.getTemplateKey().trim()));
            }
        }
    }

    @Override
    public List<AppTemplateInfoRespVO> getTemplateList() {
        return storeTemplateMapper.getTemplateList();
    }

    @Override
    public Boolean checkBall(Long deviceId) {
        DeviceInfoDO deviceInfoDO = deviceInfoMapper.selectById(deviceId);
        if (ObjectUtils.isEmpty(deviceInfoDO)) {
            throw exception(DATA_NOT_EXISTS);
        }
        if (ObjectUtils.isEmpty(deviceInfoDO.getRoomId())) {
            throw exception(DATA_NOT_EXISTS);
        }
        return deviceService.runBall(deviceInfoDO.getDeviceSn(), null, "check");
    }

    @Override
    public List<KeyValue<String, Long>> getStoreListByAdmin(String name, String cityName) {
        return storeInfoMapper.getStoreListByAdmin(name, cityName, WebFrameworkUtils.getLoginUserId());
    }

    public List<AppServiceInfoRespVO> getServiceInfo(HttpServletRequest request) {
        // 当前时间
        LocalDateTime now = LocalDateTime.now();
        ZoneId zone = ZoneId.of("Asia/Shanghai");

        List<AppServiceInfoRespVO> result = new ArrayList<>();

        // 1. 租户
        TenantBean tenant = tenantApi.getById(getTenantId(request));
        if (tenant != null && tenant.getExpireTime() != null) {
            LocalDateTime expire = tenant.getExpireTime();
            boolean show = !expire.isBefore(now) && !expire.isAfter(now.plusDays(3));
            if (show) {
                result.add(new AppServiceInfoRespVO()
                        .setName(tenant.getName())
                        .setType(1)
                        .setExpireDate(Date.from(expire.atZone(zone).toInstant()))
                        .setShow(show));
            }
        }

        // 2. 门店
        List<StoreInfoDO> storeList = storeUserMapper.getStoreByAdmin(getLoginUserId());
        if (!CollectionUtils.isEmpty(storeList)) {
            for (StoreInfoDO store : storeList) {
                if (store.getExpireTime() == null) continue;
                LocalDateTime expire = store.getExpireTime();
                boolean show = !expire.isBefore(now) && !expire.isAfter(now.plusDays(3));
                if (show) {
                    result.add(new AppServiceInfoRespVO()
                            .setName(store.getStoreName())
                            .setType(2)
                            .setExpireDate(Date.from(expire.atZone(zone).toInstant()))
                            .setShow(show));
                }
            }
        }

        return result;
    }

    @Override
    @Transactional
    public void resetQrcode(Long storeId) {
        //检查权限
        checkPermission(storeId, getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());
        //先处理门店
        WxMaService wxMaService = myWxService.initWxMa();
        try {
            String path = "pages/index/index?storeId=" + storeId;
            WxMaQrcodeService wxMaQrcodeService = wxMaService.getQrcodeService();
            byte[] bytes = generateMiniQrCode(path, wxMaQrcodeService);
            String file = fileApi.createFile(bytes);
            storeInfoMapper.updateById(new StoreInfoDO().setStoreId(storeId).setQrCode(file));
        } catch (WxErrorException e) {
            log.error("生成小程序码异常", e);
//                throw new RuntimeException(e);
        }
        //再处理门店的所有房间
        for (Long roomId : roomInfoMapper.getRoomIdsByStoreId(storeId)) {
            // 获取小程序二维码生成实例
            try {
                String path = "pages/orderSubmit/orderSubmit?storeId=" + storeId + "&roomId=" + roomId;
                WxMaQrcodeService wxMaQrcodeService = wxMaService.getQrcodeService();
                byte[] bytes = generateMiniQrCode(path, wxMaQrcodeService);
                String file = fileApi.createFile(bytes);
                roomInfoMapper.updateById(new RoomInfoDO().setRoomId(roomId).setQrCode(file));
            } catch (WxErrorException e) {
                log.error("生成小程序码异常", e);
//                throw new RuntimeException(e);
            }
            //生成续费码
            try {
                String path = "pages/roomRenew/roomRenew?storeId=" + storeId + "&roomId=" + roomId;
                WxMaQrcodeService wxMaQrcodeService = wxMaService.getQrcodeService();
                byte[] bytes = generateMiniQrCode(path, wxMaQrcodeService);
                String file = fileApi.createFile(bytes);
                roomInfoMapper.updateById(new RoomInfoDO().setRoomId(roomId).setRenewCode(file));
            } catch (WxErrorException e) {
                log.error("生成小程序码异常", e);
//                throw new RuntimeException(e);
            }
        }
    }

    @Override
    @Transactional
    public void setDouyinId(Long storeId, String dyId) {
        //检查权限
        checkPermission(storeId, getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());
        storeInfoMapper.updateById(new StoreInfoDO().setStoreId(storeId).setDouyinPoiId(dyId));
        //bindStore已迁移，门店-店铺映射现在由member_store_shop_mapping表管理


    }

    @Override
    public String getNFCScheme(AppNFCDataReqVO reqVO) {
        RoomInfoDO roomInfoDO = roomInfoMapper.selectById(reqVO.getRoomId());
        if (!ObjectUtils.isEmpty(roomInfoDO.getNfcUrl())) {
            return roomInfoDO.getNfcUrl();
        } else {
            Long storeId = reqVO.getStoreId();
            Long roomId = reqVO.getRoomId();
            if (ObjectUtils.isEmpty(roomId)) {
                roomId = roomInfoDO.getRoomId();
            }
            if (ObjectUtils.isEmpty(storeId)) {
                storeId = roomInfoDO.getStoreId();
            }

            MiniappConfigVO miniAppConfig = myWxService.getMiniAppConfig();
            if (ObjectUtils.isEmpty(miniAppConfig.getNfcModelId())) {
                throw exception(NFC_MODEL_ID_ERROR);
            }
            WxMaService wxMaService = myWxService.initWxMa();
            String sn = "roomId=" + roomId + "&storeId=" + storeId;
            WxMaGenerateNfcSchemeRequest.JumpWxa jumpWxa = WxMaGenerateNfcSchemeRequest.JumpWxa.newBuilder()
                    .path("pages/orderSubmit/orderSubmit")
                    .query("nfc=1&" + sn)
//                    .envVersion("develop")
                    .build();
            WxMaGenerateNfcSchemeRequest request = WxMaGenerateNfcSchemeRequest.newBuilder()
                    .sn(sn)
                    .modelId(miniAppConfig.getNfcModelId()).jumpWxa(jumpWxa).build();
            try {
                String url = wxMaService.getWxMaSchemeService().generateNFC(request);
                roomInfoMapper.updateById(new RoomInfoDO().setRoomId(roomInfoDO.getRoomId()).setNfcUrl(url));
                return url;
            } catch (WxErrorException e) {
//                throw new RuntimeException(e);
                throw exception(IOT_ERROR, e.getMessage());
            }
        }
    }

    /**
     * 生成小程序码字节数组（提取公共方法，避免重复调用）
     */
    private byte[] generateMiniQrCode(String path, WxMaQrcodeService wxMaQrcodeService) throws WxErrorException {
        return wxMaQrcodeService.createWxaCodeBytes(path, "release", 430, false, null, false);
    }
}
