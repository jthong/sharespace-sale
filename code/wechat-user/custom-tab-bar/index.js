// custom-tab-bar/index.js
// 自定义底部导航：首页/优惠中心/我的订单/个人中心
const app = getApp()
const tabList = [
  {
    pagePath: "/pages/index/index",
    text: "首页",
    iconPath: "https://oss.systemdiv.com/6a15ef128878f8cd0253a9674da1e0553289e8047dcc0abc75646bc7fb211d6e.png",
    selectedIconPath: "https://oss.systemdiv.com/aa0c05d48e06f7a62ca5d350a6e09aa07dd5bbbac4625e86b86ba6523d6aca52.png",
  },
  {
    pagePath: "/pages/recharge/recharge",
    text: "优惠中心",
    iconPath: "https://oss.systemdiv.com/4139abd4f624613102a83decd11fa0986dfcbb573bb29198d5937a31efca1f39.png",
    selectedIconPath: "https://oss.systemdiv.com/8de2ed5363cb072e592e2c9d576217118e7bc5b89638098545b037c273d8e987.png",
  },
  {
    pagePath: "/pages/orderList/orderList",
    text: "我的订单",
    iconPath: "https://oss.systemdiv.com/ab8f8ac4a51f78d94b47758803ff1c3fee15743153c8f1b17018a35d27169ad9.png",
    selectedIconPath: "https://oss.systemdiv.com/f9e632dcf5e34b04c1e75a873abce57f49b2650cb840a126ea1361b8ac6e678c.png",
  },
  {
    pagePath: "/pages/user/user",
    text: "个人中心",
    iconPath: "https://oss.systemdiv.com/891ed258d18cfb9fff12824f7589663b6338abecd8834fb588eae5f90790381f.png",
    selectedIconPath: "https://oss.systemdiv.com/2ec5f9e1efbb4292bb224fef9a608fb13d3e70c75a6ecd44523ef558a931ee68.png",
  },
]

Component({
  data: {
    list: tabList,
    color: "#BCBCBC",
    safeAreaInsetBottom: 0,
    selectedColor: "#47BA91",
    borderStyle: "white",
    selected: 0,
  },
  methods: {
    switchTab(e) {
      const { path, index } = e.currentTarget.dataset;
      // 需要登录才能访问的 tab：优惠中心、我的订单
      const needLoginTabs = ["/pages/recharge/recharge", "/pages/orderList/orderList"];
      const isLoggedIn = app.globalData.isLogin || wx.getStorageSync("userDatatoken");
      if (needLoginTabs.indexOf(path) > -1 && !isLoggedIn) {
        wx.navigateTo({
          url: '/pages/login/login',
        });
        return;
      }
      this.setData({
        selected: index,
      });
      wx.switchTab({
        url: path,
      });
    },
    setSafeAreaInsetBottom() {
      const info = wx.getSystemInfoSync();
      const statusBarHeight = (info && info.statusBarHeight) || 20;
      if (app.globalData.isIpx) {
        this.setData({
          safeAreaInsetBottom: 44,
        });
      } else if (statusBarHeight > 20) {
        this.setData({
          safeAreaInsetBottom: (statusBarHeight - 20) * 2 + 4,
        });
      }
    },
  },
  // 页面显示时按当前路由自动高亮对应 tab
  pageLifetimes: {
    show() {
      const pages = getCurrentPages();
      if (!pages || !pages.length) return;
      const current = pages[pages.length - 1];
      const route = "/" + current.route;
      const idx = this.data.list.findIndex(item => item.pagePath === route);
      if (idx > -1 && idx !== this.data.selected) {
        this.setData({ selected: idx });
      }
    },
  },
  attached() {
    this.setSafeAreaInsetBottom();
  },
});
