package com.cloudsteward.module.member.service.deviceinfo;

import com.cloudsteward.framework.security.core.LoginUser;
import com.cloudsteward.module.member.controller.admin.deviceinfo.vo.DeviceInfoSyncRespVO;
import com.cloudsteward.module.member.dal.dataobject.deviceinfo.DeviceInfoDO;
import com.cloudsteward.module.member.dal.dataobject.storeinfo.StoreInfoDO;
import com.cloudsteward.module.member.dal.mysql.deviceinfo.DeviceInfoMapper;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import com.cloudsteward.module.member.service.iot.IotService;
import com.cloudsteward.module.member.service.iot.device.IotResult;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceInfoSyncTest {
    DeviceInfoServiceImpl service;
    DeviceInfoMapper devices;
    StoreInfoMapper stores;
    IotService iot;

    @BeforeEach
    void setup() {
        service = new DeviceInfoServiceImpl();
        devices = mock(DeviceInfoMapper.class);
        stores = mock(StoreInfoMapper.class);
        iot = mock(IotService.class);
        ReflectionTestUtils.setField(service, "deviceInfoMapper", devices);
        ReflectionTestUtils.setField(service, "storeInfoMapper", stores);
        ReflectionTestUtils.setField(service, "iotService", iot);
        DeviceInfoDO gateway = DeviceInfoDO.builder().deviceId(1L).deviceSn("gateway")
                .storeId(18L).type(18).build();
        gateway.setCreator("other-staff");
        when(devices.selectById(1L)).thenReturn(gateway);
        when(stores.selectById(18L)).thenReturn(new StoreInfoDO());
        LoginUser user = new LoginUser();
        user.setId(129L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
    }

    @AfterEach
    void cleanup() { SecurityContextHolder.clearContext(); }

    void reply(String data) {
        IotResult<String> result = new IotResult<>();
        result.setCode(0);
        result.setData(data);
        when(iot.getSubDevices("gateway")).thenReturn(result);
    }

    @Test
    void authorizedNonCreatorImportsToGatewayStoreWithoutBindingRoomOrResettingWifi() {
        reply("[{\"deviceNo\":\"coordinator\",\"uiid\":1800},{\"deviceNo\":\"relay\",\"uiid\":100,\"online\":1}]");
        DeviceInfoSyncRespVO result = service.syncSubDevices(1L);
        assertEquals(1, result.getAdded());
        assertEquals(1, result.getSkipped());
        ArgumentCaptor<DeviceInfoDO> inserted = ArgumentCaptor.forClass(DeviceInfoDO.class);
        verify(devices).insert(inserted.capture());
        assertEquals(18L, inserted.getValue().getStoreId());
        assertEquals("gateway", inserted.getValue().getGatewaySn());
        assertNull(inserted.getValue().getRoomId());
        assertEquals(2, inserted.getValue().getType());
        assertEquals("129", inserted.getValue().getCreator());
        verify(iot).getSubDevices("gateway");
        verifyNoMoreInteractions(iot);
    }

    @Test
    void preservesExistingRoomAndRejectsDevicesInOtherStoreOrTenant() {
        reply("[{\"deviceNo\":\"existing\",\"uiid\":101},{\"deviceNo\":\"other-store\",\"uiid\":100},{\"deviceNo\":\"other-tenant\",\"uiid\":100}]");
        when(devices.getBySn("existing")).thenReturn(Collections.singletonList(DeviceInfoDO.builder().deviceId(8L).storeId(18L).roomId(29L).type(2).build()));
        when(devices.getBySn("other-store")).thenReturn(Collections.singletonList(DeviceInfoDO.builder().storeId(19L).build()));
        when(devices.countBySN("other-tenant")).thenReturn(1);
        DeviceInfoSyncRespVO result = service.syncSubDevices(1L);
        assertEquals(1, result.getExisting());
        assertEquals(2, result.getConflicts());
        assertEquals(0, result.getAdded());
        verify(devices, never()).insert(any(DeviceInfoDO.class));
        assertEquals(29L, devices.getBySn("existing").get(0).getRoomId());
        ArgumentCaptor<DeviceInfoDO> updated = ArgumentCaptor.forClass(DeviceInfoDO.class);
        verify(devices).updateById(updated.capture());
        assertEquals("gateway", updated.getValue().getGatewaySn());
        assertEquals(1, updated.getValue().getType());
        assertNull(updated.getValue().getRoomId());
    }

    @Test
    void discoveredSpeakerIsImportedToGatewayStoreWithRoomUnbound() {
        reply("[{\"speaker\":\"{\\\"uiid\\\":1401,\\\"online\\\":1}\"}]");
        DeviceInfoSyncRespVO result = service.syncSubDevices(1L);
        assertEquals(1, result.getAdded());
        assertTrue(result.getUnsupportedUiids().isEmpty());
        ArgumentCaptor<DeviceInfoDO> inserted = ArgumentCaptor.forClass(DeviceInfoDO.class);
        verify(devices).insert(inserted.capture());
        assertEquals(3, inserted.getValue().getType());
        assertEquals(18L, inserted.getValue().getStoreId());
        assertEquals("gateway", inserted.getValue().getGatewaySn());
        assertNull(inserted.getValue().getRoomId());
    }

    @Test
    void pairingSendsOnlyGatewayCommandAndDoesNotPretendDiscoveryCompleted() {
        when(iot.pairGateway("gateway")).thenReturn(true);
        service.pairGateway(1L);
        verify(iot).pairGateway("gateway");
        verifyNoMoreInteractions(iot);
        verify(devices, never()).insert(any(DeviceInfoDO.class));
        verify(devices, never()).updateById(any(DeviceInfoDO.class));
    }

    @Test
    void pairingRejectsMissingGatewayWrongDeviceTypeAndUpstreamFailure() {
        assertThrows(RuntimeException.class, () -> service.pairGateway(99L));
        when(devices.selectById(2L)).thenReturn(DeviceInfoDO.builder().storeId(18L).type(2).build());
        assertThrows(RuntimeException.class, () -> service.pairGateway(2L));
        verifyNoInteractions(iot);
        when(iot.pairGateway("gateway")).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.pairGateway(1L));
    }

    @Test
    void invisibleGatewayAndUpstreamErrorFailExplicitly() {
        assertThrows(RuntimeException.class, () -> service.syncSubDevices(99L));
        verifyNoInteractions(iot);
        reply("invalid-json");
        assertThrows(RuntimeException.class, () -> service.syncSubDevices(1L));
        verify(devices, never()).insert(any(DeviceInfoDO.class));
    }
}
