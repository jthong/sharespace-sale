// The same form descriptions drive accessible labels, validation and request field selection.
export const field = (key, label, type = 'text', extra = {}) => ({ key, label, type, required: true, ...extra })
const optional = (key, label, type = 'text', extra = {}) => field(key, label, type, { required: false, ...extra })
const money = (key, label) => field(key, label, 'money')
const num = (key, label, value = 0) => field(key, label, 'number', { default: value })
const date = (key, label) => field(key, label, 'datetime')
const pick = (key, label, options, value) => field(key, label, 'select', { options, default: value })
const bool = (key, label, value = true) => field(key, label, 'boolean', { default: value })
const end = date('endTime', '结束时间')
const start = date('startTime', '开始时间')
const phone = field('mobile', '客户手机号', 'phone')
const orderStates = { 0: '未开始', 1: '进行中', 2: '已完成', 3: '已取消' }
export const spaceStates = { 0: '已停用', 1: '空闲', 2: '待保洁', 3: '使用中', 4: '已预约' }
const columns = text => text.split(',').map(part => { const [key, label] = part.split(':'); return { key, label } })
const action = (key, label, fields = [], extra = {}) => ({ key, label, fields, ...extra })
export const actions = {
  submitOrder: action('submitOrder', '代客预约', [field('roomId', '预约空间', 'space'), phone, start, end, money('price', '计入统计金额（元）')], { help: '此操作创建管理员预约，不会自动向客户扣款。金额仅计入经营统计。' }),
  addBlacklist: action('addBlacklist', '加入黑名单', [field('phone', '用户手机号', 'phone')]),
  addInventory: action('addInventory', '办理寄存', [field('phone', '客户手机号', 'phone'), date('expireTime', '到期时间'), field('goodsList', '寄存商品', 'goods')]),
  takeInventory: action('takeInventory', '取出商品', [field('phone', '客户手机号', 'phone'), field('goodsList', '取出商品', 'goods')]),
  resetQrcode: action('resetQrcode', '重新生成门店码', [], { confirm: '将重新生成当前门店的预约及续时小程序码，是否继续？' }),
  setDouyinId: action('setDouyinId', '设置抖音门店', [field('dyId', '抖音门店 ID')]),
  groupAuth: action('groupAuth', '团购平台授权', [pick('groupPayType', '平台', { 1: '美团', 2: '抖音', 3: '快手' }, 1)]),
  openSpace: action('openSpace', '开门 / 通电', [], { confirm: '将向当前空间关联的设备下发开门、通电指令，是否继续？' }),
  closeSpace: action('closeSpace', '关门 / 断电', [], { confirm: '将向当前空间关联的设备下发关闭指令，是否继续？', danger: true }),
  disableSpace: action('disableSpace', '启用 / 停用', [], { confirm: '是否切换当前空间的启用状态？' }),
  finishSpace: action('finishSpace', '提前结单', [], { confirm: '将结束当前空间订单，并按原有业务规则处理设备，是否继续？', danger: true }),
  cleanSpace: action('cleanSpace', '完成保洁', [], { confirm: '确认当前空间已完成保洁，可恢复空闲状态？' }),
  renewOrder: action('renewOrder', '调整结束时间', [end], { help: '管理员调整时长，不向客户自动扣款。', states: [0, 1] }),
  changeOrder: action('changeOrder', '换空间 / 改时间', [field('roomId', '空间', 'space'), start, end], { states: [0, 1] }),
  changeOrderUser: action('changeOrderUser', '变更客户', [phone], { states: [0, 1] }),
  cancelOrder: action('cancelOrder', '取消（不退款）', [], { states: [0, 1], danger: true, confirm: '将取消该预约且不退还费用，是否确认？' }),
  refundOrder: action('refundOrder', '取消并退款', [], { states: [0, 1], danger: true, confirm: '将按原支付记录取消并退款，是否确认？团购券需按平台规则另行处理。' }),
  depositRefund: action('depositRefund', '退押金', [], { danger: true, confirm: '确认通过原支付流程退还当前订单押金？' }),
  recharge: action('recharge', '余额充值', [money('money', '充值金额（元）')], { help: '此操作直接增加门店会员余额，请确认已线下收款。' }),
  giftCoupon: action('giftCoupon', '赠送优惠券', [field('couponId', '优惠券', 'coupon')]),
  editMemberVip: action('editMemberVip', '调整 VIP', [num('vipLevel', 'VIP 等级')]),
  removeBlacklist: action('removeBlacklist', '移出黑名单', [], { confirm: '确认恢复该用户在本门店的正常使用权限？' }),
  deleteStaff: action('deleteStaff', '移除员工', [], { confirm: '将移除该员工在当前门店的管理权限，是否继续？', danger: true }),
  settleCleaner: action('settleCleaner', '保洁结算', [money('money', '结算金额（元）'), start, end], { help: '请确认结算期间和金额，操作会生成保洁结算记录。' }),
  cancelCleaning: action('cancelCleaning', '取消保洁任务', [], { confirm: '确认取消该保洁任务？', danger: true }),
  toggleRule: action('toggleRule', '启用 / 禁用', [], { confirm: '确认切换充值规则状态？' }),
  enablePackage: action('enablePackage', '启用 / 禁用', [], { confirm: '确认切换套餐状态？' }),
  deletePackage: action('deletePackage', '删除', [], { confirm: '确认删除此套餐？', danger: true }),
  deleteCoupon: action('deleteCoupon', '删除', [], { confirm: '确认删除此优惠券？', danger: true }),
  deleteVip: action('deleteVip', '删除', [], { confirm: '确认删除此 VIP 规则？', danger: true }),
  deleteInventoryGoods: action('deleteInventoryGoods', '删除', [], { confirm: '确认删除该寄存商品配置？', danger: true }),
  deleteCategory: action('deleteCategory', '删除', [], { confirm: '确认删除此商品分类？', danger: true }),
  toggleProduct: action('toggleProduct', '上架 / 下架', [], { confirm: '确认切换此商品的上下架状态？' }),
  deleteProduct: action('deleteProduct', '删除', [], { confirm: '确认删除此商品？', danger: true }),
  finishProductOrder: action('finishProductOrder', '完成订单', [], { confirm: '确认商品已交付，完成该订单？' }),
  refundPayment: action('refundPayment', '支付退款', [money('refundAmount', '退款金额（元）')], { help: '将通过原支付渠道退款，请核对金额。', danger: true })
}
const page = (title, description, cols, extra = {}) => ({ title, description, columns: columns(cols), rowActions: [], topActions: [], ...extra })
export const pages = {
  overview: page('经营概览', '从预约、空间和客户数据了解当前门店的经营情况。', ''),
  spaces: page('实时房态', '查看空间状态，处理预约、设备和保洁。', 'roomId:空间编号,roomName:空间名称,price:单价（元/小时）,deposit:押金,status:状态', { states: spaceStates, topActions: ['submitOrder'], rowActions: ['openSpace', 'closeSpace', 'disableSpace', 'finishSpace', 'cleanSpace'], cards: true,
    fields: [field('roomName', '空间名称'), field('roomCallName', '空间简称（用于播报）'), pick('roomClass', '经营类型', { 0: '通用计时空间', 1: '运动活动空间', 2: '影音娱乐空间' }, 0), pick('type', '空间等级', { 1: '标准', 2: '舒适', 3: '宽敞', 4: '高配', 5: '商务' }, 1), money('price', '小时单价（元）'), money('deposit', '押金（元）'), money('workPrice', '工作日单价（元）'), num('minHour', '最短预约时长（小时）', 1), num('leadHour', '允许提前入场（小时）', 1), bool('reserve', '允许预约'), bool('jumpClear', '跳过保洁', false), bool('xiaoshiSubmit', '支持按小时预约'), money('prePrice', '预付费单价（元）'), num('preUnit', '预付费计价单位（分钟）', 60), money('minCharge', '最低消费（元）'), money('morningPrice', '上午场价格（元）'), money('afternoonPrice', '下午场价格（元）'), money('nightPrice', '晚间场价格（元）'), money('txPrice', '夜间包场价格（元）'), optional('label', '空间标签（逗号分隔）'), optional('imageUrls', '空间图片地址（逗号分隔）'), num('sortId', '排序')] }),
  orders: page('预约订单', '查询预约记录，处理时长、换空间、客户和退款。', 'orderNo:订单号,roomId:空间,userId:客户, startTime:开始时间,endTime:结束时间,status:状态,payPrice:实付（元）,refundPrice:退款（元）'.replace(', ', ','), { states: orderStates, topActions: ['submitOrder'], rowActions: ['renewOrder', 'changeOrder', 'changeOrderUser', 'refundOrder', 'cancelOrder', 'depositRefund'] }),
  members: page('门店会员', '管理本门店会员、余额和权益。', 'userId:会员编号,nickname:昵称,name:姓名,mobile:手机号,balance:余额（元）,giftBalance:赠送余额（元）,vipLevel:VIP 等级,totalScore:积分', { rowActions: ['recharge', 'giftCoupon', 'editMemberVip'] }),
  staff: page('员工与保洁员', '管理手机端的店长、管理人员和保洁员。PC 登录账号由系统管理员维护。', 'userId:用户编号,name:姓名,mobile:手机号,type:角色,status:状态', { rowActions: ['deleteStaff', 'settleCleaner'], fields: [field('name', '姓名'), phone, pick('role', '岗位', { manager: '门店管理员', cleaner: '保洁员' }, 'manager'), bool('isAdmin', '商户负责人', false)], enums: { type: { 12: '商户负责人', 13: '门店管理员', 14: '保洁员' } }, noEdit: true }),
  blacklist: page('会员黑名单', '管理本门店的使用限制。', 'userId:会员编号,name:姓名,nickname:昵称,mobile:手机号', { topActions: ['addBlacklist'], rowActions: ['removeBlacklist'] }),
  rules: page('充值规则', '配置充值金额、赠送金额和有效期。', 'discountId:编号,payMoney:充值金额（元）,giftMoney:赠送金额（元）,expriceTime:到期时间,status:状态', { rowActions: ['toggleRule'], fields: [money('payMoney', '充值金额（元）'), money('giftMoney', '赠送金额（元）'), date('expriceTime', '有效期至')] }),
  packages: page('时长套餐', '配置可用时长、销售价格和平台团购关联。', 'pkgId:编号,pkgName:套餐名称,hours:时长（小时）,price:售价（元）,expireDay:购买后有效天数,enable:启用', { rowActions: ['enablePackage', 'deletePackage'], fields: [field('pkgName', '套餐名称'), num('hours', '可用时长（小时）', 1), money('price', '售价（元）'), num('expireDay', '有效天数，0 为不限'), num('maxNum', '限购数量，0 为不限'), bool('enable', '启用'), bool('balanceBuy', '支持余额支付'), bool('enableHoliday', '节假日可用'), optional('enableRoom', '适用空间', 'spaces'), optional('enableWeek', '适用星期', 'week'), optional('mtId', '美团团购 ID'), optional('dyId', '抖音团购 ID'), optional('ksId', '快手团购 ID'), num('sortId', '排序')] }),
  coupons: page('优惠券', '创建优惠券，查看发放和使用状态。', 'couponId:编号,couponName:优惠券名称,userId:持有人,minUsePrice:使用门槛（元）,price:面额 / 时长,type:类型,status:状态,expriceTime:到期时间', { rowActions: ['deleteCoupon'], fields: [field('couponName', '优惠券名称'), pick('type', '优惠券类型', { 1: '抵扣券', 2: '满减券', 3: '加时券' }, 1), money('price', '面额（元）或加时时长'), money('minUsePrice', '最低消费金额（元）'), date('expriceTime', '到期时间'), optional('roomType', '适用空间类型编号', 'number')] }),
  vip: page('VIP 规则', '按积分配置会员等级和折扣。', 'vipId:编号,vipName:等级名称,vipLevel:等级,vipDiscount:折扣百分比,score:积分门槛', { rowActions: ['deleteVip'], fields: [field('vipName', '等级名称（最多 3 字）'), num('vipDiscount', '折扣百分比（1–99）', 95), num('score', '积分门槛', 100)] }),
  cleaning: page('保洁任务', '查看任务进度和交付记录。', 'clearId:任务编号,orderNo:关联订单,roomId:空间,userId:保洁员,status:状态,startTime:开始时间,finishTime:完成时间,settlementTime:结算时间', { rowActions: ['cancelCleaning'] }),
  settlements: page('保洁结算记录', '按门店查看保洁结算和对应任务。', 'id:编号,userId:保洁员,money:结算金额（元）,orderNum:任务数,createTime:结算时间'),
  payments: page('支付与退款', '支付金额以分记录，展示时统一换算为元。', 'id:支付编号,orderNo:订单号,orderDesc:说明,price:支付金额（元）,payStatus:支付状态,refundPrice:已退款（元）,payTime:支付时间', { rowActions: ['refundPayment'], cents: ['price', 'refundPrice'] }),
  ledger: page('会员资金流水', '区分余额和赠送余额，追溯充值、消费及退款。', 'id:编号,userId:会员,type:业务类型,money:金额（元）,moneyType:账户,totalMoney:余额（元）,totalGiftMoney:赠送余额（元）,remark:说明,createTime:时间', { enums: { moneyType: { 1: '余额', 2: '赠送余额' } } }),
  products: page('商品管理', '管理单规格商品和上下架。已有多规格商品仍可查询，完整规格编辑保留在手机端。', 'id:编号,storeName:商品名称,price:价格（元）,stock:库存,unitName:单位,isShow:上架', { rowActions: ['toggleProduct', 'deleteProduct'], fields: [field('storeName', '商品名称'), field('cateId', '商品分类', 'category'), field('image', '商品图片地址（HTTPS）'), money('price', '价格（元）'), num('stock', '库存'), field('unitName', '单位', 'text', { default: '件' }), pick('isShow', '上架状态', { 0: '下架', 1: '上架' }, 1), optional('storeInfo', '商品简介', 'textarea')] }),
  categories: page('商品分类', '整理当前门店的商品分类。', 'id:编号,name:分类名称,parentId:父分类,sort:排序,status:状态', { rowActions: ['deleteCategory'], fields: [field('name', '分类名称'), num('parentId', '父分类编号，0 为一级'), num('sort', '排序'), pick('status', '状态', { 0: '启用', 1: '停用' }, 0), optional('description', '说明', 'textarea')] }),
  productOrders: page('商品订单', '查询商品消费并确认交付。', 'orderNo:订单号,userName:客户,userPhone:手机号,roomId:空间,totalNum:件数,payPrice:实付（元）,status:状态,payTime:支付时间', { rowActions: ['finishProductOrder'] }),
  inventory: page('商品寄存', '办理寄存、取出并查看有效期。', 'id:寄存编号,userId:客户,orderNo:寄存单号,expireTime:到期时间,remark:备注', { topActions: ['addInventory', 'takeInventory'] }),
  inventoryGoods: page('寄存商品配置', '设置允许寄存的商品及计量单位。', 'id:编号,goodsName:商品名称,units:单位,enable:启用', { rowActions: ['deleteInventoryGoods'], fields: [field('goodsName', '商品名称'), field('units', '计量单位'), bool('enable', '启用')] }),
  inventoryRecords: page('寄存出入记录', '查询每次存入和取出的数量记录。', 'id:编号,inventoryId:寄存单,detailId:明细,num:数量,createTime:时间'),
  sounds: page('语音播报', '按空间类别设置入场、到时和自定义播报内容。', 'soundId:编号,roomClass:空间类别,welcomeText:欢迎语,endText30:提前 30 分钟,endText5:提前 5 分钟,endText:结束提示', { fields: [num('roomClass', '空间类别'), optional('welcomeText', '欢迎语', 'textarea'), optional('endText30', '提前 30 分钟', 'textarea'), optional('endText5', '提前 5 分钟', 'textarea'), optional('endText', '结束提示', 'textarea'), optional('nightText', '夜间提示', 'textarea'), optional('customizeText', '自定义播报', 'textarea')] }),
  platforms: page('团购平台', '查看平台与门店对应关系。绑定团购门店、美团及抖音授权请前往“门店管理”。', 'id:编号,platform:平台,shopId:团购服务商门店 ID', { topActions: ['setDouyinId'], enums: { platform: { 1: '美团', 2: '抖音', 3: '快手' } } }),
  devices: page('设备操作记录', '追溯开门、通断电等操作。', 'id:编号,userId:操作人,roomId:空间,cmd:操作,createTime:时间'),
  settings: page('公告与门店信息', '维护公告、联系信息和网络说明，支持查看及更新门店码。', 'storeId:门店编号,storeName:门店名称,notice:公告,address:地址,kefuPhone:联系电话', { topActions: ['resetQrcode'], fields: [optional('notice', '门店公告', 'textarea'), optional('address', '地址'), optional('kefuPhone', '联系电话'), optional('wifiInfo', 'Wi-Fi 名称'), optional('wifiPwd', 'Wi-Fi 密码')], singleton: true })
}
