package com.cloudsteward.module.member.dal.dataobject.storesound;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloudsteward.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

@TableName("member_store_sound_info")
@KeySequence("member_store_sound_info_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreSoundInfoDO extends BaseDO {
    /**
     * ID
     */
    @TableId
    private Long soundId;
    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 场地类别
     */
    private Integer roomClass;

    /**
     * 欢迎语
     */
    private String welcomeText;
    /**
     * 结束前30分钟
     */
    private String endText30;
    /**
     * 结束前5分钟
     */
    private String endText5;
    /**
     * 结束语
     */
    private String endText;
    /**
     * 深夜提醒
     */
    private String nightText;
    /**
     * 自定义播报
     */
    private String customizeText;
}
