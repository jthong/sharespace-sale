package com.cloudsteward.module.member.enums;

import com.cloudsteward.framework.common.exception.ErrorCode;

/**
 * Member 错误码枚举类
 * <p>
 * member 系统，使用 1-004-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 用户相关  1004001000============
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1004001000, "用户不存在");
    ErrorCode USER_PASSWORD_FAILED = new ErrorCode(1004001001, "密码校验失败");
    ErrorCode USER_EXISTS = new ErrorCode(1004001002, "用户已存在，请勿重复添加");

    // ========== AUTH 模块 1004003000 ==========
    ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1004003000, "登录失败，账号密码不正确");
    ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1004003001, "登录失败，账号被禁用");
    ErrorCode AUTH_PROMISSION_ERROR = new ErrorCode(1004003002, "暂无操作权限");
    ErrorCode AUTH_TOKEN_EXPIRED = new ErrorCode(1004003004, "Token 已经过期");
    ErrorCode AUTH_THIRD_LOGIN_NOT_BIND = new ErrorCode(1004003005, "未绑定账号，需要进行绑定");
    ErrorCode AUTH_WEIXIN_MINI_APP_PHONE_CODE_ERROR = new ErrorCode(1004003006, "获得手机号失败!{}");
    ErrorCode AUTH_WEIXIN_MINI_APP_LOGIN_CODE_ERROR = new ErrorCode(1004003007, "获取微信用户信息失败！{}");
    ErrorCode AUTH_USER_BIND_MINIAPP_ERROR = new ErrorCode(1004003008, "该用户未授权微信登录，无法下单！");
    ErrorCode USER_WEIXIN_PAY_ERROR = new ErrorCode(1004003009, "微信支付失败！");
    ErrorCode USER_WEIXIN_PAY_REFUND_ERROR = new ErrorCode(1004003010, "押金退款失败！该订单无法退款");
    ErrorCode ADMIN_WEIXIN_PAY_REFOUND_ERROR = new ErrorCode(1004003011, "微信退款失败!该订单无法退款");
    ErrorCode ADMIN_WEIXIN_PAY_SPLIT_ERROR = new ErrorCode(1004003012, "门店分账失败！请检查配置！");
    ErrorCode ADMIN_STORE_TEMPLATE_KEY_ERROR = new ErrorCode(1004003013, "门店模板key不存在！");

    // ========== app相关 1004004000 ==========
    ErrorCode NOT_START_ORDER = new ErrorCode(1004004001, "没有进行中的订单！");
    ErrorCode DEVICE_OPRATION_ERROR = new ErrorCode(1004004002, "设备操作失败！");
    ErrorCode CLEAR_USER_DELETE_ERROR = new ErrorCode(1004004003, "请结算完所有已完成的任务，再删除用户！");
    ErrorCode ROOM_CLEAR_SUBMIT_ORDER_ERROR = new ErrorCode(1004004004, "该房间不允许清洁时预定！");
    ErrorCode ORDER_START_TIME_ERROR = new ErrorCode(1004004005, "订单开始时间不能小于当前时间！");
    ErrorCode ORDER_START_TIME_GT_END_ERROR = new ErrorCode(1004004006, "订单开始时间不能小于结束时间！");
    ErrorCode ORDER_START_TIME_MAX_ERROR = new ErrorCode(1004004007, "订单开始时间超过允许提前下单的时间！");
    ErrorCode ORDER_TIME_CHECK_ERROR = new ErrorCode(1004004008, "您选择的时间段该场地不可用，请修改时间！");
    ErrorCode TIME_UNIT_ERROR = new ErrorCode(1004004009, "时间单位错误，必须以0.5小时/30分钟为一个单位！");
    ErrorCode PAY_TYPE_ERROR = new ErrorCode(1004004010, "支付方式选择错误！");
    ErrorCode ORDER_TIME_MIN_ERROR = new ErrorCode(1004004011, "选择的时长不能低于4个小时！");
    ErrorCode COUPON_NOT_FOUND_ERROR = new ErrorCode(1004004012, "选择的优惠券不存在！");
    ErrorCode COUPON_MIN_USER_PRICE_ERROR = new ErrorCode(1004004013, "选择的优惠券未达到使用门槛！");
    ErrorCode MEMBER_BALANCE_MIN_ERROR = new ErrorCode(1004004014, "账户余额不足，请充值后重试！");
    ErrorCode ORDER_WEIXIN_PAY_ERROR = new ErrorCode(1004004015, "微信支付失败！");
    ErrorCode ORDER_STATUS_CANCEL_OPRATION_ERROR = new ErrorCode(1004004016, "订单已取消，不支持续费！");
    ErrorCode ORDER_STATUS_FINISH_OPRATION_ERROR = new ErrorCode(1004004017, "订单已结束，不支持续费！请重新下单");
    ErrorCode ORDER_START_OPRATION_ERROR = new ErrorCode(1004004018, "订单已开始！");
    ErrorCode ORDER_CANCEL_OPRATION_ERROR = new ErrorCode(1004004019, "无法取消当前订单！仅允许取消下单不超过5分钟，且状态为未开始、进行中的订单！");
    ErrorCode ADMIN_ORDER_OPRATION_ERROR = new ErrorCode(1004004020, "当前订单不支持此操作!");
    ErrorCode ORDER_START_TIME_LT_NOW_ERROR = new ErrorCode(1004004021, "开始时间已经小于当前时间5分钟，请重新选择开始时间！");
    ErrorCode ORDER_MAX_END_TIME_ERROR = new ErrorCode(1004004022, "订单总时长不能超过24小时，请重新选择预订时间！");
    ErrorCode DISCOUNTRULE_REPETITION_ERROR = new ErrorCode(1004004024, "该充值金额已存在其他规则中，不允许重复添加！");
    ErrorCode COUPON_USED_ERROR = new ErrorCode(1004004025, "该优惠券已被使用或已过期！");
    ErrorCode COUPON_USE_CHECK_ERROR = new ErrorCode(1004004026, "该优惠券不符合使用条件！");
    ErrorCode COUPON_USE_CHECK_STORE_ERROR = new ErrorCode(1004004027, "该优惠券不能在当前门店使用！");
    ErrorCode USER_NO_MONEY_WITHDRAWAL_ERROR = new ErrorCode(1004004028, "您当前没有可提现的收入！");
    ErrorCode GAME_CREATE_NUM_MAX_ERROR = new ErrorCode(1004004030, "每日允许创建5条组局信息，请明天再试！");
    ErrorCode GAME_DELETE_USER_ERROR = new ErrorCode(1004004031, "只有组队中或未支付的对局才可以踢出玩家！");
    ErrorCode GAME_JOIN_USER_ERROR = new ErrorCode(1004004032, "只有组队中或已加入的对局才能操作！");
    ErrorCode GAME_MAX_USER_ERROR = new ErrorCode(1004004033, "对局人数已满！");
    ErrorCode GAME_DELETE_ME_ERROR = new ErrorCode(1004004034, "不能踢出自己！");
    ErrorCode GAME_START_TIME_ERROR = new ErrorCode(1004004035, "开始时间不能小于当前时间！");
    ErrorCode ORDER_CHANGE_ROOM_ERROR = new ErrorCode(1004004036, "只能更换到当前场地级别同等及以下的场地！");
    ErrorCode ORDER_NOT_FOUND_ERROR = new ErrorCode(1004004037, "当前不存在订单，请先下单！");
    ErrorCode ORDER_CHANGE_ROOM_DEPOSIT_ERROR = new ErrorCode(1004004038, "新旧场地的押金设置不一致，无法更换！");
    ErrorCode ORDER_CHANGE_ROOM_CLASS_ERROR = new ErrorCode(1004004039, "新旧场地的类别不一致，无法更换！");
    ErrorCode CLEAR_ORDER_NOT_JIEDAN = new ErrorCode(1004004040, "订单已经被其他人抢走！");
    ErrorCode CLEAR_ORDER_STATUS_ERROR = new ErrorCode(1004004041, "订单当前状态不允许进行此操作！");
    ErrorCode USER_TYPE_CHECK_ERROR = new ErrorCode(1004004042, "用户类型检查异常！用户不能同时成为管理员和保洁员！");
    ErrorCode CHECK_STORE_PROMISSION_ERROR = new ErrorCode(1004004050, "选择的门店中包含未授权的门店！");
    ErrorCode MEMBER_PAGE_PARAM_ERROR = new ErrorCode(1004004060, "参数错误！");
    ErrorCode ORDER_PAGE_PARAM_ERROR = new ErrorCode(1004004061, "参数错误！");
    ErrorCode NOT_FINISH_CLEAR_IONF_ERROR = new ErrorCode(1004004062, "该条件下没有可结算的订单！");
    ErrorCode CLEAR_INFO_STATUS_OPRATION_ERROR = new ErrorCode(1004004063, "当前状态不能操作,请确认保洁订单状态！");
    ErrorCode CLEAR_IMAGE_NOT_FOUNT_ERROR = new ErrorCode(1004004064, "请上传清洁完成的图片！");
    ErrorCode ORDER_OPEN_DOOR_ERROR = new ErrorCode(1004004065, "订单当前状态不支持开门！");
    ErrorCode DEVICE_IOT_OP_ERROR = new ErrorCode(1004004070, "操作失败,错误信息:{}");
    ErrorCode STORE_STORE_IS_DISABLE = new ErrorCode(1004004071, "门店当前状态不允许此操作！");
    ErrorCode CLEAR_AND_FINISH_ROOM_STATUS_ERROR = new ErrorCode(1004004072, "房间当前状态不允许执行此操作！");
    ErrorCode ORDER_MIN_HOUR_ERROR = new ErrorCode(1004004073, "订单时长必须达到设置的起步时长！");
    ErrorCode GOURP_NO_PAY_ROOM_TYPE_CHECK_ERROR = new ErrorCode(1004004080, "团购券适用的房间类型，与当前订单预定的房间类型不匹配，请检查!");
    ErrorCode GOURP_NO_PAY_TIME_HOUR_CHECK_ERROR = new ErrorCode(1004004081, "团购券的使用时长，与当前预订时长不匹配，请检查!");
    ErrorCode STORE_MT_TUANGOU_PAY_ERROR = new ErrorCode(1004004082, "当前门店暂不支持团购券支付！请选择其他支付方式！");
    ErrorCode GROUP_NO_CHECK_ERROR = new ErrorCode(1004004083, "团购券验证失败！请检查券码是否正确或重试！");
    ErrorCode GROUP_NO_CANCEL_TIMEOUT_ERROR = new ErrorCode(1004004084, "团购券退款失败,该团购券不支持退款，或已超过退款时效！");
    ErrorCode GROUP_NO_CHECK_REFLASH_ERROR = new ErrorCode(1004004085, "该券码已被撤销验证，请在团购平台查看新生成的券码！");
    ErrorCode TONGXIAO_ORDER_START_ERROR = new ErrorCode(1004004086, "当前预定的时段为通宵场！不支持提前开始消费！");
    ErrorCode GROUP_NO_CHECK_STORE_ERROR = new ErrorCode(1004004087, "此团购券不可在本店消费，请在团购平台查询券信息！");
    ErrorCode STORE_DY_TUANGOU_PAY_ERROR = new ErrorCode(1004004088, "当前店铺暂不支持抖音团购券支付！请选择其他支付方式！");
    ErrorCode GROUP_PAY_WORK_CHECK_ERROR = new ErrorCode(1004004089, "您输入的团购券仅周一至周四可用！请修改预定时间或更换团购券");
    ErrorCode GROUP_PAY_ROOM_TYPE_CHECK_ERROR = new ErrorCode(1004004090, "团购券未设置可用场地类型，请联系管理员");
    ErrorCode GROUP_MEITUAN_SCOPE_ERROR = new ErrorCode(1004004091, "该门店未授权给团购平台，需要管理员授权！");
    ErrorCode ORDER_START_TIQIAN_ERROR = new ErrorCode(1004004092, "提前消费，不允许超过门店设定的最大提前时间！");
    ErrorCode CHECK_TONGXIAO_TIME_ERROR = new ErrorCode(1004004093, "通宵场开始时间不符合规则！请修改预定时间后重试！");
    ErrorCode CHECK_GROUP_NO_TIME_ERROR = new ErrorCode(1004004094, "团购券未设置抵扣时长，请联系商家！");
    ErrorCode DELETE_ROOM_ERROR = new ErrorCode(1004004095, "删除失败,房间存在未完成的订单！");
    ErrorCode ORDER_ROOM_SUMBIT_ERROR = new ErrorCode(1004004096, "该房间存在其他用户已提交未支付的订单，请1分钟后刷新重试！");
    ErrorCode CLEAR_FINISH_ORDER_START_ERROR = new ErrorCode(1004004097, "该房间有订单进行中，无法完成保洁订单！");
    ErrorCode DEVICE_DATA_EXISTS_ERROR = new ErrorCode(1004004098, "该设备已存在，请勿重复添加！");
    ErrorCode ROOM_BAN_TIME_ERROR = new ErrorCode(1004004099, "房间禁用时间请填写完整！");
    ErrorCode DEVICE_ADD_MAX_NUM_ERROR = new ErrorCode(1004004100, "此类型设备每个场地只允许添加1个！");
    ErrorCode STORE_RENEW_TIME_ERROR = new ErrorCode(1004004101, "不允许超过提前1个月续费！");
    ErrorCode LOCK_NOT_FOUND_ERROR = new ErrorCode(1004004102, "密码锁不存在！");
    ErrorCode USER_IS_STORE_BLACK = new ErrorCode(1004004103, "黑名单用户不允许下单");
    ErrorCode COUPON_IS_NOT_ENABLE = new ErrorCode(1004004104, "优惠券状态不允许回收");
    ErrorCode IOT_GROUP_PAY_NOT_SUPPOT = new ErrorCode(1004004105, "未对接团购平台");

    ErrorCode PRODUCT_OUT_OF_STOCK = new ErrorCode(1004004106, "商品库存不足");
    ErrorCode PRODUCT_REFOUND_ERROR = new ErrorCode(1004004107, "商品退款失败 {}");

    ErrorCode ROOM_PRE_CONFIG_ERROR = new ErrorCode(1004004108, "预付费配置参数不完整");

    ErrorCode PKG_BUY_MAX_NUM_ERROR = new ErrorCode(1004004200, "超过购买该套餐的最大数量限制！");
    ErrorCode PKG_BUY_DISABLE_ERROR = new ErrorCode(1004004201, "该套餐不支持购买！");
    ErrorCode PKG_USE_STATUS_ERROR = new ErrorCode(1004004202, "该套餐状态无法使用！");
    ErrorCode PKG_USE_STORE_ERROR = new ErrorCode(1004004203, "该套餐不能在当前门店使用！");

    ErrorCode PKG_USE_CHECK_TIME_ERROR = new ErrorCode(1004004204, "该套餐可用时间段与订单时间段不匹配！");
    ErrorCode PKG_USE_CHECK_ROOM_TYPE_ERROR = new ErrorCode(1004004205, "该套餐可用场地类型与订单不匹配！");
    ErrorCode PKG_USE_CHECK_WEEK_ERROR = new ErrorCode(1004004206, "该套餐可用星期与订单不匹配！");
    ErrorCode PKG_USE_CHECK_HOLIDAY_ERROR = new ErrorCode(1004004207, "该套餐节假日不可使用！");
    ErrorCode PKG_USE_CHECK_HOUR_ERROR = new ErrorCode(1004004208, "该套餐的抵扣时长与选择的订单时长不匹配！");

    ErrorCode PKG_ORDER_PAY_TYPE_ERROR = new ErrorCode(1004004209, "此套餐暂不支持余额支付，请使用微信支付！");

    ErrorCode WXPAY_CONFIG_PARAM_ERROR = new ErrorCode(1004004210, "商户收款模式，必须填写支付密钥和支付证书！");
    ErrorCode ORDER_RENEW_TIME_ERROR = new ErrorCode(1004004211, "订单续费时间异常，请重新选择时间提交！");

    ErrorCode PKGINFO_MTID_EXIST = new ErrorCode(1004004212, "该美团团购id已与其他套餐绑定,请更换其他团购id!");
    ErrorCode PKGINFO_DYID_EXIST = new ErrorCode(1004004213, "该抖音团购id已与其他套餐绑定,请更换其他团购id!");
    ErrorCode PKGINFO_KSID_EXIST = new ErrorCode(1004004214, "该抖音团购id已与其他套餐绑定,请更换其他团购id!");
    ErrorCode PKG_USE_CHECK_ROOM_ERROR = new ErrorCode(1004004215, "该套餐可用房间与订单不匹配！");

    ErrorCode STORE_VIP_CONFIG_SCORE_ERROR = new ErrorCode(1004004230, "存在重复的积分门槛！");
    ErrorCode STORE_VIP_CONFIG_MAX_ERROR = new ErrorCode(1004004231, "最多支持设置三个会员等级！");
    ErrorCode ORDER_DEPOSIT_REFUND_ERROR = new ErrorCode(1004004232, "订单没有押金，或者押金已经退回！");
    ErrorCode CLOSE_ORDER_CHECK_BALL_ERR = new ErrorCode(1004004233, "订单提前结束失败，请检查球是否已全部归还！");
    ErrorCode ORDER_CREATE_TIMEOUT = new ErrorCode(1004004234, "订单提交超时，请重新提交下单！");
    ErrorCode ORDER_TIME_INDEX_NOT_SUPPORT = new ErrorCode(1004004235, "不支持的包场类型！");
    ErrorCode ORDER_TIME_NOT_SUPPORT = new ErrorCode(1004004236, "消费时间不匹配该场次时间");
    ErrorCode ORDER_TIME_INDEX_MIN_ERROR = new ErrorCode(1004004237, "本场次消费时间已不足1小时，不允许下单");
    ErrorCode ORDER_TIME_INDEX_START_ERROR = new ErrorCode(1004004238, "包场订单不支持提前开始，到预订时间会自动开始订单");
    ErrorCode STORE_DELETE_VIP_CONFIG_ERROR = new ErrorCode(1004004239, "该VIP规则下已存在会员，无法删除！");
    ErrorCode PLATFORM_ORDER_SUBMIT_ERROR = new ErrorCode(1004004240, "订单提交失败:{}");
    ErrorCode PLATFORM_ORDER_GROUP_CHECK_ERROR = new ErrorCode(1004004241, "订单提交失败:{}");
    ErrorCode STORE_PAY_CONFIG_EXISTS = new ErrorCode(1004004242, "该门店已存在支付配置");
    ErrorCode STORE_PAY_SPLIT_RATE_CONFIG_EXISTS = new ErrorCode(1004004243, "门店剩余可分账比例不足，还允许分账{}%");
    ErrorCode STORE_PAY_SPLIT_MCHID_EXISTS = new ErrorCode(1004004244, "该门店已存在此商户号的分账配置");
    ErrorCode USER_MERCHANT_APPLY_EXISTS = new ErrorCode(1004004245, "已存在申请记录，请勿重复提交");
    ErrorCode STORE_PAY_CONFIG_NOT_EXISTS = new ErrorCode(1004004246, "请先给门店配置收款账户");
    ErrorCode STORE_PAY_CONFIG_SPLIT_NOT_OPEN = new ErrorCode(1004004247, "该门店未开启分账功能，请先开启");


    ErrorCode COUPON_EXPIRE_ERROR = new ErrorCode(1004004248, "该优惠券已过期！");
    ErrorCode COUPON_ACTIVE_NOT_FOUND = new ErrorCode(1004004249, "活动不存在！");
    ErrorCode COUPON_ACTIVE_IS_END = new ErrorCode(1004004250, "活动已结束！");
    ErrorCode COUPON_ACTIVE_IS_GET = new ErrorCode(1004004251, "您已参与该活动，不能重复参与！");
    ErrorCode COUPON_ACTIVE_END_TIME_ERROR = new ErrorCode(1004004252, "活动截止时间不能晚于优惠券过期时间！");

    ErrorCode PAY_ORDER_REFUND_MAX_ERROR = new ErrorCode(1004004253, "该订单最大可退款金额为:{}元");
    ErrorCode PAY_ORDER_REFUND_ERROR = new ErrorCode(1004004254, "订单未支付，退款失败！");
    ErrorCode NFC_MODEL_ID_ERROR = new ErrorCode(1004004255, "请先通过后台管理系统配置NFC的model_id！");
    ErrorCode INVENTORY_EXPIRE_ERROR = new ErrorCode(1004004256, "寄存商品：{},过期时间：{},不支持取出！");
    ErrorCode INVENTORY_TAKE_ERROR = new ErrorCode(1004004257, "寄存商品：{},可取出数量不足！");


    ErrorCode STORE_PAY_CONFIG_NOT_FOUND = new ErrorCode(1004004998, "请先给门店配置支付收款信息！");
    ErrorCode STORE_WX_PAY_CONFIG_NOT_FOUND = new ErrorCode(1004004999, "该门店暂不支持微信支付！");
    ErrorCode DATA_NOT_EXISTS = new ErrorCode(1004005000, "数据不存在");
    ErrorCode DATA_EXISTS_ERROR = new ErrorCode(1004005001, "数据已存在，请不要重复保存！");
    ErrorCode OPRATION_ERROR = new ErrorCode(1004005002, "非法操作");
    ErrorCode IOT_ERROR = new ErrorCode(1004005003, "操作失败！{}");
    ErrorCode IOT_SIGN_ERROR = new ErrorCode(1004005004, "签名不匹配");
    ErrorCode IOT_PARAMS_ERROR = new ErrorCode(1004005005, "参数不符合要求,{}");


}
