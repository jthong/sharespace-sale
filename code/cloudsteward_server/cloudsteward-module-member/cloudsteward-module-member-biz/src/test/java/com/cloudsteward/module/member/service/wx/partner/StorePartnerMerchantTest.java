package com.cloudsteward.module.member.service.wx.partner;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import com.cloudsteward.module.member.dal.dataobject.storeinfo.StoreInfoDO;
import org.junit.jupiter.api.*;
import com.cloudsteward.framework.security.core.LoginUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class StorePartnerMerchantTest {
 @BeforeEach void login(){LoginUser user=new LoginUser();user.setId(1L);user.setUserType(2);SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null));}
 @AfterEach void logout(){SecurityContextHolder.clearContext();}
 @Test void memberIdentityCannotChangeReceivingMerchant(){
  LoginUser user=new LoginUser();user.setId(1L);user.setUserType(1);SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null));
  StorePartnerMerchantService service=new StorePartnerMerchantService();assertThrows(RuntimeException.class,()->service.bind(18L,"1109667212"));
 }

 @Test void oneMerchantPerStoreCanBeReadAndUpdated(){
  JdbcTemplate db=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:merchant;MODE=MySQL;DB_CLOSE_DELAY=-1","sa",""));db.execute("CREATE TABLE member_store_info(store_id bigint primary key,deleted int)");db.execute("INSERT INTO member_store_info VALUES(18,0)");db.execute("CREATE TABLE member_store_partner_merchant(store_id bigint primary key,sub_mch_id varchar(32),update_time timestamp)");
  StoreInfoMapper stores=mock(StoreInfoMapper.class);when(stores.selectById(18L)).thenReturn(new StoreInfoDO());StorePartnerMerchantService service=new StorePartnerMerchantService();ReflectionTestUtils.setField(service,"jdbc",db);ReflectionTestUtils.setField(service,"stores",stores);
  assertNull(service.get(18L));service.bind(18L,"1109667212");assertEquals("1109667212",service.get(18L));service.bind(18L,"1109667213");assertEquals("1109667213",service.get(18L));assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM member_store_partner_merchant",Integer.class));
 }
 @Test void unavailableStoreCannotReadOrChangeMerchant(){
  StorePartnerMerchantService service=new StorePartnerMerchantService();StoreInfoMapper stores=mock(StoreInfoMapper.class);JdbcTemplate db=mock(JdbcTemplate.class);ReflectionTestUtils.setField(service,"jdbc",db);ReflectionTestUtils.setField(service,"stores",stores);
  assertThrows(RuntimeException.class,()->service.get(99L));assertThrows(RuntimeException.class,()->service.bind(99L,"1109667212"));verifyNoInteractions(db);
 }
 @Test void merchantIdValidationPreventsEmptyOrNonNumericValues(){
  StorePartnerMerchantService service=new StorePartnerMerchantService();StoreInfoMapper stores=mock(StoreInfoMapper.class);when(stores.selectById(18L)).thenReturn(new StoreInfoDO());JdbcTemplate db=mock(JdbcTemplate.class);ReflectionTestUtils.setField(service,"jdbc",db);ReflectionTestUtils.setField(service,"stores",stores);
  for(String value:new String[]{null,"","merchant","123"})assertThrows(RuntimeException.class,()->service.bind(18L,value));verifyNoInteractions(db);
 }
}
