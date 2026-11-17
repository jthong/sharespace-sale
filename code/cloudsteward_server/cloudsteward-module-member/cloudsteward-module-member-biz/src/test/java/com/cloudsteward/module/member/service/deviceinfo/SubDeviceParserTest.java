package com.cloudsteward.module.member.service.deviceinfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SubDeviceParserTest {
    @Test
    void decodesGatewayMacKeyedAndMultiplyEncodedPayload() {
        JSONArray array = new JSONArray();
        String[] sns = {"9548c7fefffa317c", "38203679dc38c1a4", "bc32a3299038c1a4",
                "79f1dd432738c1a4", "636cca057238c1a4", "b5b69335f338c1a4",
                "472c90f6d838c1a4", "6baf1f4fbd38c1a4", "5b96fd910138c1a4", "7b43916a8738c1a4"};
        int[] uiids = {1800, 100, 101, 101, 100, 100, 101, 100, 100, 100};
        for (int i = 0; i < sns.length; i++) {
            JSONObject state = new JSONObject();
            state.put("online", 1);
            state.put("uiid", uiids[i]);
            JSONObject item = new JSONObject();
            item.put(sns[i], JSON.toJSONString(state).replace("\"", "\\\""));
            array.add(item);
        }
        List<SubDeviceParser.Device> devices = SubDeviceParser.parse(JSON.toJSONString(JSON.toJSONString(array)));
        assertEquals(10, devices.size());
        assertEquals(18, devices.get(0).getType());
        assertEquals(6, devices.stream().filter(d -> Integer.valueOf(2).equals(d.getType())).count());
        assertEquals(3, devices.stream().filter(d -> Integer.valueOf(1).equals(d.getType())).count());
        assertEquals("bc32a3299038c1a4", devices.get(2).getSn());
        assertEquals(1, devices.get(2).getOnline());
    }

    @Test
    void leadingZerosUnknownTypesAndDuplicateSerials() {
        List<SubDeviceParser.Device> devices = SubDeviceParser.parse("[{\"deviceNo\":\"a\",\"uiid\":\"0100\"},{\"deviceNo\":\"a\",\"uiid\":\"0101\",\"online\":1},{\"deviceNo\":\"b\",\"uiid\":1400}]");
        assertEquals(2, devices.size());
        assertEquals(1, devices.get(0).getType());
        assertNull(devices.get(1).getType());
    }

    @Test
    void recognizesDiscoveredMp3SpeakerWithoutGuessingOtherHardwareTypes() {
        List<SubDeviceParser.Device> devices = SubDeviceParser.parse("[{\"speaker\":\"{\\\"uiid\\\":1401,\\\"online\\\":1}\"},{\"deviceNo\":\"unknown\",\"uiid\":1900}]");
        assertEquals(3, devices.get(0).getType());
        assertEquals(1, devices.get(0).getOnline());
        assertNull(devices.get(1).getType());
    }

    @Test
    void rejectsMalformedOrNullPayloadInsteadOfReportingSuccess() {
        assertThrows(IllegalArgumentException.class, () -> SubDeviceParser.parse("not-json"));
        assertThrows(IllegalArgumentException.class, () -> SubDeviceParser.parse("null"));
        assertThrows(IllegalArgumentException.class, () -> SubDeviceParser.parse("{}"));
        assertTrue(SubDeviceParser.parse("[]").isEmpty());
    }
}
