package com.cloudsteward.server;

import com.dtflys.forest.springboot.annotation.ForestScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/**
 * 项目的启动类
 * <p>
 */
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${cloudsteward.info.base-package}
@SpringBootApplication(scanBasePackages = {"${cloudsteward.info.base-package}.server", "${cloudsteward.info.base-package}.module"})
@ForestScan(basePackages = "com.cloudsteward.module.member.forest")
public class CloudStewardServerApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(CloudStewardServerApplication.class, args);
    }
}
