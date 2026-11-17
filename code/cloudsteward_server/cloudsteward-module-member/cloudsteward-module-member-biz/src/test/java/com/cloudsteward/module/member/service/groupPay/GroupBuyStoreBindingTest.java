package com.cloudsteward.module.member.service.groupPay;

import com.cloudsteward.module.member.dal.dataobject.store.StoreShopMappingDO;
import com.cloudsteward.module.member.dal.dataobject.storeinfo.StoreInfoDO;
import com.cloudsteward.module.member.dal.mysql.store.StoreShopMappingMapper;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupBuyStoreBindingTest {
    GroupBuyStoreBindingService service;
    StoreInfoMapper stores;
    StoreShopMappingMapper mappings;
    JdbcTemplate jdbc;
    static final String SHOP = "10000000000001";
    @BeforeEach void setup() {
        service = new GroupBuyStoreBindingService(); stores = mock(StoreInfoMapper.class);
        mappings = mock(StoreShopMappingMapper.class); jdbc = mock(JdbcTemplate.class);
        ReflectionTestUtils.setField(service,"stores",stores);
        ReflectionTestUtils.setField(service,"mappings",mappings);
        ReflectionTestUtils.setField(service,"jdbc",jdbc);
        when(stores.selectById(18L)).thenReturn(new StoreInfoDO());
    }
    @Test void bindsSameTextIdForBothPlatforms() {
        service.bind(18L, SHOP);
        ArgumentCaptor<StoreShopMappingDO> saved = ArgumentCaptor.forClass(StoreShopMappingDO.class);
        verify(mappings,times(2)).insert(saved.capture());
        assertEquals(Arrays.asList(1,2), Arrays.asList(saved.getAllValues().get(0).getPlatform(),saved.getAllValues().get(1).getPlatform()));
        for(StoreShopMappingDO row:saved.getAllValues()) { assertEquals(18L,row.getStoreId()); assertEquals(SHOP,row.getShopId()); }
    }
    @Test void invisibleStoreCannotReadOrBind() {
        assertThrows(RuntimeException.class,()->service.getShopId(99L));
        assertThrows(RuntimeException.class,()->service.bind(99L,SHOP));
        verifyNoInteractions(jdbc,mappings);
    }
    @Test void invalidIdIsRejectedBeforeWrites() {
        for(String id:Arrays.asList(null,"","abc","0","01818","-1","1.0",String.join("",Collections.nCopies(65,"1")))) {
            assertThrows(RuntimeException.class,()->service.bind(18L,id));
        }
        verifyNoInteractions(jdbc,mappings);
    }
    @Test void idUsedByOtherStoreIsRejectedGlobally() {
        when(jdbc.queryForObject(contains("store_id<>?"),eq(Integer.class),eq(SHOP),eq(18L))).thenReturn(1);
        assertThrows(RuntimeException.class,()->service.bind(18L,SHOP));
        verify(mappings,never()).insert(any(StoreShopMappingDO.class));
    }
    @Test void databaseUniquenessConflictIsTranslated() {
        when(mappings.insert(any(StoreShopMappingDO.class))).thenThrow(new DuplicateKeyException("duplicate"));
        RuntimeException error=assertThrows(RuntimeException.class,()->service.bind(18L,SHOP));
        assertTrue(error.getMessage().contains("已被绑定"));
    }
    @Test void repeatedSaveIsIdempotent() {
        for(int p:new int[]{1,2}) when(mappings.selectByStoreIdAndPlatform(18L,p)).thenReturn(StoreShopMappingDO.builder().storeId(18L).platform(p).shopId(SHOP).build());
        service.bind(18L,SHOP);
        verify(mappings,never()).insert(any(StoreShopMappingDO.class));
        verify(mappings,never()).updateById(any(StoreShopMappingDO.class));
    }
    @Test void unboundStoreCannotUseInternalIdForExternalCalls() {
        assertNull(service.getShopId(18L));
        assertThrows(RuntimeException.class,()->service.requireShopId(18L));
    }
    @Test void conflictingPlatformBindingsFailExplicitly() {
        when(mappings.selectList(any())).thenReturn(Arrays.asList(StoreShopMappingDO.builder().shopId(SHOP).build(),StoreShopMappingDO.builder().shopId("123").build()));
        assertThrows(RuntimeException.class,()->service.getShopId(18L));
    }
    @Test void existingRedemptionsPreventUnsafeRebinding() {
        when(mappings.selectList(any())).thenReturn(Collections.singletonList(StoreShopMappingDO.builder().shopId("123").build()));
        when(jdbc.queryForObject(contains("member_group_pay_info"),eq(Integer.class),eq(18L))).thenReturn(1);
        assertThrows(RuntimeException.class,()->service.bind(18L,SHOP));
        verify(mappings,never()).updateById(any(StoreShopMappingDO.class));
    }
    @Test void authorizationPrepareConsumeAndRevokeUseBoundId() {
        GroupBuyStoreBindingService bindings=mock(GroupBuyStoreBindingService.class);
        com.cloudsteward.module.member.service.iot.IotGroupPayService iot=mock(com.cloudsteward.module.member.service.iot.IotGroupPayService.class);
        when(bindings.requireShopId(18L)).thenReturn(SHOP);
        GroupPayInfoServiceImpl group=new GroupPayInfoServiceImpl();
        ReflectionTestUtils.setField(group,"groupBuyBindings",bindings);
        ReflectionTestUtils.setField(group,"iotGroupPayService",iot);
        ReflectionTestUtils.setField(group,"groupPayInfoMapper",mock(com.cloudsteward.module.member.dal.mysql.groupPay.GroupPayInfoMapper.class));
        group.prepare(18L,1,"1234567890");
        com.cloudsteward.module.member.service.iot.groupPay.IotGroupPayPrepareRespVO ticket=new com.cloudsteward.module.member.service.iot.groupPay.IotGroupPayPrepareRespVO();
        ticket.setPayAmount(100);ticket.setTicketInfo("ticket");
        group.consume(18L,1,"1234567890",ticket);group.revoke(18L,1,"1234567890","ticket");
        verify(iot).prepare(argThat(r->SHOP.equals(r.getShopId())));
        verify(iot).consume(argThat(r->SHOP.equals(r.getShopId())));
        verify(iot).revoke(argThat(r->SHOP.equals(r.getShopId())));
        com.cloudsteward.module.member.service.storeinfo.StoreInfoServiceImpl store=new com.cloudsteward.module.member.service.storeinfo.StoreInfoServiceImpl();
        com.cloudsteward.module.member.service.iot.IotService deviceIot=mock(com.cloudsteward.module.member.service.iot.IotService.class);
        when(deviceIot.groupPay()).thenReturn(true);
        ReflectionTestUtils.setField(store,"iotService",deviceIot);ReflectionTestUtils.setField(store,"iotGroupPayService",iot);ReflectionTestUtils.setField(store,"groupBuyBindings",bindings);
        com.cloudsteward.module.member.controller.app.store.vo.GroupPayAuthUrlReqVO request=new com.cloudsteward.module.member.controller.app.store.vo.GroupPayAuthUrlReqVO();request.setStoreId(18L);request.setGroupPayType(1);
        store.getGroupPayAuthUrl(request);
        verify(iot).getScopeUrl(argThat(r->SHOP.equals(r.getShopId())));
    }
}
