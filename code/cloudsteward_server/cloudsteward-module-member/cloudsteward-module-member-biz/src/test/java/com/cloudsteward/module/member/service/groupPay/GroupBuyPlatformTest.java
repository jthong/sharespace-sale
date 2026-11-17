package com.cloudsteward.module.member.service.groupPay;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cloudsteward.framework.common.exception.ServiceException;
import com.cloudsteward.module.member.controller.app.manager.vo.AppUseGroupNoReqVO;
import com.cloudsteward.module.member.controller.app.order.vo.*;
import com.cloudsteward.module.member.service.manager.AppMangerServiceImpl;
import com.cloudsteward.module.member.service.order.AppOrderServiceImpl;
import com.cloudsteward.module.member.service.iot.IotGroupPayService;
import com.cloudsteward.module.member.service.iot.device.IspResult;
import com.cloudsteward.module.member.service.iot.groupPay.*;
import com.cloudsteward.module.member.forest.IotGroupPayClient;
import com.cloudsteward.module.member.service.wx.WorkWxService;
import com.cloudsteward.module.member.dal.dataobject.groupPay.GroupPayInfoDO;
import com.cloudsteward.module.member.dal.dataobject.pkginfo.PkgInfoDO;
import com.cloudsteward.module.member.dal.mysql.groupPay.GroupPayInfoMapper;
import com.cloudsteward.module.member.dal.mysql.pkginfo.PkgInfoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class GroupBuyPlatformTest {
 static final String[] CODES={"1234567890","1234567890123","123456789012345","https://example.test/coupon?code=abc&other=def"};
 static int platform(String code){return code.length()==10||code.length()==13?1:2;}
 @Test void rulesAndPrefixPriority() {
  for(String code:CODES)assertEquals(platform(code),GroupBuyPlatform.detect(code));
  assertEquals(2,GroupBuyPlatform.detect("https12345"));assertEquals(2,GroupBuyPlatform.detect("HTTPS://example.test/test"));
  assertEquals(1,GroupBuyPlatform.detect(" 12345 67890 "));assertEquals(CODES[3],GroupBuyPlatform.normalize("  "+CODES[3]+"  "));
 }
 @Test void unknownCodesNeverReachVendor() {
  IotGroupPayService iot=new IotGroupPayService();IotGroupPayClient client=mock(IotGroupPayClient.class);ReflectionTestUtils.setField(iot,"iotGroupPayClient",client);
  for(String code:new String[]{null,"","12345678","12345678901","12345678901234","1234567890123456"}) {
   assertThrows(com.cloudsteward.framework.common.exception.ServiceException.class,()->GroupBuyPlatform.detect(code));
   assertThrows(com.cloudsteward.framework.common.exception.ServiceException.class,()->iot.prepare(new IotGroupPayPrepareReqVO().setCode(code)));
   assertThrows(com.cloudsteward.framework.common.exception.ServiceException.class,()->iot.consume(new IotGroupPayConsumeReqVO().setCode(code)));
  }
  verifyNoInteractions(client);OrderSaveReqVO req=new OrderSaveReqVO();req.setGroupPayNo("bad");assertThrows(com.cloudsteward.framework.common.exception.ServiceException.class,()->new AppOrderServiceImpl().save(req));
 }
 @Test void manualRedemptionInfersPlatformWithUnchangedClientRequest() {
  for(String code:CODES) {
   int p=platform(code);AppMangerServiceImpl manager=new AppMangerServiceImpl();GroupPayInfoService group=mock(GroupPayInfoService.class);
   ReflectionTestUtils.setField(manager,"groupPayInfoService",group);ReflectionTestUtils.setField(manager,"workWxService",mock(WorkWxService.class));
   AppUseGroupNoReqVO req=new AppUseGroupNoReqVO();req.setStoreId(18L);req.setGroupPayNo(code);
   IotGroupPayPrepareRespVO prepared=new IotGroupPayPrepareRespVO();when(group.prepare(18L,p,code)).thenReturn(prepared);when(group.consume(18L,p,code,prepared)).thenReturn(new GroupPayInfoDO());manager.useGroupNo(req);
   org.mockito.InOrder order=inOrder(group);order.verify(group).prepare(18L,p,code);order.verify(group).consume(18L,p,code,prepared);
   reset(group);when(group.prepare(18L,p,code)).thenThrow(new IllegalArgumentException("invalid"));assertThrows(IllegalArgumentException.class,()->manager.useGroupNo(req));verify(group,never()).consume(anyLong(),anyInt(),anyString(),any());
  }
 }
 @Test void precheckAndPackageSelectionUseInferredPlatform() {
  for(String code:CODES) {
   int p=platform(code);AppOrderServiceImpl orders=new AppOrderServiceImpl();GroupPayInfoService group=mock(GroupPayInfoService.class);PkgInfoMapper packages=mock(PkgInfoMapper.class);
   ReflectionTestUtils.setField(orders,"groupPayInfoService",group);ReflectionTestUtils.setField(orders,"pkgInfoMapper",packages);
   IotGroupPayPrepareRespVO prepared=new IotGroupPayPrepareRespVO();prepared.setShopId("same-product-id");prepared.setTicketName("test");PkgInfoDO pkg=new PkgInfoDO();pkg.setHours(2);
   when(group.prepare(18L,p,code)).thenReturn(prepared);when(packages.getPkgByShopId("same-product-id",18L,p)).thenReturn(pkg);
   PreGroupNoReqVO req=new PreGroupNoReqVO();req.setStoreId(18L);req.setCode(code);assertEquals(2,orders.preGroupNo(req).getHours());verify(group).prepare(18L,p,code);verify(packages).getPkgByShopId("same-product-id",18L,p);
  }
 }
 @Test void serviceDoesNotTrustCallerPlatformAndStoresInferredPlatform() {
  for(String code:CODES) {
   int p=platform(code);GroupPayInfoServiceImpl group=new GroupPayInfoServiceImpl();GroupBuyStoreBindingService bindings=mock(GroupBuyStoreBindingService.class);IotGroupPayService iot=mock(IotGroupPayService.class);
   when(bindings.requireShopId(18L)).thenReturn("10000000000001");ReflectionTestUtils.setField(group,"groupBuyBindings",bindings);ReflectionTestUtils.setField(group,"iotGroupPayService",iot);ReflectionTestUtils.setField(group,"groupPayInfoMapper",mock(GroupPayInfoMapper.class));
   IotGroupPayPrepareRespVO ticket=new IotGroupPayPrepareRespVO();ticket.setPayAmount(100);ticket.setTicketInfo("opaque-ticket");group.prepare(18L,3-p,code);GroupPayInfoDO record=group.consume(18L,3-p,code,ticket);group.revoke(18L,record.getGroupPayType(),code,record.getTicketInfo());
   verify(iot).prepare(argThat(r->r.getPlatform()==p&&code.equals(r.getCode())));verify(iot).consume(argThat(r->r.getPlatform()==p&&code.equals(r.getCode())));verify(iot).revoke(argThat(r->r.getPlatform()==p));assertEquals(p,record.getGroupPayType());
  }
 }
 @Test void externalPrepareConsumeAndConsumeWithResultInferBeforeSending() {
  for(String code:CODES) {
   int p=platform(code);IotGroupPayService service=spy(new IotGroupPayService());IotGroupPayClient client=mock(IotGroupPayClient.class);ReflectionTestUtils.setField(service,"iotGroupPayClient",client);doReturn("client").when(service).clientId();doReturn("secret").when(service).secret();
   IspResult<Object> pre=new IspResult<>();pre.setCode("SUCCESS");pre.setResult(JSON.parseObject("{\"ticketName\":\"2小时套餐\",\"payAmount\":100}"));IspResult<String> consumed=new IspResult<>();consumed.setCode("SUCCESS");
   when(client.prepare(any(),anyString(),anyString())).thenReturn(pre);when(client.consume(any(),anyString(),anyString())).thenReturn(consumed);when(client.consumeWithResult(any(),anyString(),anyString())).thenReturn(consumed);
   service.prepare(new IotGroupPayPrepareReqVO().setCode(code).setPlatform(3-p));service.consume(new IotGroupPayConsumeReqVO().setCode(code).setPlatform(3-p));service.consumeWithResult(new IotGroupPayConsumeReqVO().setCode(code).setPlatform(3-p));
   verify(client).prepare(argThat(r->r.getPlatform()==p&&code.equals(r.getCode())),anyString(),anyString());verify(client).consume(argThat(r->r.getPlatform()==p&&code.equals(r.getCode())),anyString(),anyString());verify(client).consumeWithResult(argThat(r->r.getPlatform()==p&&code.equals(r.getCode())),anyString(),anyString());
  }
 }
 @Test void prepareDecodesTheJsonStringReturnedByTheVendor() {
  IotGroupPayClient client = mock(IotGroupPayClient.class);
  IotGroupPayService service = spy(new IotGroupPayService());
  ReflectionTestUtils.setField(service, "iotGroupPayClient", client);
  doReturn("client").when(service).clientId();
  doReturn("secret").when(service).secret();

  String ticketInfo = "{\"token\":\"example\"}";
  JSONObject ticket = new JSONObject();
  ticket.put("ticketName", "1小时套餐");
  ticket.put("payAmount", 1600);
  ticket.put("shopId", "product");
  ticket.put("ticketInfo", ticketInfo);
  String response = "{\"code\":\"SUCCESS\",\"result\":" + JSON.toJSONString(ticket.toJSONString()) + "}";
  IspResult<Object> envelope = JSON.parseObject(response, new TypeReference<IspResult<Object>>() {});
  when(client.prepare(any(), eq("client"), eq("secret"))).thenReturn(envelope);

  IotGroupPayPrepareRespVO decoded = service.prepare(new IotGroupPayPrepareReqVO().setCode(CODES[1]));
  assertEquals("1小时套餐", decoded.getTicketName());
  assertEquals(1600, decoded.getPayAmount());
  assertEquals("product", decoded.getShopId());
  assertEquals(ticketInfo, decoded.getTicketInfo());

  envelope.setResult("not JSON");
  assertThrows(ServiceException.class, () -> service.prepare(new IotGroupPayPrepareReqVO().setCode(CODES[1])));
  envelope.setCode("FAIL");
  envelope.setMsg("券已使用");
  assertTrue(assertThrows(ServiceException.class,
          () -> service.prepare(new IotGroupPayPrepareReqVO().setCode(CODES[1]))).getMessage().contains("券已使用"));
 }
 @Test void oldMiniappDtosDoNotRequirePlatform() {
  javax.validation.Validator validator=javax.validation.Validation.buildDefaultValidatorFactory().getValidator();
  AppUseGroupNoReqVO manual=new AppUseGroupNoReqVO();manual.setStoreId(18L);manual.setGroupPayNo(CODES[0]);assertTrue(validator.validate(manual).isEmpty());
  PreGroupNoReqVO pre=new PreGroupNoReqVO();pre.setStoreId(18L);pre.setCode(CODES[2]);assertTrue(validator.validate(pre).isEmpty());
 }
}
