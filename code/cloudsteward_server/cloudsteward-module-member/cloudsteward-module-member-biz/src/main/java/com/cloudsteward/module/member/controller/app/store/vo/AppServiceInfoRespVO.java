package com.cloudsteward.module.member.controller.app.store.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

import static com.cloudsteward.framework.common.util.date.DateUtils.*;

@Data
public class AppServiceInfoRespVO {


    private String name;

    //1租户 2门店 3团购
    private Integer type;

    //过期时间
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY, timezone = TIME_ZONE_DEFAULT)
    private Date expireDate;

    private Boolean show;


}
