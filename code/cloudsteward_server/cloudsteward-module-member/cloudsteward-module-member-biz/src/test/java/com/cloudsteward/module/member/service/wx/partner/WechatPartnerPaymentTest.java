package com.cloudsteward.module.member.service.wx.partner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloudsteward.module.member.controller.app.order.vo.*;
import com.cloudsteward.module.member.dal.dataobject.payorder.PayOrderDO;
import com.cloudsteward.module.member.service.payorder.PayOrderService;
import com.cloudsteward.framework.security.core.LoginUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.cloudsteward.module.member.enums.AppWxPayTypeEnum;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WechatPartnerPaymentTest {
 @Test void descriptionsAreLimitedByUtf8BytesWithoutBreakingCharacters(){
  assertEquals("中",WechatPartnerPaymentService.utf8Limit("中文",4));
  assertEquals("中😀",WechatPartnerPaymentService.utf8Limit("中😀文",7));
  assertEquals("abc",WechatPartnerPaymentService.utf8Limit("abcd",3));
 }

    JdbcTemplate jdbc;WechatPartnerPaymentService service;WechatPartnerClient client;StorePartnerMerchantService merchants;PayOrderService pay;WechatPartnerProperties config;
    @BeforeEach void setup() throws Exception {
        DriverManagerDataSource ds=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1","sa","");jdbc=new JdbcTemplate(ds);
        Path directory=Paths.get("").toAbsolutePath(),migration=null;
        while(directory!=null&&migration==null) {
            for(String relative:new String[]{"sql/mysql/20261110_wechat_partner.sql","code/cloudsteward_server/sql/mysql/20261110_wechat_partner.sql"})if(Files.isRegularFile(directory.resolve(relative))){migration=directory.resolve(relative);break;}
            directory=directory.getParent();
        }
        assertNotNull(migration,"Cannot find the partner payment migration");
        String script=new String(Files.readAllBytes(migration),java.nio.charset.StandardCharsets.UTF_8);
        for(String sql:script.replaceAll("(?m)--.*$","").split(";"))if(!sql.trim().isEmpty())jdbc.execute(sql);
        jdbc.execute("CREATE TABLE member_pay_order(id bigint auto_increment primary key,user_id bigint,store_id bigint,order_no varchar(64),order_id bigint,pay_type int,order_desc varchar(255),price int,pay_status int,split_order int,split_status int,refund_price int,goods_name varchar(255),type varchar(64),tenant_id bigint,deleted int default 0,pay_order_no varchar(64),pay_time timestamp,pay_refund_no varchar(64),refund_time timestamp)");
        jdbc.execute("CREATE TABLE business_credit(order_no varchar(64) primary key)");jdbc.execute("CREATE TABLE refunded_score(order_no varchar(64),amount int)");
        service=new WechatPartnerPaymentService();client=mock(WechatPartnerClient.class);merchants=mock(StorePartnerMerchantService.class);pay=mock(PayOrderService.class);config=new WechatPartnerProperties();
        config.setEnabled(true);config.setSpAppId("sp-app");config.setSpMchId("1104993367");config.setMiniAppId("mini-app");config.setNotifyUrl("https://example.test/notify");config.setRefundNotifyUrl("https://example.test/refund");
        ReflectionTestUtils.setField(service,"jdbc",jdbc);ReflectionTestUtils.setField(service,"transactionManager",new DataSourceTransactionManager(ds));ReflectionTestUtils.setField(service,"properties",config);
        ReflectionTestUtils.setField(service,"client",client);ReflectionTestUtils.setField(service,"merchants",merchants);ReflectionTestUtils.setField(service,"payOrders",pay);
        when(merchants.require(18L)).thenReturn("1109667212");when(client.signMiniapp(anyString(),anyString(),anyString(),anyString())).thenReturn("rsa-signature");
        when(client.request(eq("POST"),eq("/v3/pay/partner/transactions/jsapi"),any())).thenAnswer(call->JSON.parseObject("{\"prepay_id\":\"prepay-test\"}"));
        when(pay.getByOrderNo(anyString())).thenAnswer(call->readPayment(call.getArgument(0)));
        doAnswer(call->{PayOrderDO p=call.getArgument(0);jdbc.update("INSERT INTO business_credit(order_no) VALUES(?)",p.getOrderNo());return null;}).when(pay).applyPartnerPayment(any(),any());
        doAnswer(call->{PayOrderDO p=call.getArgument(0);jdbc.update("INSERT INTO refunded_score(order_no,amount) VALUES(?,?)",p.getOrderNo(),call.getArgument(1));return null;}).when(pay).applyPartnerRefund(any(),anyInt());
    }
    PayOrderDO request(String no){return PayOrderDO.builder().orderNo(no).storeId(18L).userId(445L).tenantId(152L).price(1000).payType(1).goodsName("test").orderDesc("test").build();}
    PayOrderDO readPayment(String no){Map<String,Object> row=jdbc.queryForMap("SELECT * FROM member_pay_order WHERE order_no=?",no);PayOrderDO p=request(no);p.setType(String.valueOf(row.get("type")));p.setRefundPrice(((Number)row.get("refund_price")).intValue());return p;}
    Map<String,Object> attempt(String no){return jdbc.queryForMap("SELECT * FROM member_wechat_partner_payment WHERE order_no=?",no);}
    void prepare(String no){service.create(new WxPayOrderRespVO(),request(no),"payer");service.retainContext(no,new WxPayOrderInfo(AppWxPayTypeEnum.ORDER,no,445L,152L,18L,1000));}
    JSONObject success(String no){Map<String,Object> row=attempt(no);JSONObject data=new JSONObject();for(String k:new String[]{"sp_mchid","sp_appid","sub_mchid","out_trade_no"})data.put(k,row.get(k));data.put("sub_appid","mini-app");data.put("trade_state","SUCCESS");data.put("transaction_id","txn-"+no);data.put("amount",JSON.parseObject("{\"total\":1000,\"currency\":\"CNY\"}"));data.put("payer",JSON.parseObject("{\"sub_openid\":\"payer\"}"));return data;}
    int credits(){return jdbc.queryForObject("SELECT COUNT(*) FROM business_credit",Integer.class);}

    @Test void prepayUsesPartnerIdentityAndKeepsMiniappContract() {
        WxPayOrderRespVO result=service.create(new WxPayOrderRespVO(),request("order"),"payer");assertEquals("RSA",result.getSignType());assertEquals("prepay_id=prepay-test",result.getPkg());assertEquals("mini-app",result.getAppId());
        verify(client).request(eq("POST"),eq("/v3/pay/partner/transactions/jsapi"),argThat(body->"1104993367".equals(body.getString("sp_mchid"))&&"1109667212".equals(body.getString("sub_mchid"))&&"mini-app".equals(body.getString("sub_appid"))&&"payer".equals(body.getJSONObject("payer").getString("sub_openid"))&&body.getJSONObject("amount").getIntValue("total")==1000));
        assertEquals(32,String.valueOf(attempt("order").get("out_trade_no")).length());
    }
    @Test void timeoutPreservesAttemptAndRetriesSameWechatOrder() {
        when(client.request(eq("POST"),anyString(),any())).thenThrow(new IllegalStateException("network timeout")).thenReturn(JSON.parseObject("{\"prepay_id\":\"retried\"}"));
        assertThrows(IllegalStateException.class,()->service.create(new WxPayOrderRespVO(),request("order"),"payer"));String remote=String.valueOf(attempt("order").get("out_trade_no"));
        service.create(new WxPayOrderRespVO(),request("order"),"payer");assertEquals(remote,attempt("order").get("out_trade_no"));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM member_pay_order",Integer.class));
    }
    @Test void cannotChangeAmountPayerOrRouteExistingOrdinaryOrder() {
        prepare("order");PayOrderDO altered=request("order");altered.setPrice(900);assertThrows(RuntimeException.class,()->service.create(new WxPayOrderRespVO(),altered,"payer"));
        assertThrows(RuntimeException.class,()->service.create(new WxPayOrderRespVO(),request("order"),"different-payer"));
        jdbc.update("INSERT INTO member_pay_order(order_no,price,type) VALUES('legacy',1000,'微信')");assertThrows(RuntimeException.class,()->service.create(new WxPayOrderRespVO(),request("legacy"),"payer"));assertFalse(service.hasAttempt("legacy"));
    }
    @Test void changedStoreMerchantDoesNotChangePendingPayment() {
        prepare("order");when(merchants.require(18L)).thenReturn("new-merchant");service.create(new WxPayOrderRespVO(),request("order"),"payer");assertEquals("1109667212",attempt("order").get("sub_mchid"));verify(client,times(1)).request(eq("POST"),anyString(),any());
    }
    @Test void repeatedAndConcurrentSuccessCreditsBusinessOnce() throws Exception {
        prepare("order");JSONObject data=success("order");ExecutorService executor=Executors.newFixedThreadPool(2);
        try {Future<?> a=executor.submit(()->service.settle(data));Future<?> b=executor.submit(()->service.settle(data));a.get(15,TimeUnit.SECONDS);b.get(15,TimeUnit.SECONDS);} finally {executor.shutdownNow();}
        service.settle(data);assertEquals(1,credits());assertEquals("PAID",attempt("order").get("state"));verify(pay,times(1)).applyPartnerPayment(any(),any());
    }
    @Test void wrongAmountMerchantAppOrPayerCannotCredit() {
        prepare("order");for(String field:new String[]{"sp_mchid","sp_appid","sub_mchid","sub_appid","out_trade_no"}){JSONObject data=success("order");data.put(field,"wrong");assertThrows(RuntimeException.class,()->service.settle(data));}
        JSONObject amount=success("order");amount.getJSONObject("amount").put("total",999);assertThrows(RuntimeException.class,()->service.settle(amount));
        JSONObject currency=success("order");currency.getJSONObject("amount").put("currency","USD");assertThrows(RuntimeException.class,()->service.settle(currency));
        JSONObject payer=success("order");payer.getJSONObject("payer").put("sub_openid","wrong");assertThrows(RuntimeException.class,()->service.settle(payer));assertEquals(0,credits());
    }
    @Test void businessFailureRollsBackPaymentAndCanRetry() {
        prepare("order");doAnswer(call->{jdbc.update("INSERT INTO business_credit VALUES('order')");throw new IllegalStateException("business failed");}).when(pay).applyPartnerPayment(any(),any());
        assertThrows(IllegalStateException.class,()->service.settle(success("order")));assertEquals(0,credits());assertEquals("PENDING",attempt("order").get("state"));assertEquals(0,jdbc.queryForObject("SELECT pay_status FROM member_pay_order",Integer.class));
        doAnswer(call->{jdbc.update("INSERT INTO business_credit VALUES('order')");return null;}).when(pay).applyPartnerPayment(any(),any());service.settle(success("order"));assertEquals(1,credits());
    }
    @Test void missingContextIsNotAcknowledgedAsPaid() {
        service.create(new WxPayOrderRespVO(),request("order"),"payer");assertThrows(RuntimeException.class,()->service.settle(success("order")));assertEquals("PENDING",attempt("order").get("state"));
    }
    @Test void contextCannotBeSubstitutedAcrossUsersOrStores() {
        prepare("order");assertThrows(RuntimeException.class,()->service.retainContext("order",new WxPayOrderInfo(AppWxPayTypeEnum.RECHARGE,"order",446L,152L,18L,1000)));
        assertThrows(RuntimeException.class,()->service.retainContext("order",new WxPayOrderInfo(AppWxPayTypeEnum.RECHARGE,"order",445L,152L,19L,1000)));
    }
    @Test void businessCanVerifyItsOwnPaymentWithoutRecursingOrCallingWechatAgain() {
        prepare("order");
        doAnswer(call->{service.checkOrder("order",18L,1000);assertFalse(service.completeBusiness("order",AppWxPayTypeEnum.ORDER));jdbc.update("INSERT INTO business_credit VALUES('order')");return null;}).when(pay).applyPartnerPayment(any(),any());
        service.settle(success("order"));service.checkOrder("order",18L,1000);assertEquals(1,credits());verify(client,never()).request(eq("GET"),anyString(),any());
    }
    @Test void frontendConfirmationAndRepeatedCallbacksShareOneSettlement() {
        prepare("order");LoginUser user=new LoginUser();user.setId(445L);user.setUserType(1);SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null));
        try {when(client.request(eq("GET"),anyString(),any())).thenReturn(success("order"));assertTrue(service.completeBusiness("order",AppWxPayTypeEnum.ORDER));assertTrue(service.completeBusiness("order",AppWxPayTypeEnum.ORDER));service.settle(success("order"));assertEquals(1,credits());verify(client,times(1)).request(eq("GET"),anyString(),any());
            user.setId(446L);assertThrows(RuntimeException.class,()->service.completeBusiness("order",AppWxPayTypeEnum.ORDER));
        } finally {SecurityContextHolder.clearContext();}
    }
    @Test void preorderPricesAndTimesSurviveCacheExpiry() {
        prepare("order");WxPayOrderRespVO preorder=new WxPayOrderRespVO();preorder.setOrderNo("order");preorder.setPayPrice(1000);preorder.setPrice(1400);preorder.setStartTime(new Date(100000));preorder.setEndTime(new Date(200000));
        WxPayOrderInfo info=new WxPayOrderInfo(AppWxPayTypeEnum.RECHARGE,"order",445L,152L,18L,1000);service.retainContext("order",info,preorder);preorder.setPrice(9000);service.retainContext("order",info,preorder);
        assertEquals(1400,service.getPreOrder("order").getPrice());assertEquals(new Date(100000),service.getPreOrder("order").getStartTime());
    }
    @Test void closeOnlyMarksClosedAfterWechatConfirms() {
        prepare("order");when(client.request(eq("POST"),contains("/close"),any())).thenThrow(new IllegalStateException("timeout"));assertThrows(RuntimeException.class,()->service.close("order"));assertEquals("PENDING",attempt("order").get("state"));
        when(client.request(eq("POST"),contains("/close"),any())).thenReturn(new JSONObject());service.close("order");assertEquals("CLOSED",attempt("order").get("state"));assertThrows(RuntimeException.class,()->service.settle(success("order")));assertEquals(0,credits());
    }
    JSONObject refundResult(String no,JSONObject body,String state){JSONObject r=new JSONObject();r.put("out_refund_no",no);r.put("out_trade_no",attempt("order").get("out_trade_no"));r.put("sub_mchid","1109667212");r.put("transaction_id","txn-order");r.put("refund_id","refund-"+no);r.put("amount",body.getJSONObject("amount"));r.put("status",state);r.put("refund_status",state);return r;}
    @Test void processingRefundDoesNotIncreaseConfirmedRefundAndSuccessIsIdempotent() {
        prepare("order");service.settle(success("order"));
        when(client.request(eq("POST"),eq("/v3/refund/domestic/refunds"),any())).thenAnswer(call->{JSONObject body=call.getArgument(2);JSONObject r=refundResult(body.getString("out_refund_no"),body,"PROCESSING");r.remove("sub_mchid");return r;});
        service.refund(readPayment("order"),100,"test");assertEquals(0,readPayment("order").getRefundPrice());verify(pay,never()).applyPartnerRefund(any(),anyInt());
        String no=jdbc.queryForObject("SELECT out_refund_no FROM member_wechat_partner_refund",String.class);
        JSONObject body=new JSONObject();body.put("amount",JSON.parseObject("{\"refund\":100,\"total\":1000,\"currency\":\"CNY\"}"));JSONObject notified=refundResult(no,body,"SUCCESS");notified.put("mchid","1104993367");
        service.acceptRefund(notified);service.acceptRefund(notified);assertEquals(100,readPayment("order").getRefundPrice());assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM refunded_score",Integer.class));
    }
    @Test void wrongRefundAndExcessAmountCannotChangeMoney() {
        prepare("order");service.settle(success("order"));assertThrows(RuntimeException.class,()->service.refund(readPayment("order"),1001,"test"));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM member_wechat_partner_refund",Integer.class));
        when(client.request(eq("POST"),eq("/v3/refund/domestic/refunds"),any())).thenAnswer(call->{JSONObject body=call.getArgument(2);JSONObject bad=refundResult(body.getString("out_refund_no"),body,"SUCCESS");bad.put("sub_mchid","wrong");return bad;});
        assertThrows(RuntimeException.class,()->service.refund(readPayment("order"),100,"test"));assertEquals(0,readPayment("order").getRefundPrice());
    }
}
