import request from '@/utils/request'

// 创建门店支付分账配置
export function createStorePaySplit(data) {
  return request({
    url: '/member/store-pay-split/create',
    method: 'post',
    data: data
  })
}

// 更新门店支付分账配置
export function updateStorePaySplit(data) {
  return request({
    url: '/member/store-pay-split/update',
    method: 'put',
    data: data
  })
}

// 删除门店支付分账配置
export function deleteStorePaySplit(id) {
  return request({
    url: '/member/store-pay-split/delete?id=' + id,
    method: 'delete'
  })
}

// 获得门店支付分账配置
export function getStorePaySplit(id) {
  return request({
    url: '/member/store-pay-split/get?id=' + id,
    method: 'get'
  })
}

// 获得门店支付分账配置分页
export function getStorePaySplitPage(query) {
  return request({
    url: '/member/store-pay-split/page',
    method: 'get',
    params: query
  })
}

// 导出门店支付分账配置 Excel
export function exportStorePaySplitExcel(query) {
  return request({
    url: '/member/store-pay-split/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 获得门店下拉列表
export function getStoreList() {
  return request({
    url: '/index/getStoreList',
    method: 'get'
  })
}