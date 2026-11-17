package com.cloudsteward.module.member.service.deviceinfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudsteward.module.member.enums.AppEnum;
import lombok.Value;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Decodes the IoT gateway's JSON-string array and MAC-keyed device payloads. */
public final class SubDeviceParser {
    private SubDeviceParser() { }

    @Value
    public static class Device {
        String sn;
        Integer uiid;
        Integer type;
        Integer online;
    }

    public static List<Device> parse(String data) {
        Object decoded = decode(data);
        if (!(decoded instanceof JSONArray)) {
            throw new IllegalArgumentException("子设备返回格式错误，应为设备数组");
        }
        Map<String, Device> devices = new LinkedHashMap<>();
        for (Object entry : (JSONArray) decoded) {
            Object item = decode(entry);
            if (!(item instanceof JSONObject)) {
                throw new IllegalArgumentException("子设备数据格式错误");
            }
            JSONObject object = (JSONObject) item;
            if (object.containsKey("uiid")) {
                String sn = object.getString("deviceNo");
                if (sn == null) sn = object.getString("deviceId");
                if (sn == null) sn = object.getString("sn");
                add(devices, sn, object);
            } else {
                for (String sn : object.keySet()) {
                    Object state = decode(object.get(sn));
                    if (!(state instanceof JSONObject)) {
                        throw new IllegalArgumentException("子设备状态格式错误");
                    }
                    add(devices, sn, (JSONObject) state);
                }
            }
        }
        return new ArrayList<>(devices.values());
    }

    private static void add(Map<String, Device> devices, String sn, JSONObject state) {
        if (sn == null || sn.trim().isEmpty()) {
            throw new IllegalArgumentException("子设备编号缺失");
        }
        Integer uiid = null;
        try {
            if (state.get("uiid") != null) uiid = Integer.valueOf(state.get("uiid").toString());
        } catch (NumberFormatException ignored) {
            // An unknown UIID is reported as skipped, never guessed from a control type.
        }
        Integer type = null;
        if (Integer.valueOf(100).equals(uiid)) {
            type = AppEnum.device_type.ELECTRIC.getValue();
        } else if (Integer.valueOf(101).equals(uiid)) {
            // Confirmed hardware profile: access switch, protocol section 3.10 (0101).
            type = AppEnum.device_type.DOOR.getValue();
        } else if (Integer.valueOf(1401).equals(uiid)) {
            type = AppEnum.device_type.SOUND.getValue();
        } else if (Integer.valueOf(1800).equals(uiid)) {
            type = AppEnum.device_type.CLOUDSTEWARD_GATEWAY.getValue();
        }
        Integer online = state.getInteger("online");
        devices.put(sn.trim(), new Device(sn.trim(), uiid, type,
                Integer.valueOf(1).equals(online) ? 1 : 0));
    }

    private static Object decode(Object value) {
        for (int depth = 0; depth < 12 && value instanceof String; depth++) {
            String text = ((String) value).trim();
            if (text.isEmpty()) throw new IllegalArgumentException("子设备数据为空");
            try {
                value = JSON.parse(text);
            } catch (RuntimeException ex) {
                // Some gateways escape the object again without surrounding quotes.
                try {
                    value = JSON.parse("\"" + text + "\"");
                } catch (RuntimeException invalid) {
                    throw new IllegalArgumentException("子设备 JSON 无法解析", invalid);
                }
            }
        }
        return value;
    }
}
