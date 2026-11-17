App({
  globalData: {
    userDatatoken: "",                     // 登录 token
    userData: null,                        // 用户信息
    isLogin: false,                        // 登录状态
    baseUrl: "https://api.example.invalid",   // 替换为本实例的后端 API 地址
    tenantId: "",                          // 租户ID（系统已关闭多租户，留空即可）
    appName: "云掌柜共享空间",
    mainColor: "#47BA91",
    isIpx: false,                          // 是否全面屏（iPhone X 及以后）
    isIos: false,                          // 是否 iOS
  },
  onLaunch: function () {
    var info = wx.getSystemInfoSync();
    this.globalData.isIos = info.platform === "ios";
    this.globalData.isIpx = (info.platform === "ios" && info.screenHeight >= 812);
    wx.setStorageSync('statusBarHeight', info.statusBarHeight || 20);
    // 恢复本地缓存的登录态，避免重启后所有接口 401
    var cachedToken = wx.getStorageSync("userDatatoken");
    if (cachedToken) {
      this.globalData.userDatatoken = cachedToken;
      this.globalData.isLogin = true;
    }
  }
})
