// packageA/pages/addLock/addLock.js
const app = getApp()
var http = require('../../utils/http');
Page({

  /**
   * 页面的初始数据
   */
  data: {
    isIpx: app.globalData.isIpx ? true : false,
    isLogin: app.globalData.isLogin,
    storeId: '', // 门店id
    payCart: [], // 需要支付的商品信息
    roomList: [],  //房间信息
    roomIndex: '',
    roomId: '', //下单房间
    cartTotalPrice: 0.00, // 总价
    showItems: [], // 默认展示的内容 前三个商品
    isShowAll: false, // 是否展示全部商品
    userName: '', //用户姓名
    userPhone: '', // 用户联系方式
    mark: '', // 订单备注
    productNum: 0, // 商品数量'
    orderNo: '',
    showYeepay: false,
    isSubmitting: false,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    let that = this
    let cart = wx.getStorageSync("payCart")
    that.setData({
      storeId: options.storeId,
      roomId: options.roomId,
      payCart: cart,
      showItems: cart.slice(0, 2)
    })
    let num = 0
    cart.forEach(item => {
      num += item.number
    });
    that.setData({
      productNum: num
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
    let that = this
    if (that.data.showYeepay && that.data.orderNo) {
      // 清除跳转标记
      that.setData({
        showYeepay: false,
      });
      wx.navigateTo({
        url: '../productOrder/productOrder',
      })
    }else{
      that.getRoomList();
      that.calculateCartTotalPrice();
    }
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

  //计算价格 
  calculateCartTotalPrice() {
    let that = this
    let totalPrice = that.data.payCart.reduce((acc, cur) => acc + cur.number * cur.price, 0);
    that.setData({
      cartTotalPrice: parseFloat(totalPrice).toFixed(2)
    });
  },
  // 默认展示两个，点击取反
  showMore: function () {
    let that = this
    let show = that.data.isShowAll
    that.setData({
      isShowAll: !show
    })
  },
  // 点击付款
  topay: function () {
    let that = this
    if (that.data.isSubmitting) return
    if (that.data.payCart.length <= 0) {
      wx.showToast({ title: '非法操作，无购买商品', icon: 'none' });
      return
    }
    if (!that.data.roomIndex) {
      wx.showToast({ title: '请选择下单房间', icon: 'none' });
      return
    }
    that.setData({ isSubmitting: true })
    wx.showLoading({ title: '提交订单中' })
    if (app.globalData.isLogin) {
      http.request(
        "/product/order/create",
        "1",
        "post",
        {
          "productInfo": that.data.payCart,
          "roomId": that.data.roomList[that.data.roomIndex].value,
          "mark": that.data.mark
        },
        app.globalData.userDatatoken.accessToken,
        "",
        function success(info) {
          wx.hideLoading()
          if (info.code == 0) {
            that.setData({
              orderNo: info.data.orderNo
            })
            if(info.data.prePayTn){
              wx.openEmbeddedMiniProgram({
                appId: info.data.appId,
                envVersion: 'release',
                path: info.data.prePayTn,
                success:(res)=>{
                  that.setData({
                    showYeepay: true,
                  });
                },
                fail:(err)=>{
                  that.setData({ isSubmitting: false })
                  wx.showToast({
                    title: '支付调起失败',
                    icon: 'none'
                  })
                }
              })
            }else{
              that.payMent(info);
            }
          }else{
            that.setData({ isSubmitting: false })
            wx.showModal({
              title: '温馨提示',
              content: info.msg,
              showCancel: false
            })
          }
        },
        function fail(info) {
          wx.hideLoading()
          that.setData({ isSubmitting: false })
          wx.showToast({ title: '网络异常，请重试', icon: 'none' })
        }
      );
    } else {
      that.setData({ isSubmitting: false })
    }
  },
  //支付
  payMent: function (pay) {
    var that = this;
    wx.requestPayment({
      'timeStamp': pay.data.timeStamp,
      'nonceStr': pay.data.nonceStr,
      'package': pay.data.pkg,
      'signType': pay.data.signType,
      'paySign': pay.data.paySign,
      'success': function (res) {
        that.clearCartAfterPay()
        wx.navigateTo({
          url: '../productOrder/productOrder',
        })
      },
      'fail': function (res) {
        that.setData({ isSubmitting: false })
        wx.showToast({
          title: '支付取消!',
          icon: 'none'
        })
        wx.navigateBack(1)
      },
      'complete': function (res) {
      }
    })
  },
  // 支付成功后清除购物车中已购商品
  clearCartAfterPay: function() {
    let pay = this.data.payCart
    let cart = wx.getStorageSync("cart") || []
    let newcart = []
    for (let i = 0; i < cart.length; i++) {
      let matched = false
      for (let j = 0; j < pay.length; j++) {
        if (cart[i].id == pay[j].id) {
          matched = true
          let num = cart[i].number - pay[j].number
          if (num > 0) {
            let remain = Object.assign({}, cart[i])
            remain.number = num
            newcart.push(remain)
          }
          break
        }
      }
      if (!matched) {
        newcart.push(cart[i])
      }
    }
    wx.setStorageSync("cart", newcart)
  },
  // 取消支付 修改订单状态
  cancelPay: function (orderNo) {
    if (app.globalData.isLogin) {
      http.request(
        "/product/order/cancel",
        "1",
        "post",
        {
          "orderNo": orderNo
        },
        app.globalData.userDatatoken.accessToken,
        "",
        function success(info) {
        }
      );
    }
  },
  // 减少
  handleCartItemReduce: function (e) {
    let that = this
    let index = e.currentTarget.dataset.index
    let cart = that.data.payCart
    if (cart[index].number === 1) {
      cart.splice(index, 1)
    } else {
      cart[index].number -= 1
    }
    if (cart.length == 0) {
      wx.navigateBack(1)
    }
    that.setData({
      payCart: cart,
      showItems: cart.slice(0, 2)
    })
    that.calculateCartTotalPrice()
  },
  // 加
  handleCartItemAdd: function (e) {
    let that = this
    let index = e.currentTarget.dataset.index
    let cart = that.data.payCart

    cart[index].number += 1
    that.setData({
      payCart: cart,
      showItems: cart.slice(0, 2)
    })
    that.calculateCartTotalPrice()
  },
  getRoomList: function () {
    var that = this;
    http.request(
      "/member/index/getRoomList/" + that.data.storeId,
      "1",
      "get",
      {
        // "storeId": that.data.storeId,
      },
      app.globalData.userDatatoken.accessToken,
      "",
      function success(info) {
        if (info.code == 0) {
          let roomList = []
          let index = '';
          info.data.map((it, idx) => {
            roomList.push({ text: it.key, value: it.value })
            if(it.value==that.data.roomId){
              index = idx
            }
          })
          roomList.unshift({ text: "请选择房间", value: "" })
          that.setData({
            roomList: roomList,
            roomIndex: index
          })
        }
      },
      function fail(info) {
      }
    );
  },
  bindRoomSelect: function (e) {
    this.setData({
      roomIndex: e.detail.value,
    })
  },
})