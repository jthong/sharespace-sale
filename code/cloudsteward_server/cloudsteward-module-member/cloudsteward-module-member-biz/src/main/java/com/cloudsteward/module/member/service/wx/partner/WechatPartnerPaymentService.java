package com.cloudsteward.module.member.service.wx.partner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloudsteward.framework.common.exception.ErrorCode;
import com.cloudsteward.framework.security.core.LoginUser;
import com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils;
import com.cloudsteward.framework.tenant.core.context.TenantContextHolder;
import com.cloudsteward.module.member.controller.app.order.vo.WxPayOrderInfo;
import com.cloudsteward.module.member.controller.app.order.vo.WxPayOrderRespVO;
import com.cloudsteward.module.member.dal.dataobject.payorder.PayOrderDO;
import com.cloudsteward.module.member.service.payorder.PayOrderService;
import com.cloudsteward.module.member.enums.AppWxPayTypeEnum;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;

/** Durable attempts keep money/merchant identity independent of mutable store configuration. */
@Service
public class WechatPartnerPaymentService {
    public static final String TYPE="微信服务商V3";
    @Resource private JdbcTemplate jdbc;
    @Resource private PlatformTransactionManager transactionManager;
    @Resource private WechatPartnerProperties properties;
    @Resource private WechatPartnerClient client;
    @Resource private StorePartnerMerchantService merchants;
    @Resource @Lazy private PayOrderService payOrders;
    private static final ErrorCode ERROR=new ErrorCode(1004999012,"{}");
    private final ThreadLocal<String> applyingOrder=new ThreadLocal<>();
    private <T> T transaction(boolean separate,Supplier<T> action) {
        TransactionTemplate tx=new TransactionTemplate(transactionManager);
        if(separate)tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return tx.execute(status->action.get());
    }
    private void check(boolean condition,String message){if(!condition)throw exception(ERROR,message);}
    static String utf8Limit(String value,int maxBytes) {
        int end=0,bytes=0;
        while(end<value.length()) {
            int next=end+Character.charCount(value.codePointAt(end));
            int size=value.substring(end,next).getBytes(StandardCharsets.UTF_8).length;
            if(bytes+size>maxBytes)break;
            bytes+=size;end=next;
        }
        return value.substring(0,end);
    }
    private static String text(Map<String,Object> row,String key){Object value=row.get(key);return value==null?null:String.valueOf(value);}
    private static int number(Map<String,Object> row,String key){Object value=row.get(key);return value==null?0:((Number)value).intValue();}
    private static long id(Map<String,Object> row,String key){Object value=row.get(key);return value==null?0:((Number)value).longValue();}
    private static boolean flag(Map<String,Object> row,String key){Object v=row.get(key);return Boolean.TRUE.equals(v)||(v instanceof Number&&((Number)v).intValue()!=0)||(v instanceof byte[]&&((byte[])v).length>0&&((byte[])v)[0]!=0);}
    private Map<String,Object> attempt(String orderNo,boolean lock) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM member_wechat_partner_payment WHERE order_no=?"+(lock?" FOR UPDATE":""),orderNo);
        check(rows.size()==1,"微信支付流水不存在");return rows.get(0);
    }
    public boolean hasAttempt(String orderNo) { return jdbc.queryForObject("SELECT COUNT(*) FROM member_wechat_partner_payment WHERE order_no=?",Integer.class,orderNo)>0; }
    public boolean enabled(){return properties.isEnabled();}

    public WxPayOrderRespVO create(WxPayOrderRespVO response,PayOrderDO payment,String openId) {
        client.validateConfiguration();
        check(payment.getPrice()!=null&&payment.getPrice()>0,"支付金额必须大于0");
        check(payment.getOrderNo()!=null&&payment.getOrderNo().length()<=64,"支付订单号无效");
        check(openId!=null&&!openId.isEmpty(),"请先登录并绑定当前小程序身份");
        Map<String,Object> saved=transaction(true,()->{
            if(!hasAttempt(payment.getOrderNo())) {
                check(jdbc.queryForObject("SELECT COUNT(*) FROM member_pay_order WHERE order_no=? AND deleted=0",Integer.class,payment.getOrderNo())==0,"原订单已有支付记录，请继续原支付通道或先关闭原支付单");
                String sub=merchants.require(payment.getStoreId());
                jdbc.update("INSERT INTO member_wechat_partner_payment(order_no,out_trade_no,store_id,user_id,tenant_id,sp_mchid,sp_appid,sub_mchid,app_id,payer_scope,payer_open_id,amount) VALUES(?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE order_no=order_no",
                        payment.getOrderNo(),"SS"+UUID.randomUUID().toString().replace("-","").substring(0,30),payment.getStoreId(),payment.getUserId(),payment.getTenantId(),properties.getSpMchId(),properties.getSpAppId(),sub,properties.getMiniAppId(),properties.getPayerScope(),openId,payment.getPrice());
            }
            Map<String,Object> row=attempt(payment.getOrderNo(),true);
            check(id(row,"store_id")==payment.getStoreId()&&id(row,"user_id")==payment.getUserId()&&number(row,"amount")==payment.getPrice(),"支付流水与当前订单不一致，请继续原订单处理");
            check(Objects.equals(text(row,"app_id"),properties.getMiniAppId())&&Objects.equals(text(row,"payer_open_id"),openId),"支付身份已变更，请先关闭原支付单");
            check("PENDING".equals(text(row,"state")),"订单已支付或关闭，请查询订单状态");
            if(jdbc.queryForObject("SELECT COUNT(*) FROM member_pay_order WHERE order_no=? AND deleted=0",Integer.class,payment.getOrderNo())==0) {
                jdbc.update("INSERT INTO member_pay_order(user_id,store_id,order_no,order_id,pay_type,order_desc,price,pay_status,split_order,split_status,refund_price,goods_name,type,tenant_id) VALUES(?,?,?,?,?,?,?,0,0,0,0,?,?,?)",
                        payment.getUserId(),payment.getStoreId(),payment.getOrderNo(),payment.getOrderId(),payment.getPayType(),payment.getOrderDesc(),payment.getPrice(),payment.getGoodsName(),TYPE,payment.getTenantId());
            }
            return row;
        });
        check(properties.getSpMchId().equals(text(saved,"sp_mchid")),"服务商身份已变更，请先处理原支付单");
        String prepay=text(saved,"prepay_id");
        if(prepay==null) {
            JSONObject body=new JSONObject();body.put("sp_appid",text(saved,"sp_appid"));body.put("sp_mchid",text(saved,"sp_mchid"));body.put("sub_mchid",text(saved,"sub_mchid"));
            body.put("out_trade_no",text(saved,"out_trade_no"));body.put("notify_url",properties.getNotifyUrl());body.put("description",utf8Limit(payment.getGoodsName()==null||payment.getGoodsName().trim().isEmpty()?"共享空间消费":payment.getGoodsName(),127));
            JSONObject amount=new JSONObject();amount.put("total",number(saved,"amount"));amount.put("currency","CNY");body.put("amount",amount);
            JSONObject payer=new JSONObject();String scope=text(saved,"payer_scope");payer.put("sub".equals(scope)?"sub_openid":"sp_openid",openId);body.put("payer",payer);
            if("sub".equals(scope))body.put("sub_appid",text(saved,"app_id"));
            prepay=client.request("POST","/v3/pay/partner/transactions/jsapi",body).getString("prepay_id");check(prepay!=null&&!prepay.isEmpty(),"微信未返回预支付标识，请使用原订单重试");
            final String value=prepay;transaction(true,()->jdbc.update("UPDATE member_wechat_partner_payment SET prepay_id=?,update_time=CURRENT_TIMESTAMP WHERE order_no=? AND state='PENDING'",value,payment.getOrderNo()));
        }
        String timestamp=String.valueOf(Instant.now().getEpochSecond()),nonce=UUID.randomUUID().toString().replace("-",""),pkg="prepay_id="+prepay;
        response.setAppId(text(saved,"app_id"));response.setPkg(pkg);response.setNonceStr(nonce);response.setTimeStamp(timestamp);response.setSignType("RSA");response.setPaySign(client.signMiniapp(text(saved,"app_id"),timestamp,nonce,pkg));return response;
    }

    public void retainContext(String orderNo,WxPayOrderInfo context) {
        retainContext(orderNo,context,null);
    }
    public void retainContext(String orderNo,WxPayOrderInfo context,WxPayOrderRespVO preorder) {
        if(!hasAttempt(orderNo))return;
        Map<String,Object> row=attempt(orderNo,false);
        if(context.getUserId()==null)context.setUserId(id(row,"user_id"));
        if(context.getStoreId()==null)context.setStoreId(id(row,"store_id"));
        check(Objects.equals(orderNo,context.getOrderNo())&&id(row,"user_id")==context.getUserId()&&id(row,"store_id")==context.getStoreId()
                &&id(row,"tenant_id")== (context.getTenantId()==null?0:context.getTenantId()),"支付业务上下文不匹配");
        jdbc.update("UPDATE member_wechat_partner_payment SET business_context=?,update_time=CURRENT_TIMESTAMP WHERE order_no=? AND business_context IS NULL",JSON.toJSONString(context),orderNo);
        if(preorder!=null) {
            check(Objects.equals(orderNo,preorder.getOrderNo())&&Objects.equals(preorder.getPayPrice(),number(row,"amount")),"预订单支付金额不匹配");
            jdbc.update("UPDATE member_wechat_partner_payment SET pre_order_context=?,update_time=CURRENT_TIMESTAMP WHERE order_no=? AND pre_order_context IS NULL",JSON.toJSONString(preorder),orderNo);
        }
    }

    public WxPayOrderRespVO getPreOrder(String orderNo) {
        String value=text(attempt(orderNo,false),"pre_order_context");
        check(value!=null,"预订单信息尚未就绪，请稍后重试");return JSON.parseObject(value,WxPayOrderRespVO.class);
    }

    /** Frontend confirmation and callbacks enter the same locked business settlement. */
    public boolean completeBusiness(String orderNo,AppWxPayTypeEnum type) {
        if(orderNo==null||!hasAttempt(orderNo)||Objects.equals(orderNo,applyingOrder.get()))return false;
        Map<String,Object> row=attempt(orderNo,false);
        LoginUser user=SecurityFrameworkUtils.getLoginUser();
        check(user!=null&&Integer.valueOf(1).equals(user.getUserType())&&Objects.equals(user.getId(),id(row,"user_id")),"无权处理其他用户的支付单");
        WxPayOrderInfo context=JSON.parseObject(text(row,"business_context"),WxPayOrderInfo.class);
        check(context!=null&&type==context.getWxPayTypeEnum(),"支付业务类型不匹配");
        if(type==AppWxPayTypeEnum.ORDER) {
            Integer payType=payOrders.getByOrderNo(orderNo).getPayType();
            if(!Integer.valueOf(1).equals(payType)&&!Integer.valueOf(4).equals(payType))return false;
        }
        checkOrder(orderNo,id(row,"store_id"),number(row,"amount"));return true;
    }

    private void verifyTransaction(Map<String,Object> row,JSONObject data) {
        check("SUCCESS".equals(data.getString("trade_state")),"微信订单尚未支付成功");
        for(String key:new String[]{"sp_mchid","sp_appid","sub_mchid","out_trade_no"})check(Objects.equals(text(row,key),data.getString(key)),"微信订单商户或订单号不匹配");
        String scope=text(row,"payer_scope"),app=text(row,"app_id");
        check(Objects.equals(app,data.getString("sub".equals(scope)?"sub_appid":"sp_appid")),"微信支付AppID不匹配");
        JSONObject amount=data.getJSONObject("amount"),payer=data.getJSONObject("payer");
        check(amount!=null&&"CNY".equals(amount.getString("currency"))&&Objects.equals(amount.getInteger("total"),number(row,"amount")),"微信支付金额或币种不匹配");
        check(payer!=null&&Objects.equals(text(row,"payer_open_id"),payer.getString("sub".equals(scope)?"sub_openid":"sp_openid")),"微信付款人不匹配");
        check(data.getString("transaction_id")!=null&&!data.getString("transaction_id").isEmpty(),"微信交易号缺失");
    }

    public void settle(JSONObject data) {
        List<String> names=jdbc.queryForList("SELECT order_no FROM member_wechat_partner_payment WHERE out_trade_no=?",String.class,data.getString("out_trade_no"));check(names.size()==1,"微信支付流水不存在");
        transaction(false,()->{
            String orderNo=names.get(0);Map<String,Object> row=attempt(orderNo,true);verifyTransaction(row,data);
            if("PAID".equals(text(row,"state"))&&flag(row,"business_settled")) {check(Objects.equals(text(row,"transaction_id"),data.getString("transaction_id")),"重复通知交易号不一致");return null;}
            check(!"CLOSED".equals(text(row,"state")),"支付单已关闭，请人工核对");
            String context=text(row,"business_context");check(context!=null,"支付业务上下文尚未就绪，请稍后重试");
            WxPayOrderInfo info=JSON.parseObject(context,WxPayOrderInfo.class);check(id(row,"user_id")==info.getUserId(),"支付业务用户不匹配");
            PayOrderDO payment=payOrders.getByOrderNo(orderNo);check(payment!=null&&TYPE.equals(payment.getType())&&Objects.equals(payment.getPrice(),number(row,"amount"))&&payment.getStoreId()==id(row,"store_id")&&payment.getUserId()==id(row,"user_id"),"本地支付流水不匹配");
            payment.setPayStatus(true);payment.setPayOrderNo(data.getString("transaction_id"));payment.setPayTime(java.time.LocalDateTime.now());
            jdbc.update("UPDATE member_pay_order SET pay_status=1,pay_order_no=?,pay_time=CURRENT_TIMESTAMP WHERE order_no=? AND deleted=0",payment.getPayOrderNo(),orderNo);
            jdbc.update("UPDATE member_wechat_partner_payment SET state='PAID',transaction_id=?,update_time=CURRENT_TIMESTAMP WHERE order_no=?",payment.getPayOrderNo(),orderNo);
            String previous=applyingOrder.get();applyingOrder.set(orderNo);
            try {payOrders.applyPartnerPayment(payment,info);} finally {if(previous==null)applyingOrder.remove();else applyingOrder.set(previous);}
            jdbc.update("UPDATE member_wechat_partner_payment SET state='PAID',business_settled=1,transaction_id=?,update_time=CURRENT_TIMESTAMP WHERE order_no=?",payment.getPayOrderNo(),orderNo);return null;
        });
    }

    public JSONObject query(String orderNo) {
        Map<String,Object> row=attempt(orderNo,false);check(properties.getSpMchId().equals(text(row,"sp_mchid")),"服务商身份与原支付单不一致");
        JSONObject data=client.request("GET","/v3/pay/partner/transactions/out-trade-no/"+text(row,"out_trade_no")+"?sp_mchid="+text(row,"sp_mchid")+"&sub_mchid="+text(row,"sub_mchid"),null);
        if("SUCCESS".equals(data.getString("trade_state")))settle(data);return data;
    }
    public void checkOrder(String orderNo,Long storeId,Integer amount) {
        Map<String,Object> row=attempt(orderNo,false);check(id(row,"store_id")==storeId&&Objects.equals(amount,number(row,"amount")),"支付门店或金额不匹配");
        if("PAID".equals(text(row,"state"))&&(flag(row,"business_settled")||Objects.equals(orderNo,applyingOrder.get())))return;
        query(orderNo);check("PAID".equals(text(attempt(orderNo,true),"state")),"微信订单尚未支付成功");
    }
    public void close(String orderNo) {
        Map<String,Object> row=attempt(orderNo,false);check("PENDING".equals(text(row,"state")),"支付单已支付或关闭");
        JSONObject body=new JSONObject();body.put("sp_mchid",text(row,"sp_mchid"));body.put("sub_mchid",text(row,"sub_mchid"));
        client.request("POST","/v3/pay/partner/transactions/out-trade-no/"+text(row,"out_trade_no")+"/close",body);
        transaction(true,()->jdbc.update("UPDATE member_wechat_partner_payment SET state='CLOSED',update_time=CURRENT_TIMESTAMP WHERE order_no=? AND state='PENDING'",orderNo));
    }

    private String refundNumber(String orderNo,int cumulative) {
        try { byte[] hash=MessageDigest.getInstance("SHA-256").digest((orderNo+":"+cumulative).getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder("SR");for(byte v:hash)b.append(String.format("%02x",v));return b.substring(0,32); }
        catch(Exception e){throw new IllegalStateException(e);}
    }
    public void refund(PayOrderDO original,Integer amount,String reason) {
        check(amount!=null&&amount>0,"退款金额必须大于0");
        String no=refundNumber(original.getOrderNo(),Math.addExact(original.getRefundPrice()==null?0:original.getRefundPrice(),amount));
        Map<String,Object> payment=transaction(true,()->{
            Map<String,Object> row=attempt(original.getOrderNo(),true);check("PAID".equals(text(row,"state")),"微信订单尚未支付成功");
            check(properties.getSpMchId().equals(text(row,"sp_mchid")),"服务商身份与原支付单不一致");
            List<Map<String,Object>> existing=jdbc.queryForList("SELECT * FROM member_wechat_partner_refund WHERE out_refund_no=?",no);
            if(existing.isEmpty()) {
                int pending=jdbc.queryForObject("SELECT COUNT(*) FROM member_wechat_partner_refund WHERE order_no=? AND state IN ('PENDING','PROCESSING')",Integer.class,original.getOrderNo());
                check(pending==0,"该支付单有退款处理中，请先核对原退款状态");
                Long reserved=jdbc.queryForObject("SELECT COALESCE(SUM(refund_amount),0) FROM member_wechat_partner_refund WHERE order_no=? AND state<>'CLOSED'",Long.class,original.getOrderNo());
                check(reserved+amount<=number(row,"amount"),"退款累计金额不能超过实际支付金额");
                jdbc.update("INSERT INTO member_wechat_partner_refund(out_refund_no,order_no,refund_amount) VALUES(?,?,?)",no,original.getOrderNo(),amount);
            } else {check(number(existing.get(0),"refund_amount")==amount,"退款请求金额不一致");}
            return row;
        });
        Map<String,Object> record=jdbc.queryForMap("SELECT * FROM member_wechat_partner_refund WHERE out_refund_no=?",no);
        if(flag(record,"applied"))return;
        check(!"CLOSED".equals(text(record,"state")),"原退款单已关闭，请核对后重新发起退款");
        JSONObject body=new JSONObject();body.put("sub_mchid",text(payment,"sub_mchid"));body.put("out_trade_no",text(payment,"out_trade_no"));body.put("out_refund_no",no);body.put("notify_url",properties.getRefundNotifyUrl());
        body.put("reason",utf8Limit(reason==null?"退款":reason,80));
        JSONObject total=new JSONObject();total.put("refund",amount);total.put("total",number(payment,"amount"));total.put("currency","CNY");body.put("amount",total);
        JSONObject response=client.request("POST","/v3/refund/domestic/refunds",body);
        // The synchronous response uses status; notifications use refund_status.
        response.put("refund_status",response.getString("status"));
        // These identifiers are required and must be echoed by WeChat, never synthesized.
        processRefund(response,false);
    }
    public void acceptRefund(JSONObject data) {
        processRefund(data,true);
    }
    private void processRefund(JSONObject data,boolean notification) {
        String no=data.getString("out_refund_no");
        List<String> names=jdbc.queryForList("SELECT order_no FROM member_wechat_partner_refund WHERE out_refund_no=?",String.class,no);check(names.size()==1,"退款流水不存在");
        transaction(false,()->{
            Map<String,Object> payment=attempt(names.get(0),true);
            Map<String,Object> refund=jdbc.queryForMap("SELECT * FROM member_wechat_partner_refund WHERE out_refund_no=? FOR UPDATE",no);
            // Signed notifications carry merchant identity. API responses do not always echo it;
            // those are verified by the official HTTP client and tied to our signed request.
            if(notification||data.getString("sub_mchid")!=null)check(Objects.equals(text(payment,"sub_mchid"),data.getString("sub_mchid")),"退款子商户不匹配");
            check(Objects.equals(text(payment,"out_trade_no"),data.getString("out_trade_no")),"退款支付单不匹配");
            check(Objects.equals(text(payment,"transaction_id"),data.getString("transaction_id")),"退款交易号不匹配");
            if(notification||data.getString("mchid")!=null)check(Objects.equals(text(payment,"sp_mchid"),data.getString("mchid")),"退款服务商不匹配");
            JSONObject amount=data.getJSONObject("amount");check(amount!=null&&Objects.equals(amount.getInteger("refund"),number(refund,"refund_amount"))&&Objects.equals(amount.getInteger("total"),number(payment,"amount")),"退款金额不匹配");
            if(amount.getString("currency")!=null)check("CNY".equals(amount.getString("currency")),"退款币种不匹配");
            String status=data.getString("refund_status");check(Arrays.asList("SUCCESS","PROCESSING","CLOSED","ABNORMAL").contains(status),"退款状态无效");
            check(data.getString("refund_id")!=null,"微信退款编号缺失");
            if(flag(refund,"applied")){check(Objects.equals(text(refund,"refund_id"),data.getString("refund_id")),"重复退款通知编号不一致");return null;}
            jdbc.update("UPDATE member_wechat_partner_refund SET state=?,refund_id=?,update_time=CURRENT_TIMESTAMP WHERE out_refund_no=?",status,data.getString("refund_id"),no);
            if("SUCCESS".equals(status)) {
                int value=number(refund,"refund_amount");
                jdbc.update("UPDATE member_pay_order SET refund_price=COALESCE(refund_price,0)+?,pay_refund_no=?,refund_time=CURRENT_TIMESTAMP WHERE order_no=? AND deleted=0",value,no,names.get(0));
                PayOrderDO paid=payOrders.getByOrderNo(names.get(0));payOrders.applyPartnerRefund(paid,value);
                jdbc.update("UPDATE member_wechat_partner_refund SET applied=1 WHERE out_refund_no=?",no);
            }
            return null;
        });
    }
    public void queryRefund(String no) {
        Map<String,Object> refund=jdbc.queryForMap("SELECT * FROM member_wechat_partner_refund WHERE out_refund_no=?",no);
        Map<String,Object> payment=attempt(text(refund,"order_no"),false);
        check(properties.getSpMchId().equals(text(payment,"sp_mchid")),"服务商身份与原支付单不一致");
        JSONObject result=client.request("GET","/v3/refund/domestic/refunds/"+no+"?sub_mchid="+text(payment,"sub_mchid"),null);result.put("refund_status",result.getString("status"));processRefund(result,false);
    }
}
