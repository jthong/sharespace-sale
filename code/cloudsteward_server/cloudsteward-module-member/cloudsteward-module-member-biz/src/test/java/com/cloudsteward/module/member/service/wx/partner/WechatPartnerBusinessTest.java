package com.cloudsteward.module.member.service.wx.partner;
import com.cloudsteward.module.member.service.payorder.*;
import com.cloudsteward.module.member.service.order.*;
import com.cloudsteward.module.member.service.user.*;
import com.cloudsteward.module.member.service.pkg.*;
import com.cloudsteward.module.member.service.productorder.*;
import com.cloudsteward.module.member.controller.app.order.vo.*;
import com.cloudsteward.module.member.controller.app.user.vo.*;
import com.cloudsteward.module.member.controller.app.pkg.vo.*;
import com.cloudsteward.module.member.dal.mysql.storeuser.*;
import com.cloudsteward.module.member.dal.mysql.storevipconfig.*;
import com.cloudsteward.module.member.dal.mysql.orderinfo.*;
import com.cloudsteward.module.member.dal.dataobject.storeuser.*;
import com.cloudsteward.module.member.dal.dataobject.payorder.*;
import com.cloudsteward.module.member.enums.AppWxPayTypeEnum;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.redis.core.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class WechatPartnerBusinessTest {
 PayOrderServiceImpl service;AppOrderService orders;AppUserService users;PkgService packages;ProductOrderService products;WechatPartnerPaymentService gateway;StoreUserMapper storeUsers;
 @BeforeEach void setup(){
  service=new PayOrderServiceImpl();orders=mock(AppOrderService.class);users=mock(AppUserService.class);packages=mock(PkgService.class);products=mock(ProductOrderService.class);gateway=mock(WechatPartnerPaymentService.class);storeUsers=mock(StoreUserMapper.class);
  ReflectionTestUtils.setField(service,"appOrderService",orders);ReflectionTestUtils.setField(service,"appUserService",users);ReflectionTestUtils.setField(service,"pkgService",packages);ReflectionTestUtils.setField(service,"productOrderService",products);ReflectionTestUtils.setField(service,"partnerPayments",gateway);ReflectionTestUtils.setField(service,"storeUserMapper",storeUsers);ReflectionTestUtils.setField(service,"storeVipConfigMapper",mock(StoreVipConfigMapper.class));
  when(storeUsers.getByUserIdAndStoreId(445L,18L)).thenReturn(new StoreUserDO().setTotalScore(0).setVipLevel((byte)0));
  RedisTemplate redis=mock(RedisTemplate.class);when(redis.opsForValue()).thenReturn(mock(ValueOperations.class));ReflectionTestUtils.setField(service,"redisTemplate",redis);
 }
 PayOrderDO payment(String order,int type){return PayOrderDO.builder().orderNo(order).storeId(18L).userId(445L).price(1000).payType(type).build();}
 WxPayOrderInfo context(AppWxPayTypeEnum type,String order){WxPayOrderInfo info=new WxPayOrderInfo(type,order,445L,152L,18L,1000);info.setNightLong(false);info.setPreSubmit(false);return info;}
 @Test void rechargePackageAndProductUseTheExistingBusinessServices(){
  service.applyPartnerPayment(payment("recharge",1),context(AppWxPayTypeEnum.RECHARGE,"recharge"));verify(users).rechargeBalance(any(AppRechargeBalanceReqVO.class));
  service.applyPartnerPayment(payment("package",1),context(AppWxPayTypeEnum.PKG,"package"));verify(packages).buyPkg(any(AppBuyPkgReqVO.class));
  service.applyPartnerPayment(payment("SH-product",1),new WxPayOrderInfo().setTenantId(152L));verify(products).paymentOrder(any(PayOrderDO.class));
 }
 @Test void fullWechatOrderIsCreatedButWalletAndGroupBuyDepositsWaitForTheirOriginalOrderFlow(){
  service.applyPartnerPayment(payment("full",1),context(AppWxPayTypeEnum.ORDER,"full"));verify(orders).save(any(OrderSaveReqVO.class));clearInvocations(orders);
  service.applyPartnerPayment(payment("wallet",2),context(AppWxPayTypeEnum.ORDER,"wallet"));service.applyPartnerPayment(payment("voucher",3),context(AppWxPayTypeEnum.ORDER,"voucher"));verifyNoInteractions(orders);
 }
 @Test void renewalPreservesTheOriginalOrderReference(){
  WxPayOrderInfo info=context(AppWxPayTypeEnum.RENEW,"renew");info.setIgnoreOrderId(77L);service.applyPartnerPayment(payment("renew",1),info);verify(orders).renew(argThat(req->Long.valueOf(77).equals(req.getOrderId())&&"renew".equals(req.getOrderNo())));
 }
 @Test void confirmedFrontendRequestsReturnWithoutRepeatingBusinessMutations(){
  PayOrderService pay=mock(PayOrderService.class);when(pay.completePartnerBusiness(anyString(),any())).thenReturn(true);
  AppUserServiceImpl user=new AppUserServiceImpl();ReflectionTestUtils.setField(user,"payOrderService",pay);AppRechargeBalanceReqVO recharge=new AppRechargeBalanceReqVO();recharge.setOrderNo("recharge");user.rechargeBalance(recharge);
  PkgServiceImpl pkg=new PkgServiceImpl();ReflectionTestUtils.setField(pkg,"payOrderService",pay);AppBuyPkgReqVO buy=new AppBuyPkgReqVO();buy.setOrderNo("package");pkg.buyPkg(buy);
  AppOrderServiceImpl order=new AppOrderServiceImpl();ReflectionTestUtils.setField(order,"payOrderService",pay);ReflectionTestUtils.setField(order,"orderInfoMapper",mock(OrderInfoMapper.class));OrderSaveReqVO save=new OrderSaveReqVO();save.setOrderNo("order");order.save(save);OrderRenewalReqVO renew=new OrderRenewalReqVO();renew.setOrderNo("renew");order.renew(renew);
  verify(pay,times(4)).completePartnerBusiness(anyString(),any());
 }
}
