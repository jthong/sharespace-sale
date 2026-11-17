package com.cloudsteward.module.member.service.wx.partner;
import com.alibaba.fastjson.JSONObject;
import com.cloudsteward.module.member.controller.app.callback.WechatPartnerCallbackController;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class WechatPartnerCallbackTest {
 @Test void invalidSignatureCannotReachPaymentBusiness(){
  WechatPartnerCallbackController controller=new WechatPartnerCallbackController();WechatPartnerClient client=mock(WechatPartnerClient.class);WechatPartnerPaymentService service=mock(WechatPartnerPaymentService.class);ReflectionTestUtils.setField(controller,"client",client);ReflectionTestUtils.setField(controller,"payments",service);
  when(client.decodeNotification(anyString(),any(),any(),any(),any())).thenThrow(new IllegalArgumentException("bad signature"));assertEquals(400,controller.payment("{}",new MockHttpServletRequest()).getStatusCodeValue());verifyNoInteractions(service);
 }
 @Test void verifiedSuccessReturns204ButBusinessFailureReturns500(){
  WechatPartnerCallbackController controller=new WechatPartnerCallbackController();WechatPartnerClient client=mock(WechatPartnerClient.class);WechatPartnerPaymentService service=mock(WechatPartnerPaymentService.class);ReflectionTestUtils.setField(controller,"client",client);ReflectionTestUtils.setField(controller,"payments",service);
  JSONObject event=new JSONObject();event.put("event_type","TRANSACTION.SUCCESS");event.put("resource",new JSONObject());when(client.decodeNotification(anyString(),any(),any(),any(),any())).thenReturn(event);
  assertEquals(204,controller.payment("{}",new MockHttpServletRequest()).getStatusCodeValue());doThrow(new IllegalStateException("db failure")).when(service).settle(any());assertEquals(500,controller.payment("{}",new MockHttpServletRequest()).getStatusCodeValue());
 }
 @Test void nonSuccessPaymentEventIsNotAcknowledgedAsSuccess(){
  WechatPartnerCallbackController controller=new WechatPartnerCallbackController();WechatPartnerClient client=mock(WechatPartnerClient.class);WechatPartnerPaymentService service=mock(WechatPartnerPaymentService.class);ReflectionTestUtils.setField(controller,"client",client);ReflectionTestUtils.setField(controller,"payments",service);
  JSONObject event=new JSONObject();event.put("event_type","TRANSACTION.CLOSED");when(client.decodeNotification(anyString(),any(),any(),any(),any())).thenReturn(event);assertEquals(400,controller.payment("{}",new MockHttpServletRequest()).getStatusCodeValue());verifyNoInteractions(service);
 }
 @Test void refundCallbackMustBelongToThisServiceProvider(){
  WechatPartnerCallbackController controller=new WechatPartnerCallbackController();WechatPartnerClient client=mock(WechatPartnerClient.class);WechatPartnerPaymentService service=mock(WechatPartnerPaymentService.class);WechatPartnerProperties config=new WechatPartnerProperties();config.setSpMchId("1104993367");ReflectionTestUtils.setField(controller,"client",client);ReflectionTestUtils.setField(controller,"payments",service);ReflectionTestUtils.setField(controller,"properties",config);
  JSONObject event=new JSONObject(),resource=new JSONObject();resource.put("mchid","other");event.put("event_type","REFUND.SUCCESS");event.put("resource",resource);when(client.decodeNotification(anyString(),any(),any(),any(),any())).thenReturn(event);
  assertEquals(400,controller.refund("{}",new MockHttpServletRequest()).getStatusCodeValue());verifyNoInteractions(service);resource.put("mchid","1104993367");assertEquals(204,controller.refund("{}",new MockHttpServletRequest()).getStatusCodeValue());
 }
}
