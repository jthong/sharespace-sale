package com.cloudsteward.module.member.service.manager;

import cn.hutool.core.util.HexUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudsteward.framework.common.core.KeyValue;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.module.member.api.user.MemberUserApi;
import com.cloudsteward.module.member.controller.admin.user.vo.AppUserRechargeReqVO;
import com.cloudsteward.module.member.controller.app.chart.vo.*;
import com.cloudsteward.module.member.controller.app.clear.vo.AppClearPageReqVO;
import com.cloudsteward.module.member.controller.app.clear.vo.AppClearPageRespVO;
import com.cloudsteward.module.member.controller.app.manager.vo.*;
import com.cloudsteward.module.member.controller.app.order.vo.OrderListRespVO;
import com.cloudsteward.module.member.controller.app.order.vo.OrderPageReqVO;
import com.cloudsteward.module.member.controller.app.order.vo.OrderRenewalReqVO;
import com.cloudsteward.module.member.controller.app.user.vo.*;
import com.cloudsteward.module.member.dal.dataobject.clearbill.ClearBillDO;
import com.cloudsteward.module.member.dal.dataobject.clearinfo.ClearInfoDO;
import com.cloudsteward.module.member.dal.dataobject.couponinfo.CouponInfoDO;
import com.cloudsteward.module.member.dal.dataobject.groupPay.GroupPayInfoDO;
import com.cloudsteward.module.member.dal.dataobject.orderinfo.OrderInfoDO;
import com.cloudsteward.module.member.dal.dataobject.payorder.PayOrderDO;
import com.cloudsteward.module.member.dal.dataobject.pkguserinfo.PkgUserInfoDO;
import com.cloudsteward.module.member.dal.dataobject.roominfo.RoomInfoDO;
import com.cloudsteward.module.member.dal.dataobject.storeinfo.StoreInfoDO;
import com.cloudsteward.module.member.dal.dataobject.storeuser.StoreUserDO;
import com.cloudsteward.module.member.dal.dataobject.user.MemberUserDO;
import com.cloudsteward.module.member.dal.dataobject.userwithdrawal.UserWithdrawalDO;
import com.cloudsteward.module.member.dal.mysql.clearbill.ClearBillMapper;
import com.cloudsteward.module.member.dal.mysql.clearinfo.ClearInfoMapper;
import com.cloudsteward.module.member.dal.mysql.couponinfo.CouponInfoMapper;
import com.cloudsteward.module.member.dal.mysql.groupPay.GroupPayInfoMapper;
import com.cloudsteward.module.member.dal.mysql.orderinfo.OrderInfoMapper;
import com.cloudsteward.module.member.dal.mysql.payorder.PayOrderMapper;
import com.cloudsteward.module.member.dal.mysql.pkguserinfo.PkgUserInfoMapper;
import com.cloudsteward.module.member.dal.mysql.roominfo.RoomInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storeuser.StoreUserMapper;
import com.cloudsteward.module.member.dal.mysql.user.AppUserMapper;
import com.cloudsteward.module.member.dal.mysql.user.MemberUserMapper;
import com.cloudsteward.module.member.dal.mysql.userwithdrawal.UserWithdrawalMapper;
import com.cloudsteward.module.member.enums.AppEnum;
import com.cloudsteward.module.member.service.device.DeviceService;
import com.cloudsteward.module.member.service.groupPay.GroupPayInfoService;
import com.cloudsteward.module.member.service.iot.IotGroupPayService;
import com.cloudsteward.module.member.service.iot.IotService;
import com.cloudsteward.module.member.service.iot.groupPay.IotGroupPayPrepareRespVO;
import com.cloudsteward.module.member.service.order.AppOrderService;
import com.cloudsteward.module.member.service.payorder.PayOrderService;
import com.cloudsteward.module.member.service.storeinfo.StoreInfoService;
import com.cloudsteward.module.member.service.user.AppUserService;
import com.cloudsteward.module.member.service.user.MemberUserService;
import com.cloudsteward.module.member.service.wx.WorkWxService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserType;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.*;

@Service
@Validated
public class AppMangerServiceImpl implements AppMangerService {

    @Resource
    private ClearInfoMapper clearInfoMapper;
    @Resource
    private ClearBillMapper clearBillMapper;
    @Resource
    private StoreUserMapper storeUserMapper;

    @Resource
    private MemberUserMapper memberUserMapper;

    @Resource
    private UserWithdrawalMapper withdrawalMapper;

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private PayOrderMapper payOrderMapper;

    @Resource
    private RoomInfoMapper roomInfoMapper;

    @Resource
    private StoreInfoMapper storeInfoMapper;

    @Resource
    private CouponInfoMapper couponInfoMapper;

    @Resource
    private AppUserMapper appUserMapper;

    @Resource
    private StoreInfoService storeInfoService;

    @Resource
    private AppOrderService appOrderService;

    @Resource
    private MemberUserService memberUserService;
    @Resource
    private DeviceService deviceService;

    @Resource
    private WorkWxService workWxService;

    @Resource
    private IotService iotService;

    @Resource
    private GroupPayInfoMapper groupPayInfoMapper;

    @Resource
    private PkgUserInfoMapper pkgUserInfoMapper;

    @Resource
    private PayOrderService payOrderService;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private AppUserService appUserService;

    @Resource
    private GroupPayInfoService groupPayInfoService;

    @Resource
    private IotGroupPayService iotGroupPayService;

    @Override
    public PageResult<OrderListRespVO> getOrderPage(OrderPageReqVO reqVO) {
        // 校验用户类型
        storeInfoService.checkPermission(null, null, getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        String storeIds = storeUserMapper.getIdsByUserIdAndAdmin(getLoginUserId()).stream().collect(Collectors.joining(","));
        reqVO.setStoreIds(storeIds);
        if (StringUtils.isEmpty(storeIds)) {
            return new PageResult<>();
        }
        IPage<OrderListRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        orderInfoMapper.getOrderPage(page, reqVO);
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            page.getRecords().forEach(x -> {
                if (!ObjectUtils.isEmpty(x.getRoomImg())) {
                    x.setRoomImg(x.getRoomImg().split(",")[0]);
                }
                if (x.getStatus().compareTo(AppEnum.order_status.CANCEL.getValue()) != 0
                        && x.getStatus().compareTo(AppEnum.order_status.FINISH.getValue()) != 0) {
                    x.setRenewBtn(true);
                } else {
                    x.setRenewBtn(false);
                }
            });
        }
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public PageResult<AppMemberPageRespVO> getMemberPage(AppMemberPageReqVO reqVO) {
        // 校验用户类型
        storeInfoService.checkPermission(null, null, getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //参数检查 排序字段和排序规则  要么全部为空，要么都不能为空
        if (ObjectUtils.isEmpty(reqVO.getCloumnName()) && ObjectUtils.isEmpty(reqVO.getSortRule())) {

        } else if (!ObjectUtils.isEmpty(reqVO.getCloumnName()) && !ObjectUtils.isEmpty(reqVO.getSortRule())) {

        } else {
            throw exception(MEMBER_PAGE_PARAM_ERROR);
        }
        //内容检查
        if (!ObjectUtils.isEmpty(reqVO.getCloumnName())) {
            if (reqVO.getCloumnName().equals("createTime") || reqVO.getCloumnName().equals("orderTime") || reqVO.getCloumnName().equals("orderCount")) {
                //合法
            } else {
                throw exception(MEMBER_PAGE_PARAM_ERROR);
            }
            if (reqVO.getSortRule().toLowerCase().equals("asc") || reqVO.getSortRule().toLowerCase().equals("desc")) {
                //合法
            } else {
                throw exception(MEMBER_PAGE_PARAM_ERROR);
            }
        }
        IPage<AppMemberPageRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        appUserMapper.getMemberPage(page, reqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public PageResult<AppCouponPageRespVO> getPresentCouponPage(AppPresentCouponPageReqVO reqVO) {

        return null;
    }

    @Override
    public PageResult<AppCouponPageRespVO> getCouponPage(AppManagerCouponPageReqVO reqVO) {
        // 校验用户类型
        storeInfoService.checkPermission(null, null, getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //仅限查看当前用户所在门店的优惠券列表
        String storeIds = storeUserMapper.getIdsByUserIdAndAdmin(getLoginUserId()).stream().collect(Collectors.joining(","));
        if (StringUtils.isEmpty(storeIds)) {
            return PageResult.empty();
        }
        IPage<AppCouponPageRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        couponInfoMapper.getCouponPageByAdmin(page, reqVO, storeIds);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public AppCouponDetailRespVO getCouponDetail(Long couponId) {
        AppCouponDetailRespVO couponDetail = couponInfoMapper.getCouponDetail(couponId);
        return couponDetail;
    }

    @Override
    @Transactional
    public void saveCouponDetail(AppCouponDetailReqVO reqVO) {
        Long loginUserId = getLoginUserId();
        //检查权限
        storeInfoService.checkPermission(reqVO.getStoreId(), loginUserId, getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //保存进去
        if (ObjectUtils.isEmpty(reqVO.getCouponId())) {
            CouponInfoDO couponInfoDO = new CouponInfoDO();
            couponInfoDO.setCouponName(reqVO.getCouponName());
            couponInfoDO.setType(reqVO.getType());
            couponInfoDO.setCreateUserId(loginUserId);
            couponInfoDO.setPrice(reqVO.getPrice());
            couponInfoDO.setMinUsePrice(reqVO.getMinUsePrice());
            couponInfoDO.setStoreId(reqVO.getStoreId());
            couponInfoDO.setExpriceTime(reqVO.getExpriceTime());
            couponInfoDO.setRoomType(reqVO.getRoomType());
            couponInfoMapper.insert(couponInfoDO);
        } else {
            CouponInfoDO couponInfoDO = couponInfoMapper.selectById(reqVO.getCouponId());
//            if (couponInfoDO.getCreateUserId().compareTo(loginUserId) != 0) {
//                throw exception(CHECK_STORE_PROMISSION_ERROR);
//            }
            couponInfoDO.setCouponName(reqVO.getCouponName());
            couponInfoDO.setType(reqVO.getType());
            couponInfoDO.setCreateUserId(loginUserId);
            couponInfoDO.setPrice(reqVO.getPrice());
            couponInfoDO.setMinUsePrice(reqVO.getMinUsePrice());
            couponInfoDO.setStoreId(reqVO.getStoreId());
            couponInfoDO.setExpriceTime(reqVO.getExpriceTime());
            if (reqVO.getExpriceTime().after(new Date())) {
                couponInfoDO.setStatus(AppEnum.coupon_status.AVAILABLE.getValue());
            }
            couponInfoDO.setRoomType(reqVO.getRoomType());
            couponInfoMapper.updateById(couponInfoDO);
        }

    }

    @Override
    public PageResult<AppClearUserPageRespVO> getClearUserPage(AppClearUserPageReqVO reqVO) {
        String ids = "";
        if (ObjectUtils.isEmpty(reqVO.getStoreId())) {
            //查询该账号权限下的所有保洁员
            List<String> storeIds = storeUserMapper.getIdsByUserIdAndAdmin(getLoginUserId());
            ids = storeIds.stream().collect(Collectors.joining(","));
        } else {
            ids = String.valueOf(reqVO.getStoreId());
        }
        IPage<AppClearUserPageRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        storeUserMapper.getClearUserPage(page, reqVO.getStoreId(), ids);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional
    public void deleteClearUser(Long storeId, Long userId) {
        // 校验权限
        storeInfoService.checkPermission(storeId, getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //检查该用户在这个门店，有没有未结算的任务
        List<ClearInfoDO> clearInfoDOS = clearInfoMapper.getByUserIdAndStatusAndStoreId(userId, AppEnum.clear_info_status.FINISH.getValue(), storeId);
        if (CollectionUtils.isEmpty(clearInfoDOS)) {
            storeUserMapper.deleteAdminUser(storeId, userId);
            //如果该用户不再属于任何店的保洁  则清除身份
            int i = storeUserMapper.countPermission(userId);
            if (i == 0) {
                appUserService.updateUserType(userId, AppEnum.member_user_type.MEMBER.getValue());
            }
        } else {
            throw exception(CLEAR_USER_DELETE_ERROR);
        }

    }

    @Override
    @Transactional
    public void saveClearUser(AppClearUserDetailReqVO reqVO) {
        // 校验权限
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //通过该手机号，查询出用户
        MemberUserDO memberUserDO = memberUserMapper.selectByMobile(reqVO.getMobile());
        if (ObjectUtils.isEmpty(memberUserDO)) {
            //不存在则创建用户
            memberUserDO = appUserService.createUserIfAbsent(reqVO.getMobile(), getClientIP());
//            throw exception(AUTH_USER_PHONE_ERROR);
        }
        if (memberUserDO.getId().compareTo(getLoginUserId()) == 0) {
            throw exception(OPRATION_ERROR);
        }
        //只能把用户角色改成保洁员  不能同时拥有2个类型
        if (memberUserDO.getUserType().compareTo(AppEnum.member_user_type.MEMBER.getValue()) != 0 && memberUserDO.getUserType().compareTo(AppEnum.member_user_type.CLEAR.getValue()) != 0) {
            throw exception(USER_TYPE_CHECK_ERROR);
        }
        //如果用户之前不是保洁员角色 则改成保洁员的用户类型
        if (memberUserDO.getUserType().compareTo(AppEnum.member_user_type.CLEAR.getValue()) != 0) {
            memberUserDO.setUserType(AppEnum.member_user_type.CLEAR.getValue());
            memberUserMapper.updateById(memberUserDO);
        }
        //已经绑定的门店不能再绑定
        StoreUserDO storeUserDO = storeUserMapper.getByUserIdAndStoreId(memberUserDO.getId(), reqVO.getStoreId());
        if (ObjectUtils.isEmpty(storeUserDO)) {
            //新增
            storeUserDO = new StoreUserDO();
            storeUserDO.setUserId(memberUserDO.getId());
            storeUserDO.setType(AppEnum.member_user_type.CLEAR.getValue());
            storeUserDO.setStoreId(reqVO.getStoreId());
            storeUserDO.setName(reqVO.getName());
            storeUserMapper.insert(storeUserDO);
        } else {
            storeUserMapper.updateById(new StoreUserDO().setId(storeUserDO.getId()).setType(AppEnum.member_user_type.CLEAR.getValue()));
        }
    }

    @Override
    @Transactional
    public void settlementClearUser(AppSettlementClearUserReqVO reqVO) {
        // 校验权限
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //查询出保洁员在门店下 有没有可结算的订单
        List<Long> list = clearInfoMapper.getSettlementList(reqVO);
        if (CollectionUtils.isEmpty(list)) {
            throw exception(NOT_FINISH_CLEAR_IONF_ERROR);
        }
        //有订单 则结算
        clearInfoMapper.settlementByIds(list);
        //增加结算记录
        ClearBillDO clearBillDO = new ClearBillDO();
        clearBillDO.setUserId(reqVO.getUserId());
        clearBillDO.setMoney(reqVO.getMoney());
        clearBillDO.setStoreId(reqVO.getStoreId());
        clearBillDO.setOrderNum(list.size());
        clearBillDO.setOrderIds(list.stream().map(x -> x.toString()).collect(Collectors.joining(",")));
        clearBillMapper.insert(clearBillDO);
    }

    @Override
    @Transactional
    public void complaintClearInfo(AppComplaintClearInfoReqVO reqVO) {
        //权限校验：管理员和加盟商允许
        // 校验用户类型
        storeInfoService.checkPermission(null, null, getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //先找出来
        ClearInfoDO clearInfoDO = clearInfoMapper.selectById(reqVO.getClearId());
        //只能操作已完成或已驳回的记录
        if (clearInfoDO.getStatus().compareTo(AppEnum.clear_info_status.TOUSU.getValue()) == 0) {
            clearInfoDO.setStatus(AppEnum.clear_info_status.FINISH.getValue());
            clearInfoDO.setComplaintDesc("管理员已撤销投诉");
            clearInfoMapper.updateById(clearInfoDO);
        } else if (clearInfoDO.getStatus().compareTo(AppEnum.clear_info_status.FINISH.getValue()) == 0) {
            clearInfoDO.setStatus(AppEnum.clear_info_status.TOUSU.getValue());
            clearInfoDO.setComplaintDesc(reqVO.getComplaintDesc());
            clearInfoMapper.updateById(clearInfoDO);
        } else {
            throw exception(CLEAR_INFO_STATUS_OPRATION_ERROR);
        }
    }

    private String getWithdrawalNo() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDateTime currentDateTime = LocalDateTime.now();
        String currentDate = currentDateTime.format(dateFormatter);
        Random random = new Random();
        int randomNum = random.nextInt(1000000);
        String randomNumString = String.format("%04d", randomNum);
        return currentDate + randomNumString;
    }

    @Override
    @Transactional
    public void applyWithdrawal() {
        //仅创建者使用
        // 校验用户类型
        storeInfoService.checkPermission(null, null, getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());

        //判断当前用户的收入还有没有可以提现的
        Long loginUserId = getLoginUserId();
        MemberUserDO memberUserDO = memberUserMapper.selectById(loginUserId);
        BigDecimal subtract = memberUserDO.getMoney().subtract(memberUserDO.getWithdrawalMoney());
        if (subtract.compareTo(BigDecimal.ZERO) > 0) {
            //可以提现
            //增加提现记录
            UserWithdrawalDO userWithdrawalDO = new UserWithdrawalDO();
            userWithdrawalDO.setUserId(loginUserId);
            userWithdrawalDO.setMoney(memberUserDO.getMoney());
            userWithdrawalDO.setNo(getWithdrawalNo());
            userWithdrawalDO.setStatus(AppEnum.user_withdrawal.COMMIT.getValue());
            withdrawalMapper.insert(userWithdrawalDO);
            //增加累积提现金额  并扣掉收入
            memberUserDO.setWithdrawalMoney(memberUserDO.getWithdrawalMoney().add(memberUserDO.getMoney()));
            memberUserDO.setMoney(BigDecimal.ZERO);
            memberUserMapper.updateById(memberUserDO);
            //todo..微信转账
        } else {
            //没有可提现的收入
            throw exception(USER_NO_MONEY_WITHDRAWAL_ERROR);
        }
    }

    @Override
    public PageResult<AppWithdrawalPageRespVO> getWithdrawalPage(AppWithdrawalPageReqVO reqVO) {
        //仅创建者使用
        storeInfoService.checkPermission(null, null, getLoginUserType(), AppEnum.member_user_type.BOSS.getValue());
        reqVO.setUserId(getLoginUserId());
        IPage<AppWithdrawalPageRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        withdrawalMapper.getWithdrawalPage(page, reqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public AppRevenueChartRespVO getRevenueChart(AppChartDataReqVO reqVO) {
        //仅管理员使用
        storeInfoService.checkPermission(null, null, getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        reqVO.setUserId(getLoginUserId());
        List<String> storeIds = storeUserMapper.getIdsByUserIdAndAdmin(getLoginUserId());
        if (CollectionUtils.isEmpty(storeIds)) {
            return new AppRevenueChartRespVO().setTotalOrder(0).setTotalMoney(BigDecimal.ZERO).setWxTotalMoney(BigDecimal.ZERO).setGroupTotalMoney(BigDecimal.ZERO);
        }
        Integer wxTotalMoney = orderInfoMapper.getWxTotalMoney(storeIds, reqVO.getStoreId());
        BigDecimal adminTotalMoney = orderInfoMapper.getAdminTotalMoney(storeIds, reqVO.getStoreId());
        BigDecimal ydMoney = orderInfoMapper.getYdMoney(reqVO);
        BigDecimal plMoney = orderInfoMapper.getPlMoney(reqVO);
        BigDecimal wxMoney = new BigDecimal(String.valueOf(wxTotalMoney / 100.0));
        BigDecimal groupTotalMoney = groupPayInfoMapper.getGroupTotalMoney(storeIds, reqVO.getStoreId());
        BigDecimal mtTotalMoney = groupPayInfoMapper.getMtTotalMoney(storeIds, reqVO.getStoreId());
        BigDecimal dyTotalMoney = groupPayInfoMapper.getDyTotalMoney(storeIds, reqVO.getStoreId());

        Integer count = orderInfoMapper.countByStoreIds(storeIds, reqVO.getStoreId());
        AppRevenueChartRespVO respVO = new AppRevenueChartRespVO();
        respVO.setTotalMoney(wxMoney.add(groupTotalMoney).add(ydMoney).add(adminTotalMoney).add(plMoney));
        respVO.setMtTotalMoney(mtTotalMoney);
        respVO.setDyTotalMoney(dyTotalMoney);
        respVO.setTotalOrder(count);
        respVO.setWxTotalMoney(wxMoney);
        respVO.setGroupTotalMoney(groupTotalMoney);
        return respVO;

    }

    @Override
    public AppBusinessStatisticsRespVO getBusinessStatistics(AppChartDataReqVO reqVO) {
        //仅管理员使用
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        reqVO.setUserId(getLoginUserId());
        //订单数
        Integer orderNum = orderInfoMapper.getCountOrder(reqVO);
        Integer userNum = orderInfoMapper.countUser(reqVO);
        //团购收入
        BigDecimal tgMoney = groupPayInfoMapper.getBusinessStatistics(reqVO).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal mtMoney = groupPayInfoMapper.getGroupBusinessStatistics(reqVO.setGroupPayType(AppEnum.member_group_no_type.MEITUAN.getValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal dyMoney = groupPayInfoMapper.getGroupBusinessStatistics(reqVO.setGroupPayType(AppEnum.member_group_no_type.DOUYIN.getValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal ksMoney = groupPayInfoMapper.getGroupBusinessStatistics(reqVO.setGroupPayType(AppEnum.member_group_no_type.KUAISHOU.getValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
        //微信支付收入
        BigDecimal money = payOrderMapper.getMoney(reqVO).setScale(2, BigDecimal.ROUND_HALF_UP);

        BigDecimal adminMoney = orderInfoMapper.getAdminOrderMoney(reqVO).setScale(2, BigDecimal.ROUND_HALF_UP);
        //预订收入
        BigDecimal ydMoney = orderInfoMapper.getYdMoney(reqVO).setScale(2, BigDecimal.ROUND_HALF_UP);
        //平台收入
        BigDecimal plMoney = orderInfoMapper.getPlMoney(reqVO).setScale(2, BigDecimal.ROUND_HALF_UP);
        AppBusinessStatisticsRespVO respVO = new AppBusinessStatisticsRespVO();
        respVO.setOrderCount(orderNum);
        respVO.setUserCount(userNum);
        respVO.setTgMoney(tgMoney);
        respVO.setMtMoney(mtMoney);
        respVO.setDyMoney(dyMoney);
        respVO.setKsMoney(ksMoney);
        respVO.setAdminMoney(adminMoney);
        respVO.setMoney(money);
        respVO.setYdMoney(ydMoney);
        respVO.setPlMoney(plMoney);
        respVO.setTotal(money.add(tgMoney).add(ydMoney).add(adminMoney).add(plMoney));
        return respVO;
    }

    @Override
    public List<KeyValue<String, BigDecimal>> getRevenueStatistics(AppChartDataReqVO reqVO) {
        //仅管理员使用
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        reqVO.setUserId(getLoginUserId());
        return orderInfoMapper.getRevenueStatistics(reqVO);
    }

    @Override
    public List<KeyValue<String, Integer>> getOrderStatistics(AppChartDataReqVO reqVO) {
        //仅管理员使用
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        reqVO.setUserId(getLoginUserId());
        return orderInfoMapper.getOrderStatistics(reqVO);
    }

    @Override
    public List<KeyValue<String, Integer>> getMemberStatistics(AppChartDataReqVO reqVO) {
        //仅管理员使用
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        reqVO.setUserId(getLoginUserId());
        return orderInfoMapper.getMemberStatistics(reqVO);
    }


    @Override
    public List<KeyValue<String, String>> getRoomUseStatistics(AppChartDataReqVO reqVO) {
        //仅管理员使用
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        reqVO.setUserId(getLoginUserId());
        List<KeyValue<String, Long>> roomUseStatistics = orderInfoMapper.getRoomUseStatistics(reqVO);
        List<KeyValue<String, String>> resultList = new ArrayList<>();
        //再查一下总共的房间数量，算出使用率
        if (!CollectionUtils.isEmpty(roomUseStatistics)) {
            Integer count = roomInfoMapper.countByStoreIdAndUserId(reqVO.getStoreId(), reqVO.getUserId());
            //%.2f  %.表示 小数点前任意位数   2 表示两位小数 格式后的结果为f 表示浮点型
            for (KeyValue<String, Long> vo : roomUseStatistics) {
//                vo.setValue(vo.getValue() / (count * 1.0));
                KeyValue<String, String> kv = new KeyValue();
                kv.setKey(vo.getKey());
                kv.setValue(String.format("%.2f", vo.getValue().doubleValue() / count * 100.0));
                resultList.add(kv);
            }
        }
        return resultList;
    }

    @Override
    public List<KeyValue<String, String>> getRoomUseHourStatistics(AppChartDataReqVO reqVO) {
        //仅管理员使用
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        reqVO.setUserId(getLoginUserId());
        List<KeyValue<String, String>> roomUseHourStatistics = orderInfoMapper.getRoomUseHourStatistics(reqVO);
        if (!CollectionUtils.isEmpty(roomUseHourStatistics)) {
            roomUseHourStatistics.forEach(x -> x.setValue(String.format("%.2f", x.getValue())));
            //%.2f  %.表示 小数点前任意位数   2 表示两位小数 格式后的结果为f 表示浮点型
        }
        return roomUseHourStatistics;
    }

    @Override
    public List<AppRoomUseHourVO> getRoomUseHour(AppChartDataReqVO reqVO) {
        //仅管理员使用
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        reqVO.setUserId(getLoginUserId());
        List<AppRoomUseHourVO> result = orderInfoMapper.getRoomUseHours(reqVO);
        if (!CollectionUtils.isEmpty(result)) {
            result.forEach(x -> {
                int dotIndex = x.getHours().indexOf('.'); // 找到小数点的位置
                if (dotIndex != -1) {
                    x.setHours(x.getHours().substring(0, dotIndex + 3));
                }
            });
        }
        return result;
    }


    @Override
    @Transactional
    public void giftCoupon(AppGiftCouponReqVO reqVO) {
        //仅管理员使用 检查权限
        storeInfoService.checkPermission(null, null, getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        CouponInfoDO couponInfoDO = couponInfoMapper.getByIdAndAdmin(reqVO.getCouponId());
        if (!ObjectUtils.isEmpty(couponInfoDO)) {
            CouponInfoDO newCouponInfoDO = new CouponInfoDO();
            BeanUtils.copyProperties(couponInfoDO, newCouponInfoDO);
            newCouponInfoDO.setCouponId(null);
            newCouponInfoDO.setUserId(reqVO.getUserId());
            couponInfoMapper.insert(newCouponInfoDO);
            //异步发送微信通知
            workWxService.sendGiftCouponMsg(Long.valueOf(couponInfoDO.getStoreId()), reqVO.getUserId(), couponInfoDO.getCouponName(), couponInfoDO.getRoomType());
        }

    }

    @Override
    public PageResult<AppAdminUserPageRespVO> getAdminUserPage(AppClearUserPageReqVO reqVO) {
        String ids = "";
        if (ObjectUtils.isEmpty(reqVO.getStoreId())) {
            //查询该账号权限下的
            List<String> storeIds = storeUserMapper.getIdsByUserIdAndAdmin(getLoginUserId());
            ids = storeIds.stream().collect(Collectors.joining(","));
        } else {
            storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.ADMIN.getValue());
            ids = String.valueOf(reqVO.getStoreId());
        }
        IPage<AppAdminUserPageRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        storeUserMapper.getAdminUserPage(page, reqVO.getStoreId(), ids);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional
    public void deleteAdminUser(Long storeId, Long userId) {
        //检查权限
        storeInfoService.checkPermission(storeId, getLoginUserId(), null, AppEnum.member_user_type.BOSS.getValue());
        //不能删除自己
        if (userId.compareTo(getLoginUserId()) == 0) {
            throw exception(OPRATION_ERROR);
        }
        storeUserMapper.deleteAdminUser(storeId, userId);
        //如果该用户不再属于任何店的管理员  则清除身份
        int i = storeUserMapper.countPermission(userId);
        if (i == 0) {
            appUserService.updateUserType(userId, AppEnum.member_user_type.MEMBER.getValue());
        }
    }

    @Override
    public void saveAdminUser(AppClearUserDetailReqVO reqVO) {
        //检查权限
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.BOSS.getValue());
        //找出用户
        MemberUserDO memberUserDO = memberUserMapper.selectByMobile(reqVO.getMobile());
        if (ObjectUtils.isEmpty(memberUserDO)) {
            //不存在则创建用户
            memberUserDO = appUserService.createUserIfAbsent(reqVO.getMobile(), getClientIP());
//            throw exception(AUTH_USER_PHONE_ERROR);
        }
        if (memberUserDO.getId().compareTo(getLoginUserId()) == 0) {
            throw exception(OPRATION_ERROR);
        }
        //保洁员不可以改成管理员  但是如果用户没有任何绑定关系了 那么是可以的
        if (memberUserDO.getUserType().compareTo(AppEnum.member_user_type.CLEAR.getValue()) == 0) {
            List<String> idsByUserIdAndAdmin = storeUserMapper.getIdsByUserIdAndAdmin(memberUserDO.getId());
            if (!CollectionUtils.isEmpty(idsByUserIdAndAdmin)) {
                throw exception(USER_TYPE_CHECK_ERROR);
            }
        }
        Integer type = AppEnum.member_user_type.ADMIN.getValue();
        if (reqVO.getIsAdmin()) {
            type = AppEnum.member_user_type.BOSS.getValue();
        }
        //不是管理员角色 则改成管理员 如果是加盟商  则身份不变
        if (memberUserDO.getUserType().compareTo(AppEnum.member_user_type.ADMIN.getValue()) != 0
                && memberUserDO.getUserType().compareTo(AppEnum.member_user_type.BOSS.getValue()) != 0) {
            memberUserDO.setUserType(type);
            memberUserMapper.updateById(memberUserDO);
        }
        //如果已经存在门店与用户的关系 就修改关系
        StoreUserDO storeUserDO = storeUserMapper.getByUserIdAndStoreId(memberUserDO.getId(), reqVO.getStoreId());
        if (!ObjectUtils.isEmpty(storeUserDO)) {
            storeUserDO.setType(type);
            storeUserDO.setName(reqVO.getName());
            storeUserMapper.updateById(storeUserDO);
        } else {
            //不存在就添加
            storeUserDO = new StoreUserDO();
            storeUserDO.setUserId(memberUserDO.getId());
            storeUserDO.setType(type);
            storeUserDO.setName(reqVO.getName());
            storeUserDO.setStoreId(reqVO.getStoreId());
            storeUserMapper.insert(storeUserDO);
        }
    }

    @Override
    @Transactional
    public void renew(OrderRenewalReqVO reqVO) {
        OrderInfoDO orderInfoDO = orderInfoMapper.selectById(reqVO.getOrderId());
        if (orderInfoDO.getStartTime().after(reqVO.getEndTime())) {
            throw exception(ORDER_START_TIME_GT_END_ERROR);
        }
        //权限校验
        Long userId = getLoginUserId();
        storeInfoService.checkPermission(orderInfoDO.getStoreId(), userId, null, AppEnum.member_user_type.ADMIN.getValue());
        //未开始=0 进行中=1  已完成=2  已取消=3
        switch (orderInfoDO.getStatus()) {
            case 0:
            case 1:
                //未开始和进行中  直接续费
                break;
            case 2://已完成，不能续费
                throw exception(ORDER_STATUS_FINISH_OPRATION_ERROR);
            case 3://已经取消，不能续费
                throw exception(ORDER_STATUS_CANCEL_OPRATION_ERROR);
        }
        RoomInfoDO roomInfoDO = roomInfoMapper.selectById(orderInfoDO.getRoomId());
//        StoreInfoDO storeInfoDO = storeInfoMapper.selectById(roomInfoDO.getStoreId());
        //如果是减少时间 不用校验时间冲突
        if (orderInfoDO.getEndTime().before(reqVO.getEndTime())) {
            //增加时间
            //管理员续费  不需要算钱了，但是要校验时间冲突
            appOrderService.checkOrderTime(0, roomInfoDO, orderInfoDO.getStartTime(), orderInfoDO.getEndTime(), reqVO.getOrderId());
            //如果状态是已完成  则状态改成进行中 并触发一次通电 还要清除保洁订单信息
            if (orderInfoDO.getStatus().compareTo(AppEnum.order_status.FINISH.getValue()) == 0 && reqVO.getEndTime().after(new Date())) {
                orderInfoDO.setStatus(AppEnum.order_status.START.getValue());
                deviceService.openRoomDoor(userId, roomInfoDO.getStoreId(), roomInfoDO.getRoomId(), 1);
                roomInfoMapper.updateStatusById(AppEnum.room_status.USED.getValue(), roomInfoDO.getRoomId());
                clearInfoMapper.cancelByRoomId(roomInfoDO.getRoomId());
            }
        }
        //修改订单的结束时间
        orderInfoDO.setEndTime(reqVO.getEndTime());
        orderInfoMapper.updateById(orderInfoDO);
        //同步美团预订时间
        //异步发送微信通知
        workWxService.sendRenewMsg(roomInfoDO.getStoreId(), getLoginUserId(), roomInfoDO.getRoomName(), BigDecimal.ZERO, reqVO.getPayType(), orderInfoDO.getOrderNo(), orderInfoDO.getEndTime(), null, true);
        //如果订单是进行中 -- 已下线（新IoT平台不支持）
        // if (orderInfoDO.getStatus().compareTo(AppEnum.order_status.START.getValue()) == 0) {
        //     deviceService.runClock(orderInfoDO.getRoomId(), orderInfoDO.getStartTime(), orderInfoDO.getEndTime(), 0);
        // }
    }

    @Override
    public PageResult<AppClearPageRespVO> getClearManagerPage(AppClearPageReqVO reqVO) {
        //权限检查
        storeInfoService.checkPermission(null, null, getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //查询出账号权限的门店
        List<String> storeIds = storeUserMapper.getIdsByUserIdAndAdmin(getLoginUserId());
        if (CollectionUtils.isEmpty(storeIds)) {
            return PageResult.empty();
        }
        reqVO.setStoreIds(storeIds.stream().collect(Collectors.joining(",")));
        IPage<AppClearPageRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        clearInfoMapper.getClearManagerPage(page, reqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, boolean refund) {
        OrderInfoDO orderInfoDO = orderInfoMapper.selectById(orderId);
        Long userId = orderInfoDO.getUserId();
        CouponInfoDO couponInfoDO = null;
        //权限检查
        storeInfoService.checkPermission(orderInfoDO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.ADMIN.getValue());
        boolean cancelFlag = true;//默认允许取消订单
        //对于管理员 未开始和进行中的订单  都可以取消  不判断下单时间  但是团购下单的不退团购券
        cancelFlag = orderInfoDO.getStatus().compareTo(AppEnum.order_status.PENDING.getValue()) == 0
                || orderInfoDO.getStatus().compareTo(AppEnum.order_status.START.getValue()) == 0;
        if (cancelFlag) {
            if (refund) {
                //进行退款
                // 请注意  这里管理员退款，是没有退还团购券的，让用户自己去团购平台申请退款
                orderInfoDO.setRefundPrice(orderInfoDO.getPayPrice());
                //退还优惠券
                if (!ObjectUtils.isEmpty(orderInfoDO.getCouponId())) {
                    couponInfoDO = couponInfoMapper.selectById(orderInfoDO.getCouponId());
                    if (couponInfoDO.getExpriceTime().after(new Date())) {
                        couponInfoDO.setStatus(AppEnum.coupon_status.AVAILABLE.getValue());
                    } else {
                        couponInfoDO.setStatus(AppEnum.coupon_status.EXPIRE.getValue());
                    }
                    couponInfoMapper.updateById(couponInfoDO);
                }
                //如果有使用套餐，则把套餐设置过期  然后再退款
                PkgUserInfoDO pkgUserInfoDO = pkgUserInfoMapper.getByOrderId(orderId);
                if (!ObjectUtils.isEmpty(pkgUserInfoDO)) {
                    pkgUserInfoMapper.updateById(new PkgUserInfoDO().setId(pkgUserInfoDO.getId()).setStatus(AppEnum.coupon_status.EXPIRE.getValue()));
                }
                //进行微信退款 （微信支付押金或微信续费的金额） 取消是全退
                payOrderService.refundByOrder(orderInfoDO.getOrderId(), orderInfoDO.getOrderNo(), orderInfoDO.getStoreId());
                //进行余额退款 （下单或续费的金额）
                payOrderService.refundBalance(orderInfoDO.getStoreId(), orderInfoDO.getOrderNo(), orderInfoDO.getUserId());
                //如果是预订或平台订单  通知平台进行退款
                if (orderInfoDO.getOrderNo().startsWith("Y2") || orderInfoDO.getOrderNo().startsWith("PL")) {
                }
            } else {
                //取消订单
                //如果是预订订单  管理员取消订单，则告知平台到店核销（不退款）
                if (orderInfoDO.getPayType().compareTo(AppEnum.order_pay_type.YUDING.getValue()) == 0) {
                }
            }
            orderInfoDO.setDepositRefund(true);//押金这里会退
            //取消的订单已开始  那就触发一下关门 orderCount=1
            if (orderInfoDO.getStatus().compareTo(AppEnum.order_status.START.getValue()) == 0) {
                deviceService.closeRoomDoor(userId, orderInfoDO.getStoreId(), orderInfoDO.getRoomId(), 2, 1);
            }
            //设置订单状态为取消
            orderInfoDO.setStatus(AppEnum.order_status.CANCEL.getValue());
            orderInfoMapper.updateById(orderInfoDO);
            //刷新房间状态
            appOrderService.flushRoomStatus(orderInfoDO.getRoomId(), null);
            //更新房间库存
            //异步发送微信通知
            workWxService.sendOrderCancelMsg(orderInfoDO.getStoreId(), userId, orderInfoDO.getRoomId(), orderInfoDO.getPayPrice()
                    , couponInfoDO, orderInfoDO.getPayType(), orderInfoDO.getGroupPayType(), orderInfoDO.getOrderNo(), true);
        } else {
            throw exception(ADMIN_ORDER_OPRATION_ERROR);
        }
    }


    @Override
    @Transactional
    public void useGroupNo(AppUseGroupNoReqVO reqVO) {
        reqVO.setGroupPayNo(com.cloudsteward.module.member.service.groupPay.GroupBuyPlatform.normalize(reqVO.getGroupPayNo()));
        Integer platform = com.cloudsteward.module.member.service.groupPay.GroupBuyPlatform.detect(reqVO.getGroupPayNo());
        IotGroupPayPrepareRespVO prepare = groupPayInfoService.prepare(reqVO.getStoreId(), platform, reqVO.getGroupPayNo());
        //把券使用了
        GroupPayInfoDO groupPayInfoDO = groupPayInfoService.consume(reqVO.getStoreId(), platform, reqVO.getGroupPayNo(), prepare);
        //异步发送微信通知
        workWxService.sendUseGroupNoMsg(groupPayInfoDO, getLoginUserId());
    }

    @Override
    @Transactional
    public void changeOrderUser(AppChangeOrderUserReqVO reqVO) {
        //根据手机号 查询出用户
        MemberUserDO user = appUserService.getUserByMobile(reqVO.getMobile());
        if (ObjectUtils.isEmpty(user)) {
//            throw exception(USER_NOT_EXISTS);
            //用户不存在则自动创建
            user = appUserService.createUserIfAbsent(reqVO.getMobile(), getClientIP());
        }
        OrderInfoDO orderInfoDO = orderInfoMapper.selectById(reqVO.getOrderId());
        if (!ObjectUtils.isEmpty(orderInfoDO)) {
            //权限检查
            storeInfoService.checkPermission(orderInfoDO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.ADMIN.getValue());
            if (orderInfoDO.getUserId().compareTo(user.getId()) == 0) {
                return;
            }
            //订单状态检查
            if (orderInfoDO.getStatus().compareTo(AppEnum.order_status.PENDING.getValue()) == 0
                    || orderInfoDO.getStatus().compareTo(AppEnum.order_status.PENDING.getValue()) == 1) {
                //只有未开始和进行中可以更换用户  其他状态也没有更换的必要
                orderInfoMapper.changeOrderUser(reqVO.getOrderId(), user.getId());
                //发送企业微信通知
                workWxService.sendOrderChangeUserMsg(orderInfoDO.getStoreId(), orderInfoDO.getOrderNo(), orderInfoDO.getRoomId()
                        , orderInfoDO.getStartTime(), orderInfoDO.getEndTime(), user.getId());
            } else {
                throw exception(CLEAR_ORDER_STATUS_ERROR);
            }

        }
    }

    @Override
    @Transactional
    public void changeOrder(OrderChangeReqVO reqVO) {
        //检查订单时间合法性
        OrderInfoDO orderInfoDO = orderInfoMapper.selectById(reqVO.getOrderId());
        Long oldRoomId = orderInfoDO.getRoomId();
        Long userId = orderInfoDO.getUserId();
        //权限检查
        storeInfoService.checkPermission(orderInfoDO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.ADMIN.getValue());
        //订单状态检查 只有未开始、进行中的订单，才允许修改
        boolean flag = true;//默认允许修改订单
        flag = orderInfoDO.getStatus().compareTo(AppEnum.order_status.PENDING.getValue()) == 0 || orderInfoDO.getStatus().compareTo(AppEnum.order_status.START.getValue()) == 0;
        if (flag) {
            //检查目标房间的时间是否占用
            RoomInfoDO roomInfoDO = roomInfoMapper.selectById(reqVO.getRoomId());
            appOrderService.checkOrderTime(0, roomInfoDO, reqVO.getStartTime(), reqVO.getEndTime(), reqVO.getOrderId());
            //开始修改
            //改时间
            orderInfoDO.setStartTime(reqVO.getStartTime());
            orderInfoDO.setEndTime(reqVO.getEndTime());
            if (!ObjectUtils.isEmpty(reqVO.getRoomId())) {
                orderInfoDO.setRoomId(reqVO.getRoomId());
                //判断是否更换了房间
                if (reqVO.getRoomId().compareTo(oldRoomId) != 0) {
                    //如果这个订单在进行中  则关闭房间电源 orderCount=1,因为这里关电的时候，订单的状态还没有被改变
                    if (orderInfoDO.getStatus().compareTo(AppEnum.order_status.START.getValue()) == 0) {
                        deviceService.closeRoomDoor(getLoginUserId(), orderInfoDO.getStoreId(), oldRoomId, 2, 1);
                    }
                }
            }
            if (reqVO.getStartTime().after(new Date())) {
                //开始时间在当前时间以后 订单改成未开始
                if (orderInfoMapper.countByRoomCurrent(orderInfoDO.getRoomId(), null) == 0) {
                    deviceService.closeRoomDoor(getLoginUserId(), orderInfoDO.getStoreId(), orderInfoDO.getRoomId(), 2, 1);
                }
                orderInfoDO.setStatus(AppEnum.order_status.PENDING.getValue());
            }
            orderInfoMapper.updateById(orderInfoDO);
            if (!ObjectUtils.isEmpty(reqVO.getRoomId())) {
                if (reqVO.getRoomId().compareTo(oldRoomId) != 0) {
                    //刷新 新房间的状态
                    appOrderService.flushRoomStatus(reqVO.getRoomId(), null);
                    //同步一下预订平台的房间占用信息
                }
            }
            //刷新 旧房间的状态
            appOrderService.flushRoomStatus(oldRoomId, null);
            //同步一下预订平台的房间占用信息
            //发送消息到企业微信
            workWxService.sendChangeMsg(orderInfoDO.getStoreId(), orderInfoDO.getOrderNo(), orderInfoDO.getStartTime(), orderInfoDO.getEndTime(), oldRoomId, orderInfoDO.getRoomId(), userId);
            //如果订单是进行中 -- 已下线（新IoT平台不支持）
            // if (orderInfoDO.getStatus().compareTo(AppEnum.order_status.START.getValue()) == 0) {
            //     deviceService.runClock(orderInfoDO.getRoomId(), orderInfoDO.getStartTime(), orderInfoDO.getEndTime(), 0);
            // }
        } else {
            throw exception(ADMIN_ORDER_OPRATION_ERROR);
        }
    }

    @Override
    public List<AppIncomeStatisticsRespVO> getIncomeStatistics(AppChartDataReqVO reqVO) {
        //仅管理员使用
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        reqVO.setUserId(getLoginUserId());
        return orderInfoMapper.getIncomeStatistics(reqVO);
    }

    @Override
    public List<AppRechargeStatisticsRespVO> getRechargeStatistics(AppChartDataReqVO reqVO) {
        //仅管理员使用
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        reqVO.setUserId(getLoginUserId());
        return orderInfoMapper.getRechargeStatistics(reqVO);
    }

    private String getOrderNo() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDateTime currentDateTime = LocalDateTime.now();
        String currentDate = currentDateTime.format(dateFormatter);
        Random random = new Random();
        int randomNum = random.nextInt(100000000);
        String randomNumString = String.format("%08d", randomNum);
        return currentDate + randomNumString;
    }

    @Override
    @Transactional
    public Long submitOrder(OrderSubmitReqVO reqVO) {
        //根据手机号 查询出用户
        MemberUserDO user = appUserService.getUserByMobile(reqVO.getMobile());
        if (ObjectUtils.isEmpty(user)) {
//            throw exception(USER_NOT_EXISTS);
            //用户不存在则自动创建
            user = appUserService.createUserIfAbsent(reqVO.getMobile(), getClientIP());
        }
        RoomInfoDO roomInfoDO = roomInfoMapper.selectById(reqVO.getRoomId());
        StoreInfoDO storeInfoDO = storeInfoMapper.selectById(roomInfoDO.getStoreId());
        //仅管理员使用
        storeInfoService.checkPermission(roomInfoDO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //定义一些参数 备用
        OrderInfoDO orderInfoDO = new OrderInfoDO();
        if (reqVO.getStartTime().after(reqVO.getEndTime())) {
            throw exception(ORDER_START_TIME_GT_END_ERROR);
        }
        //下单检查一遍可用时间
        appOrderService.checkOrderTime(0, roomInfoDO, reqVO.getStartTime(), reqVO.getEndTime(), null);
        //生成订单，并修改房间状态
        orderInfoDO.setOrderNo(getOrderNo());
        orderInfoDO.setOrderKey(HexUtil.encodeHexStr(orderInfoDO.getOrderNo()) + UUID.randomUUID().toString());
        orderInfoDO.setStoreId(roomInfoDO.getStoreId());
        orderInfoDO.setRoomId(roomInfoDO.getRoomId());
        orderInfoDO.setUserId(user.getId());
        orderInfoDO.setStartTime(reqVO.getStartTime());
        orderInfoDO.setEndTime(reqVO.getEndTime());
        orderInfoDO.setNightLong(false);
        orderInfoDO.setPrice(reqVO.getPrice());
        orderInfoDO.setPayPrice(BigDecimal.ZERO);
        orderInfoDO.setRefundPrice(BigDecimal.ZERO);
        orderInfoDO.setPayType(AppEnum.order_pay_type.ADMIN.getValue());
//        orderInfoDO.setStatistics(reqVO.getStatistics());
        orderInfoMapper.insert(orderInfoDO);
        //如果房间状态是待清洁，就发送提醒保洁的通知
        if (roomInfoDO.getStatus().compareTo(AppEnum.room_status.CLEAR.getValue()) == 0) {
            //异步发送微信通知
            workWxService.sendOrderClearMsg(roomInfoDO.getStoreId(), roomInfoDO.getRoomName(), orderInfoDO.getStartTime(), orderInfoDO.getEndTime());
        }
        //如果房间状态是空闲，就改成已预定
        else if (roomInfoDO.getStatus().compareTo(AppEnum.room_status.ENABLE.getValue()) == 0) {
            roomInfoMapper.updateStatusById(AppEnum.room_status.PENDING.getValue(), roomInfoDO.getRoomId());
        }
        //更新美团库存
        //异步发送微信通知
        workWxService.sendOrderSubmitMsg(roomInfoDO.getStoreId(), getLoginUserId(), reqVO.getMobile(), roomInfoDO.getRoomName(), orderInfoDO.getOrderNo(), orderInfoDO.getStartTime(), orderInfoDO.getEndTime());
        return orderInfoDO.getOrderId();
    }

    @Override
    @Transactional
    public void recharge(AppUserRechargeReqVO reqVO) {
        //权限检查
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.BOSS.getValue());
        //执行
        memberUserService.recharge(reqVO);
        //发送企业微信通知
        workWxService.sendAdminRechargeMsg(reqVO.getStoreId(), reqVO.getUserId(), getLoginUserId(), reqVO.getMoney());
    }

    @Override
    @Transactional
    public void cancelClear(Long clearId) {
        ClearInfoDO clearInfoDO = clearInfoMapper.selectById(clearId);
        if (!ObjectUtils.isEmpty(clearInfoDO)) {
            //权限检查
            storeInfoService.checkPermission(clearInfoDO.getStoreId(), getLoginUserId(), null, AppEnum.member_user_type.ADMIN.getValue());
            //执行
            clearInfoDO.setStatus(4);
            clearInfoDO.setComplaintDesc("管理员取消");
            clearInfoMapper.updateById(clearInfoDO);
        }
    }

    @Override
    public List<AppCouponPageRespVO> getUserCouponByAdmin(AppManagerUserCouponListReqVO reqVO) {
        //权限检查
        storeInfoService.checkPermission(null, null, getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        String storeIds = storeUserMapper.getIdsByUserIdAndAdmin(getLoginUserId()).stream().collect(Collectors.joining(","));
        if (StringUtils.isEmpty(storeIds)) {
            return new ArrayList<>();
        }
        reqVO.setStoreIds(storeIds);
        return couponInfoMapper.getUserCouponByAdmin(reqVO.getUserId(), reqVO.getStoreIds());
    }

    @Override
    @Transactional
    public void revokeCoupon(Long id) {
        //查询出优惠券
        CouponInfoDO couponInfoDO = couponInfoMapper.selectById(id);
        if (ObjectUtils.isEmpty(couponInfoDO)) {
            throw exception(DATA_NOT_EXISTS);
        }
        if (couponInfoDO.getStatus().compareTo(AppEnum.coupon_status.AVAILABLE.getValue()) != 0) {
            throw exception(COUPON_IS_NOT_ENABLE);
        }
        //权限检查
        storeInfoService.checkPermission(couponInfoDO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        //删除优惠券
        couponInfoMapper.deleteById(id);
    }

    @Override
    public PageResult<AppVipPageRespVO> getVipPage(AppVipPageReqVO reqVO) {
        IPage<AppVipPageRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        appUserMapper.getVipPage(page, reqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional
    public void depositRefund(Long orderId) {
        OrderInfoDO orderInfoDO = orderInfoMapper.selectById(orderId);
        if (!ObjectUtils.isEmpty(orderInfoDO)) {
            //权限检查
            storeInfoService.checkPermission(orderInfoDO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
            //如果押金已经退了 或者没有押金  就不让退
            payOrderService.refundDeposit(orderId, orderInfoDO.getOrderNo(), orderInfoDO.getDeposit(), orderInfoDO.getUserId(), orderInfoDO.getStoreId(), orderInfoDO.getDepositRefund());
            orderInfoMapper.updateById(new OrderInfoDO().setOrderId(orderId).setDepositRefund(true).setRefundPrice(orderInfoDO.getDeposit()));
        }
    }

    @Override
    @Transactional
    public void deleteVip(AppVipDeleteReqVO reqVO) {
        //权限检查
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        StoreUserDO storeUserDO = storeUserMapper.getByUserIdAndStoreId(reqVO.getUserId(), reqVO.getStoreId());
        if (!ObjectUtils.isEmpty(storeUserDO)) {
            storeUserDO.setVipLevel((byte) 0);
            storeUserDO.setTotalScore(0);
            storeUserMapper.updateById(storeUserDO);
        }

    }

    @Override
    @Transactional
    public void deleteCoupon(Long couponId) {
        CouponInfoDO couponInfoDO = couponInfoMapper.selectById(couponId);
        if (!ObjectUtils.isEmpty(couponInfoDO)) {
            //权限检查
            storeInfoService.checkPermission(couponInfoDO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
            couponInfoMapper.deleteById(couponId);
        }
    }


    @Override
    public PageResult<AppPayOrderRespVO> getPayOrderPage(AppPayOrderReqVO reqVO) {
        //权限检查
        storeInfoService.checkPermission(reqVO.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
        IPage<AppPayOrderRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        payOrderMapper.getPayOrderPage(page, reqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional
    public void refundPayOrder(AppPayRefundReqVO reqVO) {
        PayOrderDO payOrder = payOrderService.getPayOrder(reqVO.getId());
        if (!ObjectUtils.isEmpty(payOrder)) {
            //权限检查
            storeInfoService.checkPermission(payOrder.getStoreId(), getLoginUserId(), getLoginUserType(), AppEnum.member_user_type.ADMIN.getValue());
            //支付状态检查
            if (!payOrder.getPayStatus()) {
                throw exception(PAY_ORDER_REFUND_ERROR);
            }
            //退款金额检查
            int price = payOrder.getPrice() - payOrder.getRefundPrice();
            if (price < reqVO.getPrice()) {
                //金额不够退款
                throw exception(PAY_ORDER_REFUND_MAX_ERROR, price / 100.0);
            }
            //执行退款
            payOrderService.refund(payOrder, reqVO.getPrice(), "管理员手动退款");

        }
    }

}
