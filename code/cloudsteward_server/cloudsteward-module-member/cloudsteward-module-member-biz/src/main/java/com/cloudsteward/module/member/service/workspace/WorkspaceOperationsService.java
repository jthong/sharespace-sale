package com.cloudsteward.module.member.service.workspace;

import com.alibaba.fastjson.JSONObject;
import com.cloudsteward.framework.common.exception.ErrorCode;
import com.cloudsteward.framework.security.core.LoginUser;
import com.cloudsteward.framework.security.core.service.SecurityFrameworkService;
import com.cloudsteward.framework.security.core.util.SecurityFrameworkUtils;
import com.cloudsteward.module.member.dal.dataobject.storeinfo.StoreInfoDO;
import com.cloudsteward.module.member.dal.mysql.storeinfo.StoreInfoMapper;
import com.cloudsteward.module.member.service.manager.AppMangerService;
import com.cloudsteward.module.member.service.storeinfo.StoreInfoService;
import com.cloudsteward.module.member.service.pkg.PkgService;
import com.cloudsteward.module.member.service.inventorydetail.InventoryDetailService;
import com.cloudsteward.module.member.service.inventorygoods.InventoryGoodsService;
import com.cloudsteward.module.member.controller.app.manager.vo.*;
import com.cloudsteward.module.member.controller.app.order.vo.OrderRenewalReqVO;
import com.cloudsteward.module.member.controller.app.store.vo.*;
import com.cloudsteward.module.member.controller.app.pkg.vo.AppAdminPkgSaveReqVO;
import com.cloudsteward.module.member.controller.app.inventory.vo.*;
import com.cloudsteward.module.member.controller.admin.user.vo.AppUserRechargeReqVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.validation.Validator;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;
import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;

/** PC operations use the same business services as mobile; all reads and IDs are store-scoped. */
@Service
public class WorkspaceOperationsService {
    @Resource private JdbcTemplate jdbc;
    @Resource private StoreInfoMapper stores;
    @Resource private SecurityFrameworkService security;
    @Resource private StoreInfoService storeService;
    @Resource private AppMangerService manager;
    @Resource private PkgService packages;
    @Resource private InventoryDetailService inventory;
    @Resource private InventoryGoodsService inventoryGoods;
    @Resource private com.cloudsteward.module.member.service.device.DeviceService devices;
    @Resource private Validator validator;
    @Resource private com.cloudsteward.module.member.service.storevipconfig.StoreVipConfigService vip;
    @Resource private com.cloudsteward.module.member.service.storeproduct.StoreProductService products;
    @Resource private com.cloudsteward.module.member.service.storeproductcategory.ProductCategoryService categories;
    @Resource private com.cloudsteward.module.member.service.productorder.ProductOrderService productOrders;
    private static final ErrorCode INVALID = new ErrorCode(1004999001, "{}");

    static final class Spec {
        final String table, id, store, columns, extra;
        Spec(String table, String id, String store, String columns, String extra) {
            this.table=table; this.id=id; this.store=store; this.columns=columns; this.extra=extra;
        }
    }
    static final Map<String, Spec> SPECS = new LinkedHashMap<>();
    static {
        add("spaces","member_room_info","room_id","room_name,room_call_name,room_class,type,price,deposit,status,qr_code,renew_code,min_hour,lead_hour,jump_clear,reserve,work_price,tongxiao_price,label,image_urls,sort_id,pre_price,pre_unit,min_charge,tx_price,morning_price,afternoon_price,night_price,xiaoshi_submit", "");
        add("orders","member_order_info","order_id","order_no,room_id,user_id,start_time,end_time,status,price,pay_price,refund_price,deposit,pay_type,renew_price", "");
        add("members","member_store_user","id","user_id,name,type,status,balance,gift_balance,total_score,vip_level,vip_blacklist", " AND t.type=11");
        add("staff","member_store_user","id","user_id,name,type,status", " AND t.type IN (12,13,14)");
        add("blacklist","member_store_user","id","user_id,name,type,vip_blacklist", " AND t.vip_blacklist=1");
        add("rules","member_discount_rules","discount_id","pay_money,gift_money,exprice_time,status", "");
        add("packages","member_pkg_info","pkg_id","pkg_name,hours,room_type,price,expire_day,max_num,enable,balance_buy,enable_room,enable_time,enable_week,enable_holiday,sort_id,mt_id,dy_id,ks_id", "");
        add("coupons","member_coupon_info","coupon_id","coupon_name,user_id,min_use_price,price,room_type,type,status,exprice_time", "");
        add("vip","member_store_vip_config","vip_id","vip_name,vip_level,vip_discount,score", "");
        add("cleaning","member_clear_info","clear_id","order_id,order_no,room_id,user_id,status,start_time,finish_time,settlement_time,complaint_desc,imgs", "");
        add("settlements","member_clear_bill","id","user_id,money,order_num,order_ids", "");
        add("payments","member_pay_order","id","order_no,order_id,user_id,order_desc,price,pay_status,pay_type,pay_time,refund_price,refund_time,goods_name", "");
        add("ledger","member_user_money_bill","id","user_id,type,money,money_type,total_money,total_gift_money,remark", "");
        add("productOrders","member_product_order","order_id","order_no,room_id,user_id,user_name,user_phone,product_info,total_num,total_price,pay_price,status,pay_time,mark", "");
        add("inventory","member_inventory_info","id","user_id,order_no,expire_time,remark", "");
        add("inventoryGoods","member_inventory_goods","id","goods_name,units,enable", "");
        add("inventoryRecords","member_inventory_record","id","inventory_id,detail_id,num", "");
        add("sounds","member_store_sound_info","sound_id","room_class,welcome_text,end_text30,end_text5,end_text,night_text,customize_text", "");
        add("platforms","member_store_shop_mapping","id","platform,shop_id", "");
        add("devices","member_device_use_info","id","user_id,room_id,cmd", "");
        SPECS.put("products", new Spec("yshop_store_product","id","shop_id","store_name,store_info,image,cate_id,price,stock,unit_name,is_show,spec_type", ""));
        SPECS.put("categories", new Spec("yshop_store_product_category","id","shop_id","name,parent_id,sort,status,pic_url,description", ""));
        add("settings","member_store_info","store_id","store_name,notice,address,kefu_phone,wifi_info,wifi_pwd,clear_open,clear_time,delay_light,douyin_poi_id,qr_code", "");
    }
    private static void add(String name,String table,String id,String columns,String extra) { SPECS.put(name,new Spec(table,id,"store_id",columns,extra)); }
    Spec spec(String resource) { Spec spec=SPECS.get(resource); if(spec==null) throw exception(INVALID,"功能不存在"); return spec; }

    public StoreInfoDO requireStore(Long storeId) {
        LoginUser user=SecurityFrameworkUtils.getLoginUser();
        if(user==null || !Integer.valueOf(2).equals(user.getUserType()) || storeId==null) throw exception(INVALID,"请先选择有权限的门店");
        StoreInfoDO store=stores.selectById(storeId);
        if(store==null || (!security.hasRole("super_admin") && !Objects.equals(store.getTenantId(),user.getTenantId()))) throw exception(INVALID,"无权访问该门店");
        return store;
    }
    public void requireRow(String resource,Long id,Long storeId) {
        requireStore(storeId); Spec spec=spec(resource);
        if(id==null || jdbc.queryForObject("SELECT COUNT(*) FROM "+spec.table+" WHERE "+spec.id+"=? AND "+spec.store+"=? AND deleted=0",Integer.class,id,storeId)!=1) throw exception(INVALID,"记录不存在或不属于当前门店");
    }
    public Map<String,Object> page(String resource,JSONObject request) {
        Long storeId=request.getLong("storeId"); requireStore(storeId); Spec spec=spec(resource);
        int page=Math.max(1,request.getIntValue("pageNo")),size=Math.max(1,Math.min(100,request.getIntValue("pageSize")==0?20:request.getIntValue("pageSize")));
        String select="t."+spec.id+","+Arrays.stream(spec.columns.split(",")).map(c->"t."+c).collect(Collectors.joining(","))+",t.create_time";
        String from=" FROM "+spec.table+" t";
        if(Arrays.asList("members","staff","blacklist").contains(resource)) { from+=" LEFT JOIN member_user u ON u.id=t.user_id AND u.deleted=0"; select+=",u.nickname,u.mobile"; }
        String where=" WHERE t."+spec.store+"=? AND t.deleted=0"+spec.extra; List<Object> args=new ArrayList<>();args.add(storeId);
        String keyword=request.getString("keyword");
        if(keyword!=null && !keyword.trim().isEmpty()) {
            if(keyword.length()>100) throw exception(INVALID,"搜索内容过长");
            List<String> fields=new ArrayList<>();fields.add("t."+spec.id);
            for(String field:spec.columns.split(",")) if(field.endsWith("name") || field.endsWith("no") || field.equals("remark") || field.equals("cmd")) fields.add("t."+field);
            if(Arrays.asList("members","staff","blacklist").contains(resource)) { fields.add("u.mobile");fields.add("u.nickname"); }
            where+=" AND ("+fields.stream().map(f->f+" LIKE ?").collect(Collectors.joining(" OR "))+")";
            for(String ignored:fields) args.add("%"+keyword.trim()+"%");
        }
        Long total=jdbc.queryForObject("SELECT COUNT(*)"+from+where,Long.class,args.toArray());
        args.add(size);args.add((page-1)*size);
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT "+select+from+where+" ORDER BY t."+spec.id+" DESC LIMIT ? OFFSET ?",args.toArray());
        List<Map<String,Object>> mapped=new ArrayList<>();
        for(Map<String,Object> row:rows) { Map<String,Object> item=new LinkedHashMap<>();row.forEach((key,value)->item.put(camel(key),value));mapped.add(item); }
        Map<String,Object> result=new LinkedHashMap<>();result.put("list",mapped);result.put("total",total);return result;
    }
    private static String camel(String key) { StringBuilder b=new StringBuilder();boolean upper=false;for(char c:key.toCharArray()) { if(c=='_')upper=true;else { b.append(upper?Character.toUpperCase(c):c);upper=false; } }return b.toString(); }
    public Map<String,Object> overview(Long storeId) {
        requireStore(storeId);Map<String,Object> result=new LinkedHashMap<>();
        result.put("todayNet",jdbc.queryForObject("SELECT COALESCE(SUM(pay_price-refund_price),0) FROM member_order_info WHERE store_id=? AND deleted=0 AND create_time>=CURDATE()",java.math.BigDecimal.class,storeId));
        result.put("todayOrders",jdbc.queryForObject("SELECT COUNT(*) FROM member_order_info WHERE store_id=? AND deleted=0 AND create_time>=CURDATE()",Long.class,storeId));
        result.put("activeOrders",jdbc.queryForObject("SELECT COUNT(*) FROM member_order_info WHERE store_id=? AND deleted=0 AND status=1",Long.class,storeId));
        result.put("availableSpaces",jdbc.queryForObject("SELECT COUNT(*) FROM member_room_info WHERE store_id=? AND deleted=0 AND status=1",Long.class,storeId));
        List<Map<String,Object>> daily=jdbc.queryForList("SELECT DATE(create_time) AS day,COALESCE(SUM(pay_price-refund_price),0) AS amount FROM member_order_info WHERE store_id=? AND deleted=0 AND create_time>=DATE_SUB(CURDATE(),INTERVAL 13 DAY) GROUP BY DATE(create_time)",storeId);
        Map<String,Object> amounts=new HashMap<>();for(Map<String,Object> row:daily)amounts.put(row.get("day").toString(),row.get("amount"));
        List<Map<String,Object>> trend=new ArrayList<>();for(int n=13;n>=0;n--){String date=java.time.LocalDate.now().minusDays(n).toString();Map<String,Object> item=new LinkedHashMap<>();item.put("date",date);item.put("amount",amounts.getOrDefault(date,java.math.BigDecimal.ZERO));trend.add(item);}result.put("trend",trend);return result;
    }
    private <T> T valid(JSONObject data,Class<T> type) { T bean=data.toJavaObject(type);Set<ConstraintViolation<T>> errors=validator.validate(bean);if(!errors.isEmpty())throw new ConstraintViolationException(errors);return bean; }
    private Long id(JSONObject data,String key) { Long value=data.getLong(key);if(value==null)throw exception(INVALID,"缺少记录编号");return value; }
    private void member(Long storeId,Long userId) { if(userId==null || jdbc.queryForObject("SELECT COUNT(*) FROM member_store_user WHERE store_id=? AND user_id=? AND deleted=0",Integer.class,storeId,userId)==0)throw exception(INVALID,"用户不属于当前门店"); }

    @Transactional(rollbackFor=Exception.class)
    public Object save(String resource,JSONObject data) {
        Long storeId=data.getLong("storeId");requireStore(storeId);
        for(String key:Arrays.asList("payMoney","giftMoney","price","minUsePrice"))if(data.containsKey(key)&&(data.getBigDecimal(key)==null||data.getBigDecimal(key).signum()<0))throw exception(INVALID,"金额不能为负数");
        if(data.getJSONArray("enableRoom")!=null)for(Object room:data.getJSONArray("enableRoom"))requireRow("spaces",Long.valueOf(room.toString()),storeId);
        switch(resource) {
            case "spaces": if(data.getLong("roomId")!=null)requireRow(resource,data.getLong("roomId"),storeId);storeService.saveRoomDetail(valid(data,AppRoomDetailReqVO.class));break;
            case "rules": if(data.getLong("discountId")!=null)requireRow(resource,data.getLong("discountId"),storeId);storeService.saveDiscountRuleDetail(valid(data,AppDiscountRulesDetailReqVO.class));break;
            case "packages": if(data.getLong("pkgId")!=null)requireRow(resource,data.getLong("pkgId"),storeId);packages.saveAdminPkg(valid(data,AppAdminPkgSaveReqVO.class));break;
            case "coupons": if(data.getLong("couponId")!=null)requireRow(resource,data.getLong("couponId"),storeId);manager.saveCouponDetail(valid(data,AppCouponDetailReqVO.class));break;
            case "staff": if("cleaner".equals(data.getString("role"))) manager.saveClearUser(valid(data,AppClearUserDetailReqVO.class));else manager.saveAdminUser(valid(data,AppClearUserDetailReqVO.class));break;
            case "sounds": storeService.saveStoreSoundInfo(valid(data,AppStoreSoundInfoReqVO.class));break;
            case "inventoryGoods": if(data.getLong("id")!=null)requireRow(resource,data.getLong("id"),storeId);inventoryGoods.saveGoods(valid(data,InventoryGoodsSaveReqVO.class));break;
            case "vip": if(data.getLong("vipId")!=null)requireRow(resource,data.getLong("vipId"),storeId);vip.saveVipConfig(valid(data,AppStoreVipConfigSaveReqVO.class));break;
            case "categories":
                data.put("shopId",storeId);if(data.getLong("parentId")!=null && data.getLong("parentId")>0)requireRow(resource,data.getLong("parentId"),storeId);
                if(data.getLong("id")==null)categories.createCategory(valid(data,com.cloudsteward.module.member.controller.app.storeproductcategory.vo.ProductCategoryCreateReqVO.class));
                else {requireRow(resource,data.getLong("id"),storeId);categories.updateCategory(valid(data,com.cloudsteward.module.member.controller.app.storeproductcategory.vo.ProductCategoryUpdateReqVO.class));}break;
            case "products": saveProduct(data,storeId);break;
            case "settings":
                String[] fields={"notice","kefuPhone","address","wifiInfo","wifiPwd"};
                Map<String,String> columns=new LinkedHashMap<>();columns.put("notice","notice");columns.put("kefuPhone","kefu_phone");columns.put("address","address");columns.put("wifiInfo","wifi_info");columns.put("wifiPwd","wifi_pwd");
                List<Object> values=new ArrayList<>();List<String> updates=new ArrayList<>();
                for(String field:fields)if(data.containsKey(field)) { String value=data.getString(field);if(value!=null&&value.length()>4000)throw exception(INVALID,"内容过长");updates.add(columns.get(field)+"=?");values.add(value); }
                if(!updates.isEmpty()){values.add(storeId);jdbc.update("UPDATE member_store_info SET "+String.join(",",updates)+",update_time=NOW() WHERE store_id=? AND deleted=0",values.toArray());}break;
            default: throw exception(INVALID,"此功能不支持直接保存");
        }
        return true;
    }
    @Transactional(rollbackFor=Exception.class)
    public Object action(String action,JSONObject data) {
        Long storeId=data.getLong("storeId");requireStore(storeId);
        Long orderId=data.getLong("orderId"),roomId=data.getLong("roomId");
        if(orderId!=null)requireRow("orders",orderId,storeId);
        if(roomId!=null)requireRow("spaces",roomId,storeId);
        switch(action) {
            case "submitOrder": requireRow("spaces",id(data,"roomId"),storeId);return manager.submitOrder(valid(data,OrderSubmitReqVO.class));
            case "cancelOrder": manager.cancelOrder(id(data,"orderId"),false);break;
            case "refundOrder": manager.cancelOrder(id(data,"orderId"),true);break;
            case "depositRefund": manager.depositRefund(id(data,"orderId"));break;
            case "changeOrder": manager.changeOrder(valid(data,OrderChangeReqVO.class));break;
            case "renewOrder": manager.renew(valid(data,OrderRenewalReqVO.class));break;
            case "changeOrderUser": manager.changeOrderUser(valid(data,AppChangeOrderUserReqVO.class));break;
            case "openSpace": requireRow("spaces",id(data,"roomId"),storeId);devices.openRoomDoor(SecurityFrameworkUtils.getLoginUserId(),storeId,roomId,2);break;
            case "closeSpace": requireRow("spaces",id(data,"roomId"),storeId);devices.closeRoomDoor(SecurityFrameworkUtils.getLoginUserId(),storeId,roomId,2,0);break;
            case "disableSpace": requireRow("spaces",id(data,"roomId"),storeId);storeService.disableRoom(roomId);break;
            case "finishSpace": requireRow("spaces",id(data,"roomId"),storeId);storeService.finishRoomOrder(roomId);break;
            case "cleanSpace": requireRow("spaces",id(data,"roomId"),storeId);storeService.clearAndFinish(roomId);break;
            case "cancelCleaning": requireRow("cleaning",id(data,"clearId"),storeId);manager.cancelClear(data.getLong("clearId"));break;
            case "settleCleaner": member(storeId,data.getLong("userId"));manager.settlementClearUser(valid(data,AppSettlementClearUserReqVO.class));break;
            case "recharge": member(storeId,data.getLong("userId")); if(data.getBigDecimal("money")==null || data.getBigDecimal("money").signum()<=0)throw exception(INVALID,"充值金额必须大于零");manager.recharge(valid(data,AppUserRechargeReqVO.class));break;
            case "giftCoupon": requireRow("coupons",id(data,"couponId"),storeId);member(storeId,data.getLong("userId"));manager.giftCoupon(valid(data,AppGiftCouponReqVO.class));break;
            case "removeBlacklist": requireRow("blacklist",id(data,"id"),storeId);storeService.removeBlackList(data.getLong("id"));break;
            case "addBlacklist": storeService.addBlackList(valid(data,AppAddBlackListVO.class));break;
            case "enablePackage": requireRow("packages",id(data,"pkgId"),storeId);packages.enable(data.getLong("pkgId"));break;
            case "deletePackage": requireRow("packages",id(data,"pkgId"),storeId);packages.delete(data.getLong("pkgId"));break;
            case "deleteCoupon": requireRow("coupons",id(data,"couponId"),storeId);manager.deleteCoupon(data.getLong("couponId"));break;
            case "toggleRule": requireRow("rules",id(data,"discountId"),storeId);storeService.changeDiscountRulesStatus(data.getLong("discountId"));break;
            case "deleteInventoryGoods": requireRow("inventoryGoods",id(data,"id"),storeId);inventoryGoods.deleteGoods(data.getLong("id"));break;
            case "deleteVip": requireRow("vip",id(data,"vipId"),storeId);vip.deleteVipConfig(data.getLong("vipId"));break;
            case "editMemberVip": member(storeId,data.getLong("userId"));vip.editMemberVip(valid(data,AppEditMemberVipReqVO.class));break;
            case "deleteStaff":
                requireRow("staff",id(data,"id"),storeId);member(storeId,data.getLong("userId"));
                if(data.getIntValue("type")==14)manager.deleteClearUser(storeId,data.getLong("userId"));else manager.deleteAdminUser(storeId,data.getLong("userId"));break;
            case "deleteCategory": requireRow("categories",id(data,"id"),storeId);categories.deleteCategory(data.getLong("id"));break;
            case "deleteProduct": requireRow("products",id(data,"id"),storeId);products.deleteStoreProduct(data.getLong("id"));break;
            case "toggleProduct": requireRow("products",id(data,"id"),storeId);products.onSale(data.getLong("id"),data.getIntValue("isShow")==1?0:1);break;
            case "finishProductOrder": requireRow("productOrders",id(data,"productOrderId"),storeId);productOrders.finishOrder(data.getLong("productOrderId"));break;
            case "refundPayment": requireRow("payments",id(data,"id"),storeId);if(data.getIntValue("price")<=0)throw exception(INVALID,"退款金额必须大于零");manager.refundPayOrder(valid(data,AppPayRefundReqVO.class));break;
            case "addInventory": validateGoods(data,storeId);inventory.addInventory(valid(data,AppAddInventoryReqVO.class));break;
            case "takeInventory": validateGoods(data,storeId);inventory.takeInventory(valid(data,AppTakeInventoryReqVO.class));break;
            case "resetQrcode": storeService.resetQrcode(storeId);break;
            case "setDouyinId": storeService.setDouyinId(storeId,data.getString("dyId"));break;
            case "groupAuth": return storeService.getGroupPayAuthUrl(valid(data,GroupPayAuthUrlReqVO.class));
            default: throw exception(INVALID,"不支持的操作");
        }
        return true;
    }
    private void validateGoods(JSONObject data,Long storeId) {
        if(data.getJSONArray("goodsList")==null || data.getJSONArray("goodsList").isEmpty())throw exception(INVALID,"请选择寄存商品");
        for(Object item:data.getJSONArray("goodsList")) { JSONObject goods=(JSONObject)item;requireRow("inventoryGoods",goods.getLong("goodsId"),storeId);if(goods.getIntValue("num")<1||goods.getIntValue("num")>99999)throw exception(INVALID,"商品数量不正确"); }
    }
    private void saveProduct(JSONObject data,Long storeId) {
        if(data.getLong("id")!=null) {
            requireRow("products",data.getLong("id"),storeId);
            if(products.getStoreProduct(data.getLong("id")).getSpecType()==1)throw exception(INVALID,"多规格商品请在手机端编辑，避免覆盖已有规格");
        }
        requireRow("categories",data.getLong("cateId"),storeId);
        String image=data.getString("image"),name=data.getString("storeName");
        if(name==null||name.trim().isEmpty()||image==null||!image.startsWith("https://"))throw exception(INVALID,"请填写商品名称和有效的 HTTPS 图片地址");
        if(data.getBigDecimal("price")==null||data.getBigDecimal("price").signum()<0||data.getIntValue("stock")<0)throw exception(INVALID,"价格和库存不能为负数");
        com.cloudsteward.module.member.service.storeproduct.dto.StoreProductDto product=data.toJavaObject(com.cloudsteward.module.member.service.storeproduct.dto.StoreProductDto.class);
        product.setShopId(storeId.intValue());product.setSpecType(0);product.setSliderImage(Collections.singletonList(image));product.setIsPostage(1);product.setPostage(0d);
        com.cloudsteward.module.member.service.storeproduct.dto.ProductFormatDto sku=new com.cloudsteward.module.member.service.storeproduct.dto.ProductFormatDto();
        sku.setPrice(data.getDouble("price"));sku.setStock(data.getIntValue("stock"));sku.setPic(image);sku.setOtPrice(data.getDouble("price"));
        product.setAttrs(Collections.singletonList(sku));products.insertAndEditYxStoreProduct(product);
    }
}
