package com.cloudsteward.module.member.service.deviceinfo;

import com.cloudsteward.module.member.controller.admin.deviceinfo.vo.DeviceControlReqVO;
import com.cloudsteward.module.member.dal.dataobject.deviceinfo.DeviceInfoDO;
import com.cloudsteward.module.member.dal.mysql.deviceinfo.DeviceInfoMapper;
import com.cloudsteward.module.member.dal.mysql.deviceuseinfo.DeviceUseInfoMapper;
import com.cloudsteward.module.member.forest.IotDeviceClient;
import com.cloudsteward.module.member.service.device.DeviceServiceImpl;
import com.cloudsteward.module.member.service.iot.IotService;
import com.cloudsteward.module.member.service.iot.device.IotResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceControlTest {
    @Test void optionalMainDoorDoesNotPreventRoomDoorAndPowerCommands() {
        DeviceInfoMapper devices = mock(DeviceInfoMapper.class);
        DeviceUseInfoMapper usage = mock(DeviceUseInfoMapper.class);
        IotDeviceClient client = mock(IotDeviceClient.class);
        IotService iot = spy(new IotService());
        ReflectionTestUtils.setField(iot, "deviceInfoMapper", devices);
        ReflectionTestUtils.setField(iot, "iotDeviceClient", client);
        doReturn("test-client").when(iot).clientId();
        doReturn("test-secret").when(iot).secret();
        IotResult<String> response = new IotResult<>();
        response.setCode(0);
        when(client.control(any(), anyString(), anyString())).thenReturn(response);

        DeviceServiceImpl service = new DeviceServiceImpl();
        ReflectionTestUtils.setField(service, "deviceInfoMapper", devices);
        ReflectionTestUtils.setField(service, "deviceUseInfoMapper", usage);
        ReflectionTestUtils.setField(service, "iotService", iot);
        DeviceInfoDO door = DeviceInfoDO.builder().deviceSn("room-door").gatewaySn("gateway").type(1).build();
        DeviceInfoDO power = DeviceInfoDO.builder().deviceSn("room-power").gatewaySn("gateway").type(2).build();
        when(devices.getByRoomId(35L)).thenReturn(Arrays.asList(door, power));
        when(devices.getBySn("room-door")).thenReturn(Collections.singletonList(door));
        when(devices.getBySn("room-power")).thenReturn(Collections.singletonList(power));
        when(devices.getByStoreIdAndType(eq(18L), any(Integer[].class))).thenReturn(Collections.emptyList());

        service.openStoreDoor(445L, 18L, 1);
        verifyNoInteractions(client, usage);
        service.openRoomDoor(445L, 18L, 35L, 1);
        verify(client).control(argThat(req -> "room-door".equals(req.getDeviceNo())
                && "gateway".equals(req.getGatewayNo()) && req.getType() == 70 && req.getOutlet() == 0),
                eq("test-client"), eq("test-secret"));
        verify(client).control(argThat(req -> "room-power".equals(req.getDeviceNo())
                && "gateway".equals(req.getGatewayNo()) && req.getType() == 70 && req.getOutlet() == 0),
                eq("test-client"), eq("test-secret"));
        verifyNoMoreInteractions(client);
        verify(usage).insert(argThat(record -> Long.valueOf(35L).equals(record.getRoomId())));

        DeviceInfoDO entrance = DeviceInfoDO.builder().deviceSn("store-door").gatewaySn("gateway").type(1).build();
        when(devices.getByStoreIdAndType(eq(18L), any(Integer[].class))).thenReturn(Collections.singletonList(entrance));
        when(devices.getBySn("store-door")).thenReturn(Collections.singletonList(entrance));
        service.openStoreDoor(445L, 18L, 1);
        verify(client).control(argThat(req -> "store-door".equals(req.getDeviceNo())
                && "gateway".equals(req.getGatewayNo()) && req.getType() == 70 && req.getOutlet() == 0),
                eq("test-client"), eq("test-secret"));
    }

    @Test void gatewayPairingUses201AndReportsPlatformRejection() {
        IotService service = spy(new IotService());
        IotDeviceClient client = mock(IotDeviceClient.class);
        ReflectionTestUtils.setField(service, "iotDeviceClient", client);
        doReturn("test-client").when(service).clientId();
        doReturn("test-secret").when(service).secret();
        IotResult<String> result = new IotResult<>(); result.setCode(0);
        when(client.control(any(), anyString(), anyString())).thenReturn(result);
        assertTrue(service.pairGateway("gateway"));
        ArgumentCaptor<com.cloudsteward.module.member.service.iot.device.DeviceControlReqVO> sent = ArgumentCaptor.forClass(com.cloudsteward.module.member.service.iot.device.DeviceControlReqVO.class);
        verify(client).control(sent.capture(), eq("test-client"), eq("test-secret"));
        assertEquals(201, sent.getValue().getType());
        assertEquals("gateway", sent.getValue().getGatewayNo());
        assertNull(sent.getValue().getDeviceNo());
        assertNull(sent.getValue().getOutlet());
        result.setCode(1); result.setMsg("rejected");
        assertThrows(RuntimeException.class, () -> service.pairGateway("gateway"));
    }

    @Test void manualOnAndOffSendDifferentCommandsAndRejectUnknownCommands() {
        DeviceInfoMapper devices = mock(DeviceInfoMapper.class);
        IotService iot = mock(IotService.class);
        DeviceInfoServiceImpl service = new DeviceInfoServiceImpl();
        ReflectionTestUtils.setField(service, "deviceInfoMapper", devices);
        ReflectionTestUtils.setField(service, "iotService", iot);
        when(devices.selectById(229L)).thenReturn(DeviceInfoDO.builder().deviceId(229L).deviceSn("relay").type(2).build());
        when(iot.control(any())).thenReturn(true);
        DeviceControlReqVO req = new DeviceControlReqVO(); req.setDeviceId(229L);
        req.setCmd("on"); service.control(req);
        req.setCmd("off"); service.control(req);
        req.setCmd("unknown"); assertThrows(RuntimeException.class, () -> service.control(req));
        ArgumentCaptor<com.cloudsteward.module.member.service.iot.device.DeviceControlReqVO> sent = ArgumentCaptor.forClass(com.cloudsteward.module.member.service.iot.device.DeviceControlReqVO.class);
        verify(iot, times(2)).control(sent.capture());
        assertEquals(70, sent.getAllValues().get(0).getType());
        assertEquals(71, sent.getAllValues().get(1).getType());
        assertEquals(0, sent.getValue().getOutlet());
    }

    @Test void everyControlUsesPersistedDiscoveryGatewayAndFailsClosedWithoutIt() {
        IotService service = spy(new IotService());
        DeviceInfoMapper devices = mock(DeviceInfoMapper.class);
        IotDeviceClient client = mock(IotDeviceClient.class);
        ReflectionTestUtils.setField(service, "deviceInfoMapper", devices);
        ReflectionTestUtils.setField(service, "iotDeviceClient", client);
        doReturn("test-client").when(service).clientId();
        doReturn("test-secret").when(service).secret();
        IotResult<String> result = new IotResult<>(); result.setCode(0);
        when(client.control(any(), anyString(), anyString())).thenReturn(result);
        DeviceInfoDO relay = DeviceInfoDO.builder().type(2).gatewaySn("discovered-gateway").build();
        when(devices.getBySn("relay")).thenReturn(Collections.singletonList(relay));
        com.cloudsteward.module.member.service.iot.device.DeviceControlReqVO req = new com.cloudsteward.module.member.service.iot.device.DeviceControlReqVO();
        req.setDeviceNo("relay"); req.setGatewayNo("wrong-gateway"); req.setType(71);
        assertTrue(service.control(req));
        assertEquals("discovered-gateway", req.getGatewayNo());
        relay.setGatewaySn(null);
        assertThrows(RuntimeException.class, () -> service.control(req));
        relay.setGatewaySn("one");
        when(devices.getBySn("relay")).thenReturn(Arrays.asList(relay, DeviceInfoDO.builder().gatewaySn("two").build()));
        assertThrows(RuntimeException.class, () -> service.control(req));
        verify(client, times(1)).control(any(), anyString(), anyString());
    }
}
