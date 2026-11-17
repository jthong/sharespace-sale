package com.cloudsteward.module.member.service.wx.partner;

import com.cloudsteward.framework.common.exception.ErrorCode;
import com.cloudsteward.framework.security.core.LoginUser;
import com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;
import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
public class StorePartnerMerchantService {
    @Resource private StoreInfoMapper stores;
    @Resource private JdbcTemplate jdbc;
    private void authorize(Long storeId) {
        if (storeId==null || stores.selectById(storeId)==null) throw exception(new ErrorCode(1004999011,"门店不存在或无权访问"));
    }
    public String get(Long storeId) {
        authorize(storeId);
        List<String> rows=jdbc.queryForList("SELECT sub_mch_id FROM member_store_partner_merchant WHERE store_id=?",String.class,storeId);
        return rows.isEmpty()?null:rows.get(0);
    }
    public String require(Long storeId) {
        String mch=get(storeId);if(mch==null)throw exception(new ErrorCode(1004999011,"请先在门店管理配置微信特约商户号"));return mch;
    }
    @Transactional(rollbackFor=Exception.class)
    public void bind(Long storeId,String merchant) {
        LoginUser user=SecurityFrameworkUtils.getLoginUser();
        if(user==null || !Integer.valueOf(2).equals(user.getUserType())) throw exception(new ErrorCode(1004999011,"请使用有门店管理权限的后台账号配置收款商户"));
        authorize(storeId);String value=merchant==null?"":merchant.trim();
        if (!value.matches("[0-9]{8,32}")) throw exception(new ErrorCode(1004999011,"微信特约商户号须为8至32位数字"));
        jdbc.queryForObject("SELECT store_id FROM member_store_info WHERE store_id=? AND deleted=0 FOR UPDATE",Long.class,storeId);
        jdbc.update("INSERT INTO member_store_partner_merchant(store_id,sub_mch_id) VALUES(?,?) ON DUPLICATE KEY UPDATE sub_mch_id=VALUES(sub_mch_id),update_time=CURRENT_TIMESTAMP",storeId,value);
    }
}
