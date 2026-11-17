package com.cloudsteward.module.member.service.wx.partner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.*;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import com.cloudsteward.framework.common.exception.ErrorCode;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.*;
import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;

/** Uses the official APIv3 HTTP client for request signing and response verification. */
@Component
public class WechatPartnerClient {
    @Resource private WechatPartnerProperties properties;
    private volatile CloseableHttpClient http;
    private volatile Verifier verifier;
    private volatile PrivateKey privateKey;
    private volatile long nextInitializationAttempt;
    private static final ErrorCode ERROR = new ErrorCode(1004999010, "{}");

    public void validateConfiguration() {
        try {
            if (!properties.isEnabled()) throw new IllegalArgumentException("服务商支付未启用");
            requireText(properties.getSpAppId()); requireText(properties.getMiniAppId());
            if (properties.getSpMchId() == null || !properties.getSpMchId().matches("[0-9]{8,32}")) throw new IllegalArgumentException("服务商商户号未配置");
            if (properties.getApiV3Key() == null || properties.getApiV3Key().getBytes(StandardCharsets.UTF_8).length != 32) throw new IllegalArgumentException("APIv3密钥未配置");
            for (String url : new String[]{properties.getNotifyUrl(), properties.getRefundNotifyUrl()}) {
                java.net.URI uri = new java.net.URI(url); if (!"https".equals(uri.getScheme()) || uri.getHost()==null || uri.getQuery()!=null) throw new IllegalArgumentException("支付回调地址必须为HTTPS地址");
            }
            if (!"sub".equals(properties.getPayerScope()) && !"sp".equals(properties.getPayerScope())) throw new IllegalArgumentException("OpenID归属配置无效");
            if ("sp".equals(properties.getPayerScope()) && !properties.getMiniAppId().equals(properties.getSpAppId())) throw new IllegalArgumentException("服务商OpenID须属于实际小程序AppID");
            PrivateKey key;
            X509Certificate cert;
            try (InputStream in = Files.newInputStream(Paths.get(properties.getPrivateKeyPath()))) { key = PemUtil.loadPrivateKey(in); }
            try (InputStream in = Files.newInputStream(Paths.get(properties.getCertificatePath()))) { cert = PemUtil.loadCertificate(in); }
            cert.checkValidity();
            if (!cert.getSerialNumber().toString(16).equalsIgnoreCase(properties.getSerialNumber())) throw new IllegalArgumentException("服务商证书序列号不匹配");
            javax.naming.ldap.LdapName name = new javax.naming.ldap.LdapName(cert.getSubjectX500Principal().getName());
            boolean merchantMatches = name.getRdns().stream().anyMatch(r -> "CN".equalsIgnoreCase(r.getType()) && properties.getSpMchId().equals(String.valueOf(r.getValue())));
            if (!merchantMatches) throw new IllegalArgumentException("证书不属于配置的服务商商户");
            byte[] probe = "sharespace-partner-key-check".getBytes(StandardCharsets.UTF_8);
            Signature signature = Signature.getInstance("SHA256withRSA");signature.initSign(key);signature.update(probe);byte[] signed=signature.sign();
            signature.initVerify(cert.getPublicKey());signature.update(probe);if (!signature.verify(signed)) throw new IllegalArgumentException("服务商私钥与证书不匹配");
            privateKey = key;
        } catch (Exception e) { throw exception(ERROR, "微信服务商支付配置不完整或证书校验失败，请联系管理员"); }
    }

    private static void requireText(String s) { if (s==null || s.trim().isEmpty()) throw new IllegalArgumentException("missing value"); }
    protected synchronized void initialize() {
        if (http != null) return;
        if (System.currentTimeMillis() < nextInitializationAttempt) throw exception(ERROR,"微信平台证书暂不可用，请稍后重试");
        validateConfiguration();
        try {
            CertificatesManager manager=CertificatesManager.getInstance();
            manager.putMerchant(properties.getSpMchId(),new WechatPay2Credentials(properties.getSpMchId(),new PrivateKeySigner(properties.getSerialNumber(),privateKey)),properties.getApiV3Key().getBytes(StandardCharsets.UTF_8));
            verifier=manager.getVerifier(properties.getSpMchId());
            http=WechatPayHttpClientBuilder.create().withMerchant(properties.getSpMchId(),properties.getSerialNumber(),privateKey)
                    .withValidator(new WechatPay2Validator(verifier)).setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(10000).setSocketTimeout(20000).setConnectionRequestTimeout(10000).build()).build();
        } catch(Exception e) { nextInitializationAttempt=System.currentTimeMillis()+60000;throw exception(ERROR,"无法验证微信平台证书，请核对服务商凭证及网络后重试"); }
    }

    public void verifyPlatformCertificates() { initialize(); }

    public JSONObject request(String method, String path, JSONObject body) {
        if (!path.startsWith("/v3/") || path.contains("\r") || path.contains("\n")) throw exception(ERROR,"微信接口路径无效");
        initialize();
        HttpRequestBase request;
        if ("GET".equals(method)) request=new HttpGet("https://api.mch.weixin.qq.com"+path);
        else { HttpPost post=new HttpPost("https://api.mch.weixin.qq.com"+path);post.setEntity(new StringEntity(body==null?"":body.toJSONString(),StandardCharsets.UTF_8));request=post; }
        request.setHeader("Accept","application/json");request.setHeader("Content-Type","application/json");
        try (CloseableHttpResponse response=http.execute(request)) {
            int status=response.getStatusLine().getStatusCode();String content=response.getEntity()==null?"":EntityUtils.toString(response.getEntity(),StandardCharsets.UTF_8);
            if (status<200 || status>=300) {
                String code="WECHAT_ERROR";try { String candidate=JSON.parseObject(content).getString("code");if (candidate!=null && candidate.matches("[A-Z0-9_]{1,64}")) code=candidate; }catch(Exception ignored){}
                throw exception(ERROR,"微信支付请求未成功（"+code+"），请核对订单状态后重试");
            }
            return content.isEmpty()?new JSONObject():JSON.parseObject(content);
        } catch (IOException e) { throw exception(ERROR,"微信支付请求结果未确认，请使用原订单重试或查单"); }
    }

    public String signMiniapp(String appId,String timestamp,String nonce,String pkg) {
        if (privateKey==null) validateConfiguration();
        try { Signature s=Signature.getInstance("SHA256withRSA");s.initSign(privateKey);s.update((appId+"\n"+timestamp+"\n"+nonce+"\n"+pkg+"\n").getBytes(StandardCharsets.UTF_8));return Base64.getEncoder().encodeToString(s.sign()); }
        catch(Exception e) { throw exception(ERROR,"微信支付签名失败"); }
    }

    public JSONObject decodeNotification(String body,String timestamp,String nonce,String serial,String signature) {
        try {
            requireText(body);requireText(timestamp);requireText(nonce);requireText(serial);requireText(signature);
            if (Math.abs(Instant.now().getEpochSecond()-Long.parseLong(timestamp))>300) throw new IllegalArgumentException("expired");
            initialize();
            if (!verifier.verify(serial,(timestamp+"\n"+nonce+"\n"+body+"\n").getBytes(StandardCharsets.UTF_8),signature)) throw new IllegalArgumentException("signature");
            JSONObject envelope=JSON.parseObject(body);JSONObject resource=envelope.getJSONObject("resource");
            if (resource==null || !"AEAD_AES_256_GCM".equals(resource.getString("algorithm"))) throw new IllegalArgumentException("algorithm");
            String clear=new AesUtil(properties.getApiV3Key().getBytes(StandardCharsets.UTF_8)).decryptToString(
                    resource.getString("associated_data")==null?new byte[0]:resource.getString("associated_data").getBytes(StandardCharsets.UTF_8),resource.getString("nonce").getBytes(StandardCharsets.UTF_8),resource.getString("ciphertext"));
            JSONObject decoded=new JSONObject();decoded.put("event_type",envelope.getString("event_type"));decoded.put("resource",JSON.parseObject(clear));return decoded;
        } catch(Exception e) { throw exception(ERROR,"微信通知验签或解密失败"); }
    }
}
