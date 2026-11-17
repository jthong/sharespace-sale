package com.cloudsteward.module.system.service.auth;
import com.cloudsteward.framework.tenant.core.context.TenantContextHolder;
import com.cloudsteward.module.system.controller.admin.auth.vo.*;
import com.cloudsteward.module.system.dal.dataobject.user.AdminUserDO;
import com.cloudsteward.module.system.dal.dataobject.tenant.TenantDO;
import com.cloudsteward.module.system.dal.mysql.user.AdminUserMapper;
import com.cloudsteward.module.system.service.user.AdminUserService;
import com.cloudsteward.module.system.service.tenant.TenantService;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Arrays;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AccountTenantLoginTest {
    AccountTenantLoginService service;AdminUserMapper users;AdminUserService passwords;AdminAuthService auth;TenantService tenants;AuthAutoLoginReqVO request;
    @BeforeEach void setup() {
        service=new AccountTenantLoginService();users=mock(AdminUserMapper.class);passwords=mock(AdminUserService.class);auth=mock(AdminAuthService.class);tenants=mock(TenantService.class);
        ReflectionTestUtils.setField(service,"users",users);ReflectionTestUtils.setField(service,"userService",passwords);ReflectionTestUtils.setField(service,"authService",auth);ReflectionTestUtils.setField(service,"tenants",tenants);
        request=new AuthAutoLoginReqVO();request.setUsername("merchant");request.setPassword("test-only");
        AdminUserDO one=new AdminUserDO();one.setTenantId(1L);one.setPassword("hash-one");AdminUserDO two=new AdminUserDO();two.setTenantId(152L);two.setPassword("hash-two");
        when(users.selectLoginCandidates("merchant")).thenReturn(Arrays.asList(one,two));
        when(passwords.isPasswordMatch("test-only","hash-two")).thenReturn(true);
        when(tenants.getTenant(152L)).thenReturn(TenantDO.builder().id(152L).name("organization-two").build());
    }
    @AfterEach void clear() { TenantContextHolder.clear(); }
    @Test void resolvesVerifiedAccountAndRestoresUntrustedPreviousContext() {
        TenantContextHolder.setTenantId(999L);TenantContextHolder.setIgnore(true);
        when(auth.login(request)).thenAnswer(call->{assertEquals(152L,TenantContextHolder.getTenantId());assertFalse(TenantContextHolder.isIgnore());return new AuthLoginRespVO();});
        AuthLoginRespVO result=service.login(request);assertEquals(152L,result.getTenantId());verify(tenants).validTenant(152L);
        assertEquals(999L,TenantContextHolder.getTenantId());assertTrue(TenantContextHolder.isIgnore());
    }
    @Test void sameCredentialsAcrossOrganizationsNeverSelectsTheFirstAccount() {
        when(passwords.isPasswordMatch("test-only","hash-one")).thenReturn(true);
        assertThrows(RuntimeException.class,()->service.login(request));verifyNoInteractions(auth);
        request.setOrganizationName("organization-two");when(tenants.getTenant(1L)).thenReturn(TenantDO.builder().name("organization-one").build());
        when(auth.login(request)).thenReturn(new AuthLoginRespVO());assertEquals(152L,service.login(request).getTenantId());
    }
    @Test void expiredOrDisabledTenantCannotReceiveToken() {
        doThrow(new IllegalStateException("disabled")).when(tenants).validTenant(152L);
        assertThrows(RuntimeException.class,()->service.login(request));verifyNoInteractions(auth);
    }
}
