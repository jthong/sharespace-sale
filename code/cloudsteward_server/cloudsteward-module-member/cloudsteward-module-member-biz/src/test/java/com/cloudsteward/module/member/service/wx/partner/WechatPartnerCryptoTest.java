package com.cloudsteward.module.member.service.wx.partner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.time.Instant;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

/** Uses ephemeral test keys; no network or real payment is involved. */
class WechatPartnerCryptoTest {
    KeyPair platform;WechatPartnerProperties properties;WechatPartnerClient client;
    static final String API_KEY="01234567890123456789012345678901";
    @BeforeEach void setup() throws Exception {
        KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);platform=generator.generateKeyPair();
        properties=new WechatPartnerProperties();properties.setApiV3Key(API_KEY);
        client=new WechatPartnerClient(){@Override protected synchronized void initialize(){}};
        ReflectionTestUtils.setField(client,"properties",properties);
        ReflectionTestUtils.setField(client,"verifier",new Verifier(){
            public boolean verify(String serial,byte[] message,String value){try{if(!"platform-serial".equals(serial))return false;Signature s=Signature.getInstance("SHA256withRSA");s.initVerify(platform.getPublic());s.update(message);return s.verify(Base64.getDecoder().decode(value));}catch(Exception e){return false;}}
            public X509Certificate getValidCertificate(){return null;}
        });
        ReflectionTestUtils.setField(client,"privateKey",platform.getPrivate());
    }
    String body() throws Exception {
        String nonce="012345678901",associated="transaction";
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(API_KEY.getBytes(StandardCharsets.UTF_8),"AES"),new GCMParameterSpec(128,nonce.getBytes(StandardCharsets.UTF_8)));cipher.updateAAD(associated.getBytes(StandardCharsets.UTF_8));
        JSONObject resource=new JSONObject();resource.put("algorithm","AEAD_AES_256_GCM");resource.put("nonce",nonce);resource.put("associated_data",associated);resource.put("ciphertext",Base64.getEncoder().encodeToString(cipher.doFinal("{\"trade_state\":\"SUCCESS\",\"out_trade_no\":\"test\"}".getBytes(StandardCharsets.UTF_8))));
        JSONObject envelope=new JSONObject();envelope.put("event_type","TRANSACTION.SUCCESS");envelope.put("resource",resource);return envelope.toJSONString();
    }
    String sign(String body,String timestamp,String nonce) throws Exception {Signature s=Signature.getInstance("SHA256withRSA");s.initSign(platform.getPrivate());s.update((timestamp+"\n"+nonce+"\n"+body+"\n").getBytes(StandardCharsets.UTF_8));return Base64.getEncoder().encodeToString(s.sign());}
    @Test void authenticNotificationIsVerifiedAndDecrypted() throws Exception {
        String body=body(),time=String.valueOf(Instant.now().getEpochSecond());JSONObject decoded=client.decodeNotification(body,time,"nonce","platform-serial",sign(body,time,"nonce"));
        assertEquals("TRANSACTION.SUCCESS",decoded.getString("event_type"));assertEquals("SUCCESS",decoded.getJSONObject("resource").getString("trade_state"));
    }
    @Test void tamperedBodyUnknownSerialAndExpiredReplayAreRejected() throws Exception {
        String body=body(),time=String.valueOf(Instant.now().getEpochSecond()),signature=sign(body,time,"nonce");
        assertThrows(RuntimeException.class,()->client.decodeNotification(body+" ",time,"nonce","platform-serial",signature));
        assertThrows(RuntimeException.class,()->client.decodeNotification(body,time,"nonce","wrong-serial",signature));
        String old=String.valueOf(Instant.now().getEpochSecond()-600);assertThrows(RuntimeException.class,()->client.decodeNotification(body,old,"nonce","platform-serial",sign(body,old,"nonce")));
    }
    @Test void validSignatureCannotHideWrongAesKeyOrAuthenticationTag() throws Exception {
        String body=body(),time=String.valueOf(Instant.now().getEpochSecond());properties.setApiV3Key("98765432109876543210987654321098");
        assertThrows(RuntimeException.class,()->client.decodeNotification(body,time,"nonce","platform-serial",sign(body,time,"nonce")));
        properties.setApiV3Key(API_KEY);JSONObject changed=JSON.parseObject(body);changed.getJSONObject("resource").put("ciphertext",Base64.getEncoder().encodeToString(new byte[32]));String bad=changed.toJSONString();
        assertThrows(RuntimeException.class,()->client.decodeNotification(bad,time,"nonce","platform-serial",sign(bad,time,"nonce")));
    }
    @Test void rsaPaySignUsesTheActualMiniappIdAndTrailingNewline() throws Exception {
        String signature=client.signMiniapp("actual-miniapp","123","nonce","prepay_id=test");Signature verifier=Signature.getInstance("SHA256withRSA");verifier.initVerify(platform.getPublic());verifier.update("actual-miniapp\n123\nnonce\nprepay_id=test\n".getBytes(StandardCharsets.UTF_8));assertTrue(verifier.verify(Base64.getDecoder().decode(signature)));
        verifier.initVerify(platform.getPublic());verifier.update("wrong-app\n123\nnonce\nprepay_id=test\n".getBytes(StandardCharsets.UTF_8));assertFalse(verifier.verify(Base64.getDecoder().decode(signature)));
    }
    @Test void suppliedMerchantCertificateAndPrivateKeyMatchLocally(@TempDir Path temp) throws Exception {
        String directory=System.getProperty("partner.test.credentials");Assumptions.assumeTrue(directory!=null);
        WechatPartnerProperties config=new WechatPartnerProperties();config.setEnabled(true);config.setSpAppId("test-sp");config.setSpMchId("1104993367");config.setMiniAppId("test-miniapp");config.setApiV3Key(API_KEY);config.setNotifyUrl("https://example.test/notify");config.setRefundNotifyUrl("https://example.test/refund");config.setSerialNumber("2B275AC8450693F06CF1929C02102AED085711FD");
        config.setCertificatePath(Paths.get(directory,"apiclient_cert.pem").toString());config.setPrivateKeyPath(Paths.get(directory,"apiclient_key.pem").toString());WechatPartnerClient local=new WechatPartnerClient();ReflectionTestUtils.setField(local,"properties",config);local.validateConfiguration();
        config.setSpMchId("1104993368");assertThrows(RuntimeException.class,local::validateConfiguration);config.setSpMchId("1104993367");
        Path wrong=temp.resolve("wrong-key.pem");Files.write(wrong,("-----BEGIN PRIVATE KEY-----\n"+Base64.getMimeEncoder().encodeToString(platform.getPrivate().getEncoded())+"\n-----END PRIVATE KEY-----\n").getBytes(StandardCharsets.US_ASCII));config.setPrivateKeyPath(wrong.toString());assertThrows(RuntimeException.class,local::validateConfiguration);
    }
}
