package com.cloudsteward.module.member.dal.mysql.holiday;

import com.cloudsteward.framework.mybatis.core.mapper.BaseMapperX;
import com.cloudsteward.framework.tenant.core.aop.TenantIgnore;
import com.cloudsteward.module.member.dal.dataobject.holiday.HolidayDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HolidayMapper extends BaseMapperX<HolidayDo> {

    @TenantIgnore
    int isHoliday(@Param("time") String time);
}
