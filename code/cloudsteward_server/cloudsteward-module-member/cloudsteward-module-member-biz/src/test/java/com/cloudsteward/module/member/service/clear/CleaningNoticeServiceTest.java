package com.cloudsteward.module.member.service.clear;

import cn.binarywang.wx.miniapp.api.WxMaMsgService;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.cloudsteward.framework.common.enums.UserTypeEnum;
import com.cloudsteward.framework.common.exception.ServiceException;
import com.cloudsteward.module.infra.api.config.ConfigApi;
import com.cloudsteward.module.member.controller.app.store.vo.AppRoomListVO;
import com.cloudsteward.module.member.dal.dataobject.clearinfo.ClearInfoDO;
import com.cloudsteward.module.member.dal.dataobject.orderinfo.OrderInfoDO;
import com.cloudsteward.module.member.dal.dataobject.storeuser.StoreUserDO;
import com.cloudsteward.module.member.dal.mysql.clearinfo.ClearInfoMapper;
import com.cloudsteward.module.member.dal.mysql.orderinfo.OrderInfoMapper;
import com.cloudsteward.module.member.dal.mysql.roominfo.RoomInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storeuser.StoreUserMapper;
import com.cloudsteward.module.member.service.wx.MiniappConfigVO;
import com.cloudsteward.module.member.service.wx.MyWxService;
import com.cloudsteward.module.member.service.wx.WorkWxService;
import com.cloudsteward.module.system.api.social.SocialUserApi;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CleaningNoticeServiceTest {
    private static final String TEMPLATE = "example-cleaning-template-id";
    private static final String CHANNEL_KEY = "wechat.cleaning-notice.channel.18";
    private static final String TEMPLATE_KEY = "wechat.cleaning-notice.wx-example";
    private static final Integer MEMBER = UserTypeEnum.MEMBER.getValue();
    private final ConfigApi configApi = mock(ConfigApi.class);
    private final MyWxService myWxService = mock(MyWxService.class);
    private final RoomInfoMapper roomInfoMapper = mock(RoomInfoMapper.class);
    private final ClearInfoMapper clearInfoMapper = mock(ClearInfoMapper.class);
    private final OrderInfoMapper orderInfoMapper = mock(OrderInfoMapper.class);
    private final StoreUserMapper storeUserMapper = mock(StoreUserMapper.class);
    private final SocialUserApi socialUserApi = mock(SocialUserApi.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final WorkWxService workWx = mock(WorkWxService.class);
    private final WxMaService miniapp = mock(WxMaService.class);
    private final WxMaMsgService messages = mock(WxMaMsgService.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> values = mock(ValueOperations.class);
    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    private TransactionTemplate tx;
    private CleaningNoticeService service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "notice-test"), StoreUserDO.class);
        context.registerBean(TransactionalEventListenerFactory.class);
        context.getBeanFactory().registerSingleton("configApi", configApi);
        context.getBeanFactory().registerSingleton("myWxService", myWxService);
        context.getBeanFactory().registerSingleton("roomInfoMapper", roomInfoMapper);
        context.getBeanFactory().registerSingleton("clearInfoMapper", clearInfoMapper);
        context.getBeanFactory().registerSingleton("orderInfoMapper", orderInfoMapper);
        context.getBeanFactory().registerSingleton("storeUserMapper", storeUserMapper);
        context.getBeanFactory().registerSingleton("socialUserApi", socialUserApi);
        context.getBeanFactory().registerSingleton("stringRedisTemplate", redis);
        context.getBeanFactory().registerSingleton("workWxService", workWx);
        context.register(CleaningNoticeService.class);
        context.refresh();
        service = context.getBean(CleaningNoticeService.class);

        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:cleaning-notice");
        tx = new TransactionTemplate(new DataSourceTransactionManager(source));
        AppRoomListVO room = new AppRoomListVO();
        room.setRoomId(35L);
        room.setStoreId(18L);
        room.setTenantId(152L);
        room.setStoreName("测试门店");
        room.setRoomName("测试房间");
        when(roomInfoMapper.getInfoById(35L)).thenReturn(room);
        MiniappConfigVO app = new MiniappConfigVO();
        app.setMiniappId("wx-example");
        when(myWxService.getMiniAppConfig()).thenReturn(app);
        when(myWxService.initWxMa()).thenReturn(miniapp);
        when(miniapp.getWxMaConfig()).thenReturn(new WxMaDefaultConfigImpl());
        when(miniapp.getMsgService()).thenReturn(messages);
        when(redis.opsForValue()).thenReturn(values);
        when(configApi.getConfigValueByKey(TEMPLATE_KEY)).thenReturn(
                "{\"templateId\":\"" + TEMPLATE + "\",\"fields\":{\"thing1\":\"roomName\",\"time2\":\"finishedAt\"}}");
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void enterpriseIsDefaultAndOnlyRunsAfterCommit() {
        tx.executeWithoutResult(status -> {
            context.publishEvent(finished());
            verifyNoInteractions(workWx, messages);
        });
        verify(workWx).sendClearMsg(eq(18L), contains("测试房间"));
        verifyNoInteractions(messages);
    }

    @Test
    void rollbackDoesNotSendEitherKindOfNotice() {
        tx.executeWithoutResult(status -> {
            context.publishEvent(finished());
            status.setRollbackOnly();
        });
        verifyNoInteractions(workWx, messages);
    }

    @Test
    void selectedTemplateSendsOnlyToSubscribedStoreCleanersOncePerOrder() throws Exception {
        when(configApi.getConfigValueByKey(CHANNEL_KEY)).thenReturn("miniapp");
        when(configApi.getConfigValueByKey(TEMPLATE_KEY)).thenReturn(
                "{\"templateId\":\"" + TEMPLATE + "\",\"fields\":{\"thing1\":\"roomName\",\"time2\":\"finishedAt\",\"time9\":\"nextBookingAt\"}}");
        OrderInfoDO nextBooking = new OrderInfoDO();
        nextBooking.setStartTime(Date.from(Instant.parse("2026-11-12T04:00:00Z")));
        when(orderInfoMapper.getByRoomId(eq(35L), anyLong())).thenReturn(Collections.singletonList(nextBooking));
        when(clearInfoMapper.selectCount(any())).thenReturn(1L);
        when(storeUserMapper.selectList(any())).thenReturn(Arrays.asList(
                new StoreUserDO().setUserId(51L), new StoreUserDO().setUserId(52L)));
        when(redis.hasKey(endsWith(":51"))).thenReturn(true);
        when(values.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(true, false);
        when(socialUserApi.getUserOpenIdByType(eq(51L), anyInt())).thenReturn("subscribed-openid");

        tx.executeWithoutResult(status -> context.publishEvent(finished()));
        tx.executeWithoutResult(status -> context.publishEvent(finished()));
        ArgumentCaptor<WxMaSubscribeMessage> sent = ArgumentCaptor.forClass(WxMaSubscribeMessage.class);
        verify(messages).sendSubscribeMsg(sent.capture());
        assertEquals("subscribed-openid", sent.getValue().getToUser());
        assertEquals(TEMPLATE, sent.getValue().getTemplateId());
        assertEquals("packageA/pages/task/task?status=0&fromCleaningNotice=1", sent.getValue().getPage());
        assertTrue(sent.getValue().getData().stream().anyMatch(d -> "thing1".equals(d.getName()) && "测试房间".equals(d.getValue())));
        assertTrue(sent.getValue().getData().stream().anyMatch(d -> "time2".equals(d.getName()) && "2026年11月12日 10:05".equals(d.getValue())));
        assertTrue(sent.getValue().getData().stream().anyMatch(d -> "time9".equals(d.getName()) && "2026年11月12日 12:00".equals(d.getValue())));
        verify(socialUserApi, never()).getUserOpenIdByType(eq(52L), anyInt());
        verifyNoInteractions(workWx);
    }

    @Test
    void templateAuthorizationRequiresCleanerAndCurrentTemplate() {
        when(configApi.getConfigValueByKey(CHANNEL_KEY)).thenReturn("miniapp");
        when(storeUserMapper.getByUserIdAndType(51L, 14)).thenReturn(Collections.singletonList(
                new StoreUserDO().setUserId(51L).setStoreId(18L).setStatus(0)));
        assertNull(service.getTemplateId(52L, MEMBER));
        assertEquals(TEMPLATE, service.getTemplateId(51L, MEMBER));
        assertThrows(ServiceException.class, () -> service.recordSubscription(51L, MEMBER, "different-template"));
        service.recordSubscription(51L, MEMBER, TEMPLATE);
        verify(values).set("member:cleaning-notice:accepted:wx-example:" + TEMPLATE + ":51", "1");
        when(configApi.getConfigValueByKey(CHANNEL_KEY)).thenReturn("wecom");
        assertNull(service.getTemplateId(51L, MEMBER));
    }

    @Test
    void selectingMiniappWithoutATemplateOrWithoutACleaningTaskDoesNotSend() {
        when(configApi.getConfigValueByKey(CHANNEL_KEY)).thenReturn("miniapp");
        tx.executeWithoutResult(status -> context.publishEvent(
                new CleaningOrderFinished(35L, Collections.emptyList(), finished().getFinishedAt())));
        when(configApi.getConfigValueByKey(TEMPLATE_KEY)).thenReturn(null);
        tx.executeWithoutResult(status -> context.publishEvent(finished()));
        verifyNoInteractions(workWx, messages);
    }

    private CleaningOrderFinished finished() {
        ClearInfoDO task = new ClearInfoDO().setRoomId(35L).setStoreId(18L).setOrderId(7L).setOrderNo("example-order");
        return new CleaningOrderFinished(task, Instant.parse("2026-11-12T02:05:00Z"));
    }
}
