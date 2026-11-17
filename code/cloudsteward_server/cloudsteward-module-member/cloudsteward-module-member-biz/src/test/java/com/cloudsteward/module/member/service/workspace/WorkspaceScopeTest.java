package com.cloudsteward.module.member.service.workspace;
import com.alibaba.fastjson.JSONObject;
import com.cloudsteward.framework.security.core.LoginUser;
import com.cloudsteward.framework.security.core.service.SecurityFrameworkService;
import com.cloudsteward.module.member.dal.dataobject.storeinfo.StoreInfoDO;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class WorkspaceScopeTest {
    WorkspaceOperationsService service;StoreInfoMapper stores;SecurityFrameworkService security;JdbcTemplate jdbc;
    @BeforeEach void setup() {
        service=new WorkspaceOperationsService();stores=mock(StoreInfoMapper.class);security=mock(SecurityFrameworkService.class);jdbc=mock(JdbcTemplate.class);
        ReflectionTestUtils.setField(service,"stores",stores);ReflectionTestUtils.setField(service,"security",security);ReflectionTestUtils.setField(service,"jdbc",jdbc);
        LoginUser user=new LoginUser();user.setId(130L);user.setUserType(2);user.setTenantId(152L);SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null));
        StoreInfoDO own=new StoreInfoDO();own.setStoreId(18L);own.setTenantId(152L);when(stores.selectById(18L)).thenReturn(own);
        StoreInfoDO foreign=new StoreInfoDO();foreign.setStoreId(19L);foreign.setTenantId(999L);when(stores.selectById(19L)).thenReturn(foreign);
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void merchantCannotReadForeignStoreEvenWhenGlobalTenantSqlIsDisabled() {
        assertNotNull(service.requireStore(18L));assertThrows(RuntimeException.class,()->service.requireStore(19L));verifyNoInteractions(jdbc);
    }
    @Test void platformRoleCanManageForeignStoreButUnknownIdsStillFail() {
        when(security.hasRole("super_admin")).thenReturn(true);assertNotNull(service.requireStore(19L));assertThrows(RuntimeException.class,()->service.requireStore(999L));
    }
    @Test void queryIdentifiersAreAllowlistedAndRecordsCannotBeMovedAcrossStores() {
        assertThrows(RuntimeException.class,()->service.spec("orders; DROP TABLE member_order_info"));
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq(5L),eq(18L))).thenReturn(0);
        assertThrows(RuntimeException.class,()->service.requireRow("orders",5L,18L));
        verify(jdbc).queryForObject(contains("order_id=? AND store_id=?"),eq(Integer.class),eq(5L),eq(18L));
    }
    @Test void unknownActionsAndMissingStoresCannotReachBusinessServices() {
        JSONObject data=new JSONObject();assertThrows(RuntimeException.class,()->service.action("refundOrder",data));
        data.put("storeId",18L);assertThrows(RuntimeException.class,()->service.action("arbitrary",data));
    }
}
