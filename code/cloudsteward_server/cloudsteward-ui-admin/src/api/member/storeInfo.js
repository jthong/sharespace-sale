import request from '@/utils/request'

export function getCleaningNotice(storeId) {
  return request({ url: '/member/store-info/cleaning-notice', method: 'get', params: { storeId }, headers: { inlineError: true } })
}

export function saveCleaningNoticeChannel(data) {
  return request({ url: '/member/store-info/cleaning-notice', method: 'put', data, headers: { inlineError: true } })
}

export function saveCleaningNoticeTemplate(data) {
  return request({ url: '/member/store-info/cleaning-notice-template', method: 'put', data, headers: { inlineError: true } })
}

export function getGroupBuyBinding(storeId) {
  return request({ url: '/member/store-info/group-buy-binding', method: 'get', params: { storeId } })
}

export function bindGroupBuyStore(data) {
  return request({ url: '/member/store-info/group-buy-binding', method: 'put', data, headers: { inlineError: true } })
}

export function getGroupBuyAuthUrl(data) {
  return request({ url: '/member/store-info/group-buy-auth-url', method: 'post', data, headers: { inlineError: true } })
}

// 创建门店管理
export function createStoreInfo(data) {
  return request({
    url: '/member/store-info/create',
    method: 'post',
    data: data
  })
}

// 更新门店管理
export function updateStoreInfo(data) {
  return request({
    url: '/member/store-info/update',
    method: 'put',
    data: data
  })
}

// 删除门店管理
export function deleteStoreInfo(id) {
  return request({
    url: '/member/store-info/delete?id=' + id,
    method: 'delete'
  })
}

// 获得门店管理
export function getStoreInfo(id) {
  return request({
    url: '/member/store-info/get?id=' + id,
    method: 'get'
  })
}

// 获得门店管理分页
export function getStoreInfoPage(query) {
  return request({
    url: '/member/store-info/page',
    method: 'get',
    params: query
  })
}

// 导出门店管理 Excel
export function exportStoreInfoExcel(query) {
  return request({
    url: '/member/store-info/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
export function getMeituanScope(id){
  return request({
    url: '/member/store-info/meituanScope/'+id,
    method: 'get'
  })
}

export function renewStore(data){
  return request({
    url: '/member/store-info/renew',
    method: 'post',
    data: data
  })
}

export function setTemplate(data){
  return request({
    url: '/member/store-info/setTemplate',
    method: 'post',
    data: data
  })
}

export function getWechatMerchant(storeId) {
  return request({ url: "/member/store-info/wechat-merchant", method: "get", params: { storeId } })
}
export function bindWechatMerchant(data) {
  return request({ url: "/member/store-info/wechat-merchant", method: "put", data, headers: { inlineError: true } })
}
