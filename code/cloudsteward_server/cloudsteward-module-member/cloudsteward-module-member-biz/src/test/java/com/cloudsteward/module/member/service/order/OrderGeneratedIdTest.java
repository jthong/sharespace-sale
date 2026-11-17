package com.cloudsteward.module.member.service.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.cloudsteward.module.member.dal.dataobject.groupPay.GroupPayInfoDO;
import com.cloudsteward.module.member.dal.dataobject.orderinfo.OrderInfoDO;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderGeneratedIdTest {
    interface Orders extends BaseMapper<OrderInfoDO> {}
    interface Redemptions extends BaseMapper<GroupPayInfoDO> {}

    @Test void databaseGeneratedIdIsReturnedEvenWhenGlobalIdGenerationIsDisabled() throws Exception {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:order-id;MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (java.sql.Connection connection = source.getConnection()) {
            connection.createStatement().execute("create table member_order_info (order_id bigint auto_increment primary key, order_no varchar(64), create_time timestamp, update_time timestamp, creator varchar(64), updater varchar(64))");
        }
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(), source));
        GlobalConfig global = new GlobalConfig();
        global.setDbConfig(new GlobalConfig.DbConfig().setIdType(IdType.NONE));
        GlobalConfigUtils.setGlobalConfig(configuration, global);
        configuration.addMapper(Orders.class);
        try (SqlSession session = new MybatisSqlSessionFactoryBuilder().build(configuration).openSession(true)) {
            OrderInfoDO order = new OrderInfoDO(); order.setOrderNo("acceptance-test");
            assertEquals(1, session.getMapper(Orders.class).insert(order));
            assertNotNull(order.getOrderId());
            assertTrue(order.getOrderId() > 0);
        }
    }

    @Test void databaseGeneratedRedemptionIdLetsTheOrderLinkBeSaved() throws Exception {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:redemption-id;MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (java.sql.Connection connection = source.getConnection();
             java.sql.Statement statement = connection.createStatement()) {
            statement.execute("create table member_group_pay_info (id bigint auto_increment primary key, "
                    + "group_no varchar(64), order_id bigint, deleted bit default 0, "
                    + "create_time timestamp, update_time timestamp, creator varchar(64), updater varchar(64))");
        }
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(), source));
        GlobalConfig global = new GlobalConfig();
        global.setDbConfig(new GlobalConfig.DbConfig().setIdType(IdType.NONE));
        GlobalConfigUtils.setGlobalConfig(configuration, global);
        configuration.addMapper(Redemptions.class);
        try (SqlSession session = new MybatisSqlSessionFactoryBuilder().build(configuration).openSession(true)) {
            Redemptions redemptions = session.getMapper(Redemptions.class);
            GroupPayInfoDO redemption = new GroupPayInfoDO().setGroupNo("test-voucher");
            assertEquals(1, redemptions.insert(redemption));
            assertNotNull(redemption.getId());
            GroupPayInfoDO link = new GroupPayInfoDO().setId(redemption.getId()).setOrderId(7L);
            assertEquals(1, redemptions.updateById(link));
        }
    }
}
