package com.cloudsteward.module.infra.api.config;

public interface ConfigApi {

    int updateConfigValue(String k, String v);

    String getConfigValueByKey(String key);

    void createConfig(String k,String v);

    void initFileLocalCache();


}
