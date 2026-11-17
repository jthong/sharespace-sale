package com.cloudsteward.module.member.framework;

import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.cloudsteward.framework.security.core.LoginUser;
import com.cloudsteward.framework.security.core.service.SecurityFrameworkService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberStoreScopeTest {
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @Test
    @SuppressWarnings("unchecked")
    void merchantSqlIsScopedEvenWhenLegacyTenantFeatureIsDisabled() {
        SecurityFrameworkService security = mock(SecurityFrameworkService.class);
        ObjectProvider<SecurityFrameworkService> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(security);
        LoginUser user = new LoginUser();
        user.setId(130L); user.setUserType(2); user.setTenantId(152L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
        TenantLineInnerInterceptor interceptor = new TenantLineInnerInterceptor(
                new MemberStoreScopeConfiguration.ScopeHandler(provider));
        String sql = interceptor.parserSingle("SELECT d.*,s.store_name FROM member_device_info d LEFT JOIN member_store_info s ON s.store_id=d.store_id WHERE d.device_id=99", null);
        assertTrue(sql.contains("d.tenant_id = 152"), sql);
        assertTrue(sql.contains("s.tenant_id = 152"), sql);
        String systemSql = interceptor.parserSingle("SELECT * FROM system_role WHERE id=1", null);
        assertFalse(systemSql.contains("tenant_id"), systemSql);
        when(security.hasRole("super_admin")).thenReturn(true);
        assertFalse(interceptor.parserSingle("SELECT * FROM member_store_info", null).contains("tenant_id"));
    }
}
