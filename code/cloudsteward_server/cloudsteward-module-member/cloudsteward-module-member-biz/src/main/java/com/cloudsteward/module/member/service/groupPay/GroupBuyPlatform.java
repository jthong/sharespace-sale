package com.cloudsteward.module.member.service.groupPay;

import com.cloudsteward.framework.common.exception.ErrorCode;
import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;

public final class GroupBuyPlatform {
    private GroupBuyPlatform() { }

    public static String normalize(String code) {
        String value = code == null ? "" : code.trim();
        // Keep URL contents intact; tolerate spaces in manually entered plain codes.
        return value.regionMatches(true, 0, "https", 0, 5) ? value : value.replaceAll("\\s+", "");
    }

    public static Integer detect(String code) {
        String value = normalize(code);
        if (value.regionMatches(true, 0, "https", 0, 5) || value.length() == 15) return 2;
        if (value.length() == 10 || value.length() == 13) return 1;
        throw exception(new ErrorCode(1004999003, "无法识别团购券：抖音支持 https 开头或15位券码，美团支持10位或13位券码"));
    }
    public static Integer require(Integer platform) {
        if (!Integer.valueOf(1).equals(platform) && !Integer.valueOf(2).equals(platform)) {
            throw exception(new ErrorCode(1004999003, "团购平台只支持美团或抖音"));
        }
        return platform;
    }
}
