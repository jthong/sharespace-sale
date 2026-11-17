// pages/login/login.js
var http = require('../../utils/http');
var util1 = require('../../utils/util.js');
const app = getApp()
Page({

  /**
   * 页面的初始数据
   */
  data: {
    appName: app.globalData.appName,
    fastLogin: true,
    username: '',
    password: '',
    mobile: '',
    loginData: '',
    agreed: false, //是否已同意协议
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.fromCleaningNotice = options.fromCleaningNotice === '1';
    // 进入页面静默检测是否已注册（不弹窗，仅显示"点击自动登录"入口）
    var that = this;
    wx.login({
      success: function (res) {
        if (res.code != null) {
          http.request(
            "/member/auth/wxLoginByCode?code=" + res.code,
            "1",
            "post", {},
            "",
            "",
            function success(info) {
              console.info(info);
              if (info.code == 0 && info.data) {
                that.setData({
                  loginData: info.data,
                  mobile: info.data.mobile,
                })
              }
            },
            function fail(info) {
            }
          )
        }
      }
    })
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    wx.stopPullDownRefresh()
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },
  // 切换协议勾选
  toggleAgreement() {
    this.setData({ agreed: !this.data.agreed });
  },
  // 打开用户协议
  openUserAgreement() {
    wx.showModal({ title: '用户协议', content: '请在微信公众平台查看完整用户协议', showCancel: false });
  },
  // 打开隐私政策
  openPrivacyPolicy() {
    wx.showModal({ title: '隐私政策', content: '请在微信公众平台查看完整隐私政策', showCancel: false });
  },
  // 检查是否已同意协议
  checkAgreed() {
    if (!this.data.agreed) {
      wx.showModal({
        title: '提示',
        content: '请先阅读并同意《用户协议》和《隐私政策》',
        showCancel: false,
      });
      return false;
    }
    return true;
  },
  continueAfterLogin() {
    const url = this.fromCleaningNotice
      ? '/packageA/pages/task/task?status=0&fromCleaningNotice=1'
      : '/pages/index/index';
    setTimeout(() => wx.reLaunch({ url }), 600);
  },
  // 一键手机号登录（微信授权手机号）
  phone: function (e) {
    if (!this.checkAgreed()) return;
    var that = this;
    if (e.detail.errMsg == "getPhoneNumber:fail user deny") {
      wx.showToast({ title: '已取消授权', icon: 'none' })
      return
    }
    if (e.detail.errMsg == "getPhoneNumber:ok") {
      wx.login({
        success: function (res) {
          if (res.code != null) {
            http.request(
              "/member/auth/weixin-mini-app-login",
              "1",
              "post", {
              "phoneCode": e.detail.code,
              "loginCode": res.code
            },
              "",
              "获取中...",
              function success(info) {
                console.info('返回111===');
                console.info(info);
                if (info.code == 0) {
                  if (info.data) {
                    app.globalData.userDatatoken = info.data;
                    app.globalData.isLogin = true;
                    //缓存服务器返回的用户信息
                    wx.setStorageSync("userDatatoken", info.data)
                    wx.showToast({
                      title: '登录成功',
                      icon: 'none'
                    })
                    that.continueAfterLogin();
                  }
                } else {
                  wx.showModal({
                    title: '登录失败',
                    content: info.msg || '手机号授权登录失败',
                    showCancel: false,
                  })
                }
              },
              function fail(info) {
              }
            )
          } else {
            wx.showToast({ title: '微信登录失败，请重试', icon: 'none' })
          }
        }
      })
    }
  },
  wxLogin: function () {
    this.setData({
      fastLogin: true
    })
  },
  pwdlogin: function () {
    this.setData({
      fastLogin: false,
      password: '',
    })
  },
  userLogin: function () {
    if (!this.checkAgreed()) return;
    var that = this;
    if (!that.data.username || that.data.username.length != 11) {
      wx.showToast({
        title: '账号格式不正确',
        icon: 'none'
      })
      return
    }
    if (!that.data.password || that.data.password.length < 6) {
      wx.showToast({
        title: '密码格式不正确',
        icon: 'none'
      })
      return
    }
    //登录
    http.request(
      "/member/auth/login",
      "1",
      "post", {
      "mobile": that.data.username,
      "password": that.data.password
    },
      "",
      "获取中...",
      function success(info) {
        console.info('返回111===');
        console.info(info);
        if (info.code == 0) {
          if (info.data) {
            app.globalData.userDatatoken = info.data;
            app.globalData.isLogin = true;
            //缓存服务器返回的用户信息
            wx.setStorageSync("userDatatoken", info.data)
            wx.showToast({
              title: '登录成功',
              icon: 'none'
            })
            that.continueAfterLogin();
          }
        } else {
          wx.showModal({
            title: '登录失败',
            content: info.msg,
            showCancel: false,
          })
        }
      },
      function fail(info) {

      }
    )

  },
  backHome: function () {
    wx.switchTab({
      url: '/pages/index/index',
    })
  },
  autoLogin: function () {
    if (!this.checkAgreed()) return;
    var that = this;
    if (!that.data.loginData) {
      wx.showToast({ title: '未检测到账号，请手机号授权登录', icon: 'none' })
      return
    }
    app.globalData.userDatatoken = that.data.loginData;
    app.globalData.isLogin = true;
    wx.setStorageSync("userDatatoken", that.data.loginData)
    wx.showToast({
      title: '登录成功',
      icon: 'success'
    })
    this.continueAfterLogin();
  },
})
