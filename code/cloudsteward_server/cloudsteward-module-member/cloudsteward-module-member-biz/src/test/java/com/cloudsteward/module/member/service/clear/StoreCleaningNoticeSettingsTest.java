package com.cloudsteward.module.member.service.clear;

import com.cloudsteward.framework.common.exception.ServiceException;
import com.cloudsteward.framework.security.core.LoginUser;
import com.cloudsteward.framework.security.core.service.SecurityFrameworkService;
import com.cloudsteward.module.infra.api.config.ConfigApi;
import com.cloudsteward.module.member.dal.dataobject.storeinfo.StoreInfoDO;
import com.cloudsteward.module.member.dal.mysql.member.StoreWxpayConfigMapper;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import com.cloudsteward.module.member.service.wx.MiniappConfigVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StoreCleaningNoticeSettingsTest {
    private static final String TEMPLATE = "test-template-012345678901";
    private final StoreCleaningNoticeSettings settings = new StoreCleaningNoticeSettings();
    private final CleaningNoticeService notices = new CleaningNoticeService();
    private final StoreInfoMapper stores = mock(StoreInfoMapper.class);
    private final StoreWxpayConfigMapper apps = mock(StoreWxpayConfigMapper.class);
    private final SecurityFrameworkService security = mock(SecurityFrameworkService.class);
    private final ConfigApi config = mock(ConfigApi.class);
    private final Map<String, String> saved = new HashMap<>();

    @BeforeEach
    void setup() {
        LoginUser admin = new LoginUser();
        admin.setUserType(2);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(admin, null));
        ReflectionTestUtils.setField(settings, "storeInfoMapper", stores);
        ReflectionTestUtils.setField(settings, "storeWxpayConfigMapper", apps);
        ReflectionTestUtils.setField(settings, "security", security);
        ReflectionTestUtils.setField(settings, "configApi", config);
        ReflectionTestUtils.setField(settings, "cleaningNotices", notices);
        ReflectionTestUtils.setField(notices, "configApi", config);
        StoreInfoDO store = new StoreInfoDO();
        store.setStoreId(18L);
        store.setTenantId(152L);
        when(stores.selectById(18L)).thenReturn(store);
        MiniappConfigVO app = new MiniappConfigVO();
        app.setMiniappId("wx-test");
        app.setMiniappSecret("test-secret");
        when(apps.getMiniappConfig(152L)).thenReturn(app);
        when(config.getConfigValueByKey(anyString())).thenAnswer(call -> saved.get(call.getArgument(0)));
        when(config.updateConfigValue(anyString(), anyString())).thenAnswer(call -> {
            saved.put(call.getArgument(0), call.getArgument(1));
            return 1;
        });
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void templateCanBePreparedBeforeStoreSwitchesChannel() {
        assertEquals("wecom", settings.get(18L).getChannel());
        assertThrows(ServiceException.class, () -> settings.saveChannel(18L, "miniapp"));
        when(security.hasRole("super_admin")).thenReturn(true);
        settings.saveTemplate(18L, TEMPLATE, Collections.singletonMap("time1", "finishedAt"));
        assertEquals(TEMPLATE, settings.get(18L).getTemplateId());
        assertEquals("wecom", settings.get(18L).getChannel());
        settings.saveChannel(18L, "miniapp");
        assertEquals("miniapp", settings.get(18L).getChannel());
        settings.saveChannel(18L, "wecom");
        assertEquals(TEMPLATE, settings.get(18L).getTemplateId());
    }

    @Test
    void storeOperatorCannotEditSharedTemplateOrUseAnUnscopedStore() {
        assertFalse(settings.get(18L).isCanEditTemplate());
        assertThrows(ServiceException.class,
                () -> settings.saveTemplate(18L, TEMPLATE, Collections.singletonMap("thing1", "roomName")));
        assertThrows(ServiceException.class, () -> settings.get(999L));
        assertThrows(ServiceException.class, () -> settings.saveChannel(999L, "wecom"));
        assertTrue(saved.isEmpty());
    }

    @Test
    void invalidFieldMappingIsRejectedBeforeSaving() {
        when(security.hasRole("super_admin")).thenReturn(true);
        assertThrows(ServiceException.class,
                () -> settings.saveTemplate(18L, TEMPLATE, Collections.singletonMap("time1", "roomName")));
        assertTrue(saved.isEmpty());
    }
}
