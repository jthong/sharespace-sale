package com.cloudsteward.module.system.service.oauth2;

import com.cloudsteward.framework.tenant.core.context.TenantContextHolder;
import com.cloudsteward.module.system.dal.dataobject.oauth2.*;
import com.cloudsteward.module.system.dal.dataobject.user.AdminUserDO;
import com.cloudsteward.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import com.cloudsteward.module.system.dal.mysql.user.AdminUserMapper;
import com.cloudsteward.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminTokenTenantTest {
    @Test
    void tokenUsesAccountTenantRatherThanMissingOrSpoofedRequestTenant() {
        OAuth2TokenServiceImpl service = new OAuth2TokenServiceImpl();
        AdminUserMapper users = mock(AdminUserMapper.class);
        AdminUserDO user = new AdminUserDO(); user.setTenantId(152L);
        when(users.selectById(130L)).thenReturn(user);
        ReflectionTestUtils.setField(service, "adminUserMapper", users);
        ReflectionTestUtils.setField(service, "oauth2AccessTokenMapper", mock(OAuth2AccessTokenMapper.class));
        ReflectionTestUtils.setField(service, "oauth2AccessTokenRedisDAO", mock(OAuth2AccessTokenRedisDAO.class));
        OAuth2RefreshTokenDO refresh = new OAuth2RefreshTokenDO();
        refresh.setUserId(130L).setUserType(2).setRefreshToken("test-only");
        OAuth2ClientDO client = new OAuth2ClientDO();
        client.setClientId("default").setAccessTokenValiditySeconds(600);
        try {
            TenantContextHolder.setTenantId(1L);
            OAuth2AccessTokenDO token = ReflectionTestUtils.invokeMethod(service, "createOAuth2AccessToken", refresh, client);
            assertNotNull(token);
            assertEquals(152L, token.getTenantId());
        } finally { TenantContextHolder.clear(); }
    }
}
