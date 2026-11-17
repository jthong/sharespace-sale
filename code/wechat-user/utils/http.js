/**
 *网络请求封装（支持超时、重试、错误分类）
 */
var util = require("util");
var app = getApp();

var REQUEST_TIMEOUT = 30000;
var MAX_RETRY = 2;

function request(url, urltype, method, data, token, message, successBack, failBack, onUnauthorized) {
  if (message != "") {
    wx.showLoading({ mask: true, title: message })
  }
  _doRequest(url, urltype, method, data, token, message, successBack, failBack, 0, onUnauthorized)
}

function _doRequest(url, urltype, method, data, token, message, successBack, failBack, retryCount, onUnauthorized) {
  var atempurl = app.globalData.baseUrl + url;
  var aheadertoken = token || "";

  wx.request({
    url: atempurl,
    data: data,
    timeout: REQUEST_TIMEOUT,
    header: {
      'tenant-id': app.globalData.tenantId,
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + aheadertoken
    },
    method: method,
    success: function (res) {
      if (res.statusCode == 401 || (res.statusCode == 200 && res.data.code == 401)) {
        if (onUnauthorized) {
          onUnauthorized()
        } else {
          wx.navigateTo({ url: '/pages/login/login' })
        }
      } else if (res.statusCode == 200) {
        successBack(res.data)
      } else {
        failBack({ errMsg: '请求失败(' + res.statusCode + ')', data: res.data })
      }
      if (message != "") {
        wx.hideLoading()
      }
    },
    fail: function (err) {
      if (retryCount < MAX_RETRY) {
        var delay = Math.pow(2, retryCount) * 1000;
        setTimeout(function () {
          _doRequest(url, urltype, method, data, token, message, successBack, failBack, retryCount + 1, onUnauthorized)
        }, delay);
        return
      }
      if (message != "") {
        wx.hideLoading()
      }
      failBack(err)
    },
  })
}
/**
 *
 *提问上传文件 
 */
function uploadFile(url, data, message, success, fail) {
  wx.showNavigationBarLoading()
  if (message != "") {
    wx.showLoading({
      title: message,
    })
  }

  wx.uploadFile({
    url: app.globalData.baseUrl + url, //仅为示例，非真实的接口地址
    filePath: data.temFile,
    name: data.file,
    formData: {
      token: app.globalData.userData.token,
    },
    header: {
      "Content-Type": "multipart/form-data"
    },
    success: function (res) {
      wx.hideNavigationBarLoading()

      if (res.statusCode == 200) {
        success(res.data)
      } else {
        fail(res)
      }
    },
    fail: function (err) {
      wx.hideNavigationBarLoading()
      if (message != "") {
        wx.hideLoading()
      }
      fail(err)
    },
  })
}

module.exports = {
  request: request,
  uploadFile: uploadFile
}
