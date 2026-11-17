package com.cloudsteward.module.member.service.payorder;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.cloudsteward.framework.common.exception.ServiceException;
import com.cloudsteward.framework.common.pojo.PageResult;
import com.cloudsteward.framework.tenant.core.context.TenantContextHolder;
import com.cloudsteward.framework.tenant.core.util.TenantUtils;
import com.cloudsteward.module.infra.api.config.ConfigApi;
import com.cloudsteward.module.member.controller.admin.payorder.vo.PayOrderExportReqVO;
import com.cloudsteward.module.member.controller.admin.payorder.vo.PayOrderPageReqVO;
import com.cloudsteward.module.member.controller.admin.payorder.vo.PayOrderUpdateReqVO;
import com.cloudsteward.module.member.controller.app.order.vo.OrderRenewalReqVO;
import com.cloudsteward.module.member.controller.app.order.vo.OrderSaveReqVO;
import com.cloudsteward.module.member.controller.app.order.vo.WxPayOrderInfo;
import com.cloudsteward.module.member.controller.app.order.vo.WxPayOrderRespVO;
import com.cloudsteward.module.member.controller.app.pkg.vo.AppBuyPkgReqVO;
import com.cloudsteward.module.member.controller.app.store.vo.AppStoreVipConfigListRespVO;
import com.cloudsteward.module.member.controller.app.user.vo.AppRechargeBalanceReqVO;
import com.cloudsteward.module.member.dal.dataobject.payorder.PayOrderDO;
import com.cloudsteward.module.member.dal.dataobject.storeinfo.StoreInfoDO;
import com.cloudsteward.module.member.dal.dataobject.storepayconfig.StorePayConfigDO;
import com.cloudsteward.module.member.dal.dataobject.storepaysplit.StorePaySplitDO;
import com.cloudsteward.module.member.dal.dataobject.storeuser.StoreUserDO;
import com.cloudsteward.module.member.dal.dataobject.usermoneybill.UserMoneyBillDO;
import com.cloudsteward.module.member.dal.mysql.payorder.PayOrderMapper;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storepayconfig.StorePayConfigMapper;
import com.cloudsteward.module.member.dal.mysql.storepaysplit.StorePaySplitMapper;
import com.cloudsteward.module.member.dal.mysql.storeuser.StoreUserMapper;
import com.cloudsteward.module.member.dal.mysql.storevipconfig.StoreVipConfigMapper;
import com.cloudsteward.module.member.dal.mysql.usermoneybill.UserMoneyBillMapper;
import com.cloudsteward.module.member.enums.AppEnum;
import com.cloudsteward.module.member.enums.AppWxPayTypeEnum;
import com.cloudsteward.module.member.service.iot.IotService;
import com.cloudsteward.module.member.service.order.AppOrderService;
import com.cloudsteward.module.member.service.pkg.PkgService;
import com.cloudsteward.module.member.service.productorder.ProductOrderService;
import com.cloudsteward.module.member.service.user.AppUserService;
import com.cloudsteward.module.member.service.wx.MyWxService;
import com.cloudsteward.module.member.service.wx.WorkWxService;
import com.cloudsteward.module.system.api.social.SocialUserApi;
import com.cloudsteward.module.system.enums.social.SocialTypeEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static com.cloudsteward.module.member.enums.AppEnum.WX_PAY_ORDER;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.*;

/**
 * 支付订单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class PayOrderServiceImpl implements PayOrderService {


    @Resource
    private PayOrderMapper payOrderMapper;

    @Autowired
    private MyWxService myWxService;
    @Resource
    private com.cloudsteward.module.member.service.wx.partner.WechatPartnerPaymentService partnerPayments;
    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private AppUserService appUserService;
    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private AppOrderService appOrderService;

    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private PkgService pkgService;

    @Resource
    private StoreInfoMapper storeInfoMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    @Lazy
    private IotService iotService;


    @Resource
    private StoreVipConfigMapper storeVipConfigMapper;

    @Resource
    private StorePayConfigMapper storePayConfigMapper;

    @Resource
    private StorePaySplitMapper storePaySplitMapper;

    @Resource
    private SocialUserApi socialUserApi;

    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private ProductOrderService productOrderService;


    @Resource
    private WorkWxService workWxService;

    @Resource
    private ConfigApi configApi;

    private String hostname = "app.hostname";

    private String getRedirectUrl() {
        return String.format("https://%s/app-api/callback/yeepay", configApi.getConfigValueByKey(hostname));
    }

    public String secret() {
        return configApi.getConfigValueByKey("iot.secret");
    }

    @Override
    public PayOrderDO getPayOrder(Long id) {
        return payOrderMapper.selectById(id);
    }

    @Override
    public List<PayOrderDO> getPayOrderList(Collection<Long> ids) {
        return payOrderMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PayOrderDO> getPayOrderPage(PayOrderPageReqVO pageReqVO) {
        //只能查询自己门店的
//        Long tenantId = TenantContextHolder.getTenantId();
        List<StoreInfoDO> storeInfoDOS = storeInfoMapper.selectList();
        if (!CollectionUtils.isEmpty(storeInfoDOS)) {
            pageReqVO.setStoreIds(storeInfoDOS.stream().map(x -> x.getStoreId()).collect(Collectors.toList()));
            return payOrderMapper.selectPage(pageReqVO);
        } else {
            return PageResult.empty();
        }

    }

    @Override
    public List<PayOrderDO> getPayOrderList(PayOrderExportReqVO exportReqVO) {
        return payOrderMapper.selectList(exportReqVO);
    }

    /**
     * 微信支付回调
     *
     * @param xmlData
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateWxOrder(String xmlData) {
        try {
            WxPayOrderNotifyResult hint = WxPayOrderNotifyResult.fromXML(xmlData);
            String orderNo = hint.getOutTradeNo();
            if (orderNo == null || partnerPayments.hasAttempt(orderNo)) return WxPayNotifyResponse.fail("支付通知通道不匹配");
            PayOrderDO payment = payOrderMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PayOrderDO>().eq(PayOrderDO::getOrderNo,orderNo).last("FOR UPDATE"));
            if (payment == null) return WxPayNotifyResponse.fail("支付订单不存在");
            WxPayService service = myWxService.initWxPay(payment.getStoreId());
            WxPayOrderNotifyResult result = service.parseOrderNotifyResult(xmlData);
            if (!java.util.Objects.equals(service.getConfig().getMchId(),result.getMchId())
                    || !java.util.Objects.equals(service.getConfig().getAppId(),result.getAppid())
                    || !java.util.Objects.equals(payment.getPrice(),result.getTotalFee())
                    || !"SUCCESS".equals(result.getReturnCode()) || !"SUCCESS".equals(result.getResultCode())) return WxPayNotifyResponse.fail("支付通知核对失败");
            if (Boolean.TRUE.equals(payment.getPayStatus())) {
                return java.util.Objects.equals(payment.getPayOrderNo(),result.getTransactionId()) ? WxPayNotifyResponse.success("已处理") : WxPayNotifyResponse.fail("交易号不一致");
            }
            payment.setPayOrderNo(result.getTransactionId());payment.setPayStatus(true);payment.setPayTime(LocalDateTime.now());
            payOrderMapper.updateById(payment);payCallback(payment);return WxPayNotifyResponse.success("接收成功");
        } catch (Exception e) {
            org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return WxPayNotifyResponse.fail("支付处理未完成，请重试");
        }
    }

    /**
     * 通用支付回调处理
     *
     * @return
     */
    public void payCallback(PayOrderDO payOrderDO) {
        String redisKey = String.format(WX_PAY_ORDER, payOrderDO.getOrderNo());
        WxPayOrderInfo context = (WxPayOrderInfo) redisTemplate.opsForValue().get(redisKey);
        if (context == null) throw exception(ORDER_WEIXIN_PAY_ERROR);
        applyPartnerPayment(payOrderDO,context);
    }

    @Override
    public void retainPartnerContext(String orderNo) {
        if (!partnerPayments.hasAttempt(orderNo)) return;
        WxPayOrderInfo info = (WxPayOrderInfo) redisTemplate.opsForValue().get(String.format(WX_PAY_ORDER,orderNo));
        if (info == null) info = (WxPayOrderInfo) redisTemplate.opsForValue().get(String.format(com.cloudsteward.module.member.enums.AppEnum.WX_PRODUCT_PAY_ORDER,orderNo));
        WxPayOrderRespVO preorder=(WxPayOrderRespVO)redisTemplate.opsForValue().get("PreOrderRespVO_"+orderNo);
        if (info != null) partnerPayments.retainContext(orderNo,info,preorder);
    }

    @Override
    public boolean completePartnerBusiness(String orderNo,AppWxPayTypeEnum type) {return partnerPayments.completeBusiness(orderNo,type);}

    @Override
    public void applyPartnerRefund(PayOrderDO payment,Integer amount) {
        refundedScore(amount,payment.getUserId(),payment.getStoreId());
    }

    @Override
    public void applyPartnerPayment(PayOrderDO payOrderDO, WxPayOrderInfo wxPayOrderInfo) {
        String orderNo=payOrderDO.getOrderNo();
        Long tenantId=wxPayOrderInfo.getTenantId();
            //模拟租户 处理支付订单
            TenantUtils.execute(tenantId, () -> {
                if (orderNo.startsWith("SH")) {
                    //商品订单
                    productOrderService.paymentOrder(payOrderDO);
                } else if (wxPayOrderInfo.getWxPayTypeEnum().compareTo(AppWxPayTypeEnum.ORDER) == 0 && (Integer.valueOf(1).equals(payOrderDO.getPayType())||Integer.valueOf(4).equals(payOrderDO.getPayType()))) {
                    //下单
                    if(partnerPayments.hasAttempt(orderNo))redisTemplate.opsForValue().set("PreOrderRespVO_"+orderNo,partnerPayments.getPreOrder(orderNo),5,java.util.concurrent.TimeUnit.MINUTES);
                    OrderSaveReqVO reqVO = new OrderSaveReqVO();
                    reqVO.setOrderNo(orderNo);
                    reqVO.setPayType(payOrderDO.getPayType());
                    reqVO.setCouponId(wxPayOrderInfo.getCouponId());
                    reqVO.setNightLong(wxPayOrderInfo.getNightLong());
                    reqVO.setRoomId(wxPayOrderInfo.getRoomId());
                    reqVO.setStartTime(wxPayOrderInfo.getStartTime());
                    reqVO.setEndTime(wxPayOrderInfo.getEndTime());
                    reqVO.setUserId(wxPayOrderInfo.getUserId());
                    reqVO.setPkgId(wxPayOrderInfo.getPkgId());
                    reqVO.setPreSubmit(wxPayOrderInfo.getPreSubmit());
                    appOrderService.save(reqVO);
                } else if (wxPayOrderInfo.getWxPayTypeEnum().compareTo(AppWxPayTypeEnum.RENEW) == 0) {
                    //续费
                    OrderRenewalReqVO orderRenewalReqVO = new OrderRenewalReqVO();
                    orderRenewalReqVO.setOrderId(wxPayOrderInfo.getIgnoreOrderId());
                    orderRenewalReqVO.setOrderNo(wxPayOrderInfo.getOrderNo());
                    orderRenewalReqVO.setEndTime(wxPayOrderInfo.getEndTime());
                    orderRenewalReqVO.setPayType(AppEnum.order_pay_type.WEIXIN.getValue());
                    orderRenewalReqVO.setUserId(wxPayOrderInfo.getUserId());
                    orderRenewalReqVO.setPkgId(wxPayOrderInfo.getPkgId());
                    orderRenewalReqVO.setCouponId(wxPayOrderInfo.getCouponId());
                    appOrderService.renew(orderRenewalReqVO);
                } else if (wxPayOrderInfo.getWxPayTypeEnum().compareTo(AppWxPayTypeEnum.RECHARGE) == 0) {
                    //充值
                    AppRechargeBalanceReqVO rechargeBalanceReqVO = new AppRechargeBalanceReqVO();
                    rechargeBalanceReqVO.setUserId(wxPayOrderInfo.getUserId());
                    rechargeBalanceReqVO.setOrderNo(orderNo);
                    rechargeBalanceReqVO.setStoreId(wxPayOrderInfo.getStoreId());
                    rechargeBalanceReqVO.setPrice(payOrderDO.getPrice());
                    appUserService.rechargeBalance(rechargeBalanceReqVO);
                } else if (wxPayOrderInfo.getWxPayTypeEnum().compareTo(AppWxPayTypeEnum.PKG) == 0) {
                    //购买套餐
                    AppBuyPkgReqVO reqVO = new AppBuyPkgReqVO();
                    reqVO.setOrderNo(orderNo);
                    reqVO.setUserId(wxPayOrderInfo.getUserId());
                    reqVO.setPkgId(wxPayOrderInfo.getPkgId());
                    reqVO.setPrice(payOrderDO.getPrice());
                    pkgService.buyPkg(reqVO);
                }
                //增加积分 这里要考虑押金造成的积分增加问题 包含押金的话 押金退款的时候要减去押金的部分
                StoreUserDO storeUserDO = storeUserMapper.getByUserIdAndStoreId(payOrderDO.getUserId(), payOrderDO.getStoreId());
                if (ObjectUtils.isEmpty(storeUserDO)) {
                    storeUserDO = new StoreUserDO()
                            .setStoreId(payOrderDO.getStoreId())
                            .setUserId(payOrderDO.getUserId())
                            .setType(AppEnum.member_user_type.MEMBER.getValue())
                            .setVipLevel((byte)0).setBalance(BigDecimal.ZERO).setGiftBalance(BigDecimal.ZERO)
                            .setTotalScore(payOrderDO.getPrice() / 100);
                    storeUserMapper.insert(storeUserDO);
                } else {
                    storeUserDO.setTotalScore(storeUserDO.getTotalScore() + (payOrderDO.getPrice() / 100));
                    storeUserMapper.updateById(storeUserDO);
                }
                //当用户到达积分门槛后  就升级VIP等级
                List<AppStoreVipConfigListRespVO> vipConfig = storeVipConfigMapper.getVipConfig(payOrderDO.getStoreId());
                if (!CollectionUtils.isEmpty(vipConfig)) {
                    for (AppStoreVipConfigListRespVO vip : vipConfig) {
                        if (storeUserDO.getTotalScore().intValue() >= vip.getScore().intValue()
                                && storeUserDO.getVipLevel().intValue() < vip.getVipLevel().intValue()) {
                            //如果当前就已经是那个等级了  就不用升级
                            storeUserDO.setVipLevel(vip.getVipLevel());
                            storeUserMapper.updateById(storeUserDO);
                            break;
                        }
                    }
                }
            });
    }

    /**
     * 积分退款
     *
     * @param totalFee
     * @param userId
     * @param storeId
     */
    private void refundedScore(Integer totalFee, Long userId, Long storeId) {
        StoreUserDO storeUserDO = storeUserMapper.getByUserIdAndStoreId(userId, storeId);
        if (ObjectUtils.isEmpty(storeUserDO)) {
            //账户都没有 就不管了
            return;
        } else {
            storeUserDO.setTotalScore(storeUserDO.getTotalScore() - (totalFee / 100));
            storeUserMapper.updateById(storeUserDO);
            //todo...收回积分后，还要考虑会员降级的问题，暂不处理，因为不是什么重要的东西
        }
    }

    /**
     * 微信退款回调
     *
     * @param xmlData
     * @return
     */
    @Override
    public String updateWxRefunded(String xmlData) {
//        log.info("收到微信支付退款回调body：{}", xmlData);
        return WxPayNotifyResponse.success("接收成功!");
    }

    /**
     * 创建支付订单
     *
     * @param userId
     * @param orderNo
     * @param orderId
     * @param storeId
     * @param payType
     * @param orderDesc
     * @param price
     * @param goodsName
     * @return
     */
    @Override
    @Transactional
    public WxPayOrderRespVO create(WxPayOrderRespVO respVO, Long userId, String orderNo, Long orderId, Long storeId, Integer payType, String orderDesc, Integer price, String goodsName, String merchantAppUrl) {
        if (partnerPayments.enabled() || partnerPayments.hasAttempt(orderNo)) {
            Long loggedUser = getLoginUserId();
            if (loggedUser == null || !loggedUser.equals(userId)) throw exception(USER_WEIXIN_PAY_ERROR);
            String openId = socialUserApi.getUserOpenIdByType(loggedUser, SocialTypeEnum.WECHAT_MINI_APP.getType());
            Long tenantId = TenantContextHolder.getTenantId();
            if (tenantId == null) tenantId = com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils.getLoginUser().getTenantId();
            PayOrderDO payment = PayOrderDO.builder().userId(userId).orderNo(orderNo).orderId(orderId).storeId(storeId)
                    .payType(payType).orderDesc(orderDesc).price(price).goodsName(goodsName).tenantId(tenantId).build();
            WxPayOrderRespVO result = partnerPayments.create(respVO,payment,openId);
            retainPartnerContext(orderNo);
            return result;
        }
        StorePayConfigDO storePay = storePayConfigMapper.getByStoreId(storeId);
        String orderType = "微信";
        PayOrderDO payOrderDO = new PayOrderDO();
        payOrderDO.setSplitOrder(false);
        if (!ObjectUtils.isEmpty(storePay)) {
            throw new RuntimeException("YeePay discontinued - WeChat partner mode pending");
        } else {
            String openId = socialUserApi.getUserOpenIdByType(getLoginUserId(), SocialTypeEnum.WECHAT_MINI_APP.getType());
            if (ObjectUtils.isEmpty(openId)) {
                throw exception(AUTH_USER_BIND_MINIAPP_ERROR);
            }
            //创建微信支付实例
            WxPayService wxPayService = myWxService.initWxPay(storeId);
            try {
                WxPayMpOrderResult wxPayMpOrderResult = myWxService.createOrder(payOrderDO, wxPayService, storeId, orderNo, price, openId);
                respVO.setPkg(wxPayMpOrderResult.getPackageValue());
                respVO.setAppId(wxPayMpOrderResult.getAppId());
                respVO.setNonceStr(wxPayMpOrderResult.getNonceStr());
                respVO.setPaySign(wxPayMpOrderResult.getPaySign());
                respVO.setSignType("MD5");
                respVO.setTimeStamp(wxPayMpOrderResult.getTimeStamp());
            } catch (Exception e) {
                log.error("创建支付订单异常", e);
//                throw new RuntimeException(e);
                throw exception(USER_WEIXIN_PAY_ERROR);
            }
        }
        payOrderDO.setGoodsName(goodsName);
        payOrderDO.setUserId(userId);
        payOrderDO.setOrderNo(orderNo);
        payOrderDO.setOrderId(orderId);
        payOrderDO.setStoreId(storeId);
        payOrderDO.setPayType(payType);
        payOrderDO.setOrderDesc(orderDesc);
        payOrderDO.setPrice(price);
        payOrderDO.setType(orderType);
        payOrderMapper.insert(payOrderDO);
        return respVO;
    }

    /**
     * 退款
     *
     * @param payOrderDO
     * @param refundPrice
     * @param desc
     */
    @Override
    public void refund(PayOrderDO payOrderDO, Integer refundPrice, String desc) {
        if (partnerPayments.hasAttempt(payOrderDO.getOrderNo())) {
            partnerPayments.refund(payOrderDO,refundPrice,desc);
            return;
        }
        String orderNo = payOrderDO.getOrderNo();
        Long tenantId = TenantContextHolder.getTenantId();
        if (ObjectUtils.isEmpty(tenantId) || tenantId.compareTo(0L) == 0) {
            //从redis中取出
            String redisKey = String.format(WX_PAY_ORDER, orderNo);
            WxPayOrderInfo wxPayOrderInfo = (WxPayOrderInfo) redisTemplate.opsForValue().get(redisKey);
            tenantId = wxPayOrderInfo.getTenantId();
        }

        TenantUtils.execute(tenantId, () -> {
            if (payOrderDO.getType().equals("易宝支付")) {
                throw new RuntimeException("YeePay discontinued");
            } else {
                WxPayService wxPayService = myWxService.initWxPay(payOrderDO.getStoreId());
                WxPayRefundRequest refundRequest = new WxPayRefundRequest();
                refundRequest.setOutTradeNo(payOrderDO.getOrderNo());
                refundRequest.setOutRefundNo("TK" + payOrderDO.getOrderNo());
                refundRequest.setTotalFee(payOrderDO.getPrice());
                refundRequest.setRefundFee(refundPrice);
                refundRequest.setRefundDesc(desc);
//            refundRequest.setNotifyUrl()
                try {
                    wxPayService.refundV2(refundRequest);

                } catch (WxPayException ex) {
                    throw exception(USER_WEIXIN_PAY_ERROR);
                }
            }
        });
        payOrderDO.setRefundTime(LocalDateTime.now());
        payOrderDO.setPayRefundNo("TK" + payOrderDO.getOrderNo());
        payOrderDO.setRefundPrice(payOrderDO.getRefundPrice() + refundPrice);
        payOrderMapper.updateById(payOrderDO);
    }

    @Override
    public PayOrderDO getByOrderNo(String orderNo) {
        return payOrderMapper.getByOrderNo(orderNo);
    }

    /**
     * 检查订单支付状态
     *
     * @param orderNo
     * @param storeId
     * @param price
     */
    @SneakyThrows
    public void checkWxOrder(String orderNo, Long storeId, Integer price) {
        if (partnerPayments.hasAttempt(orderNo)) {
            partnerPayments.checkOrder(orderNo,storeId,price);
            return;
        }
        log.info("检查门店:{}，订单：{}，支付状态！", storeId, orderNo);
        String redisKey = String.format(WX_PAY_ORDER, orderNo);
        if (redisTemplate.hasKey(redisKey)) {
            //再验证支付是否成功
            PayOrderDO payOrderDO = getByOrderNo(orderNo);
            if (ObjectUtils.isEmpty(payOrderDO)) {
                throw exception(ORDER_WEIXIN_PAY_ERROR);
            }
            if (payOrderDO.getType().equals("易宝支付")) {
                throw new RuntimeException("YeePay discontinued");
            } else {
                //创建微信支付实例
                WxPayService wxPayService = myWxService.initWxPay(storeId);
                WxPayOrderQueryResult wxPayOrderQueryResult = wxPayService.queryOrder(null, orderNo);
                //如果已经验证了 就移除这个订单号
                redisTemplate.delete(redisKey);
                String tradeNo = wxPayOrderQueryResult.getTransactionId();
                String tradeState = wxPayOrderQueryResult.getTradeState();
                String returnCode = wxPayOrderQueryResult.getReturnCode();
                Integer cashFee = wxPayOrderQueryResult.getTotalFee();//支付金额
                String resultCode = wxPayOrderQueryResult.getResultCode();
                log.info("tradeState:{},returnCode:{},resultCode:{},", tradeState, returnCode, resultCode);
                //判断支付结果
                boolean flag = tradeState.equals("SUCCESS") && returnCode.equals("SUCCESS") && resultCode.equals("SUCCESS");
                //对比实际支付的价格 和订单应支付的价格是否一致
                boolean checkPrice = cashFee >= price;
                log.info("订单：{}，微信支付状态为：{},price:{},cashFee:{}", orderNo, flag, price, cashFee);
                if (flag && checkPrice) {
                    //更新支付订单状态
                    if (!payOrderDO.getPayStatus()) {
                        //更新状态、支付订单号 和支付金额
                        payOrderDO.setPrice(cashFee);
                        payOrderDO.setPayOrderNo(tradeNo);
                        payOrderDO.setPayStatus(true);
                        payOrderDO.setPayTime(LocalDateTime.now());
                        payOrderMapper.updateById(payOrderDO);
                    }
                } else {
                    throw exception(ORDER_WEIXIN_PAY_ERROR);
                }
            }
        } else {
            throw exception(ORDER_WEIXIN_PAY_ERROR);
        }
    }


    /**
     * 押金退款
     *
     * @param orderNo 订单号
     * @param deposit 押金
     */
    @Override
    @Transactional
    public void refundDeposit(Long orderId, String orderNo, BigDecimal deposit, Long userId, Long storeId, boolean depositRefund) {
        int price = deposit.multiply(BigDecimal.valueOf(100.0)).intValue();
        //查询出支付订单
        PayOrderDO payOrderDO = payOrderMapper.getByOrderNo(orderNo);
        //查询出系统订单
        if (!ObjectUtils.isEmpty(payOrderDO)) {
            //已支付 并且 押金大于0  并且押金未退款 才退押金
            if (payOrderDO.getPayStatus() &&
                    deposit.compareTo(BigDecimal.ZERO) > 0
                    && !depositRefund) {
                //这里从第一笔开始退，因为第一笔支付肯定是大于押金的
                try {
                    refund(payOrderDO, price, "押金退款");
                } catch (Exception ex) {
//                throw new RuntimeException(ex);
                    if (partnerPayments.hasAttempt(orderNo)) throw exception(USER_WEIXIN_PAY_ERROR);
                    log.error("支付订单:{}，退款失败", orderNo, ex);
                }
                //退回押金产生的积分
                StoreUserDO storeUserDO = storeUserMapper.getByUserIdAndStoreId(userId, storeId);
                if (!ObjectUtils.isEmpty(storeUserDO) && !partnerPayments.hasAttempt(payOrderDO.getOrderNo())) {
                    //为空的时候不处理
                    storeUserDO.setTotalScore(storeUserDO.getTotalScore() - deposit.intValue());
                    if (storeUserDO.getTotalScore() < 0) {
                        storeUserDO.setTotalScore(0);
                    }
                    //暂不处理回滚VIP等级，因为不是很重要的功能  哪怕不回滚  也不是很重要的事  目的还是促进客户消费
                    storeUserMapper.updateById(storeUserDO);
                }
            } else {
                throw exception(ORDER_DEPOSIT_REFUND_ERROR);
            }
        } else {
            throw exception(ORDER_DEPOSIT_REFUND_ERROR);
        }
    }

    @Resource
    private StoreUserMapper storeUserMapper;

    @Resource
    private UserMoneyBillMapper userMoneyBillMapper;

    /**
     * 余额退款
     *
     * @param orderNo
     * @param userId
     */
    @Override
    public void refundBalance(Long storeId, String orderNo, Long userId) {
        StoreUserDO storeUserDO = storeUserMapper.getByUserIdAndStoreId(userId, storeId);
        //余额退款  把支付记录找出来
        List<UserMoneyBillDO> userMoneyBillDOList = userMoneyBillMapper.getPayByOrderNo(orderNo, userId);
        if (!CollectionUtils.isEmpty(userMoneyBillDOList)) {
            for (UserMoneyBillDO billDO : userMoneyBillDOList) {
                UserMoneyBillDO newUserMoneyBillDO = new UserMoneyBillDO();
                BeanUtils.copyProperties(billDO, newUserMoneyBillDO);
                newUserMoneyBillDO.setId(null);
                newUserMoneyBillDO.setCreateTime(null);
                newUserMoneyBillDO.setCreator(null);
                newUserMoneyBillDO.setUpdateTime(null);
                newUserMoneyBillDO.setUpdater(null);
                newUserMoneyBillDO.setType(AppEnum.user_money_bill_type.REFUND.getValue());//改成退款状态
                newUserMoneyBillDO.setRemark(newUserMoneyBillDO.getRemark().replace("支付", "退款"));
                if (billDO.getMoneyType().intValue() == 1) {
                    //账户余额  加回去
                    storeUserDO.setBalance(storeUserDO.getBalance().add(billDO.getMoney()));
                    synchronized (this) {
                        storeUserMapper.updateById(storeUserDO);
                    }
                    newUserMoneyBillDO.setTotalMoney(storeUserDO.getBalance());
                } else if (billDO.getMoneyType().intValue() == 2) {
                    //赠送余额  加回去
                    storeUserDO.setGiftBalance(storeUserDO.getGiftBalance().add(billDO.getMoney()));
                    synchronized (this) {
                        storeUserMapper.updateById(storeUserDO);
                    }
                    newUserMoneyBillDO.setTotalGiftMoney(storeUserDO.getGiftBalance());
                } else {
                    throw exception(OPRATION_ERROR);
                }
                userMoneyBillMapper.insert(newUserMoneyBillDO);
            }
        }

    }

    /**
     * @param orderId
     * @param orderNo
     * @param storeId
     */
    @Override
    public void refundByOrder(Long orderId, String orderNo, Long storeId) {
        List<PayOrderDO> payOrderList = payOrderMapper.getByOrder(orderId, orderNo);
        if (!CollectionUtils.isEmpty(payOrderList)) {
            //退款
            for (PayOrderDO payOrderDO : payOrderList) {
                try {
                    refund(payOrderDO, payOrderDO.getPrice(), "退款");
                    if (!partnerPayments.hasAttempt(payOrderDO.getOrderNo())) refundedScore(payOrderDO.getPrice(), payOrderDO.getUserId(), payOrderDO.getStoreId());
                } catch (Exception ex) {
//                throw new RuntimeException(ex);
                    if (partnerPayments.hasAttempt(payOrderDO.getOrderNo())) throw exception(USER_WEIXIN_PAY_ERROR);
                    log.error("支付订单:{}，退款失败", payOrderDO.getOrderNo(), ex);
                }
            }
        }
    }

    /**
     * 支付退款
     *
     * @param orderId
     * @param orderNo
     * @param storeId
     * @param price   退款的金额
     */
    @Override
    public void refundByOrder(Long orderId, String orderNo, Long storeId, int price) {
        //这里从最近的一笔开始退， 和退押金的反过来的，避免这里退了，押金就不够退
        List<PayOrderDO> payOrderList = payOrderMapper.getByOrder(orderId, orderNo);
        if (!CollectionUtils.isEmpty(payOrderList)) {
//            WxPayService wxPayService = myWxService.initWxPay(storeId);
            //退款
            int refundPrice = 0;//记录下已经退款的金额
            boolean finish = false;//退款是否已经结束
            for (PayOrderDO payOrderDO : payOrderList) {
                int theRefundPrice = 0; //本次退款的钱
                if (payOrderDO.getPrice() >= price) {
                    //够退完
                    finish = true;
                    theRefundPrice = price;
                } else {
                    //不够退完 那么先把这笔全退 然后再继续退
                    theRefundPrice = payOrderDO.getPrice();
                }
                refundPrice += theRefundPrice;//累加已退款金额
                try {
                    refund(payOrderDO, theRefundPrice, "退款");
                    if (!partnerPayments.hasAttempt(payOrderDO.getOrderNo())) refundedScore(theRefundPrice, payOrderDO.getUserId(), payOrderDO.getStoreId());
                    if (finish) {
                        break;
                    }
                } catch (Exception ex) {
//                throw new RuntimeException(ex);
                    if (partnerPayments.hasAttempt(payOrderDO.getOrderNo())) throw exception(USER_WEIXIN_PAY_ERROR);
                    log.error("支付订单:{}，退款失败", payOrderDO.getOrderNo(), ex);
                }
            }
        }


    }

    @Override
    @Transactional
    public void update(PayOrderUpdateReqVO reqVO) {
        log.info("管理员：{}，修改订单：{}", getLoginUserId(), reqVO);
        PayOrderDO payOrderDO = payOrderMapper.selectById(reqVO.getId());
        if (!ObjectUtils.isEmpty(payOrderDO)) {
            if (reqVO.getPrice() != null) {
                payOrderDO.setPrice(reqVO.getPrice());
            }
            payOrderMapper.updateById(payOrderDO);
        }
    }

    @Override
    public String updateYeepayOrder(JSONObject data) {
        log.info("收到易宝支付回调body：{}", data);
        //签名检查
        String iotSign = data.getString("iotSign");
        String iotTimestamp = data.getString("iotTimestamp");
        String yeepayCallType = data.getString("yeepayCallType");
        String orderId = data.getString("orderId");
        String newSign = SecureUtil.md5(secret() + iotTimestamp);
        if (newSign.equals(iotSign)) {
            PayOrderDO payOrderDO = payOrderMapper.getByOrderNo(orderId);
            if (!ObjectUtils.isEmpty(payOrderDO)) {
                if (yeepayCallType.equals("yeepayPay")) {
                    //支付成功
                    // 加入自己处理订单的业务逻辑，需要判断订单是否已经支付过，否则可能会重复调用
                    String payOrderNo = data.getString("uniqueOrderNo");
                    String cashFee = data.getString("payAmount");//支付金额
                    int price = new BigDecimal(cashFee).multiply(new BigDecimal(100)).intValue();
                    String status = data.getString("status");
                    if (!payOrderDO.getPayStatus() && status.equals("SUCCESS")) {
                        //更新状态、支付订单号 和支付金额
                        payOrderDO.setPrice(price);
                        payOrderDO.setPayOrderNo(payOrderNo);
                        payOrderDO.setPayStatus(true);
                        payOrderDO.setPayTime(LocalDateTime.now());
                        payOrderMapper.updateById(payOrderDO);
                        try {
                            payCallback(payOrderDO);
                            return "SUCCESS";
                        } catch (ServiceException e) {
                            log.error("微信回调处理异常", e);
                            refund(payOrderDO, payOrderDO.getPrice(), "支付失败退款");
                            return "SUCCESS";
                        } catch (Exception e) {
                            log.error("易宝回调结果异常", e);
                            return "易宝回调结果异常";
                        }
                    } else {
                        //退款
//                        refund(payOrderDO, payOrderDO.getPrice(), "支付失败退款");
                        return "SUCCESS";
                    }

                } else if (yeepayCallType.equals("yeepaySplit")) {
                    //分账
                    payOrderMapper.updateById(new PayOrderDO().setOrderId(payOrderDO.getId()).setSplitStatus(true));
                } else if (yeepayCallType.equals("yeepayRefund")) {
                    //退款  通知下退款失败
                    String refundAmount = data.getString("refundAmount");
                    String errorMessage = data.getString("errorMessage");
                    workWxService.sendRefundError(payOrderDO.getStoreId(), payOrderDO.getOrderNo(), refundAmount, errorMessage);
                }
            }
        } else {
            return "签名不匹配";
        }
        return "SUCCESS";
    }


}
