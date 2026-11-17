import request from '@/utils/request'

// 创建门店支付收款配置
export function createStorePayConfig(data) {
  return request({
    url: '/member/store-pay-config/create',
    method: 'post',
    data: data
  })
}

// 更新门店支付收款配置
export function updateStorePayConfig(data) {
  return request({
    url: '/member/store-pay-config/update',
    method: 'put',
    data: data
  })
}

// 删除门店支付收款配置
export function deleteStorePayConfig(id) {
  return request({
    url: '/member/store-pay-config/delete?id=' + id,
    method: 'delete'
  })
}

// 获得门店支付收款配置
export function getStorePayConfig(id) {
  return request({
    url: '/member/store-pay-config/get?id=' + id,
    method: 'get'
  })
}

// 获得门店支付收款配置分页
export function getStorePayConfigPage(query) {
  return request({
    url: '/member/store-pay-config/page',
    method: 'get',
    params: query
  })
}

// 导出门店支付收款配置 Excel
export function exportStorePayConfigExcel(query) {
  return request({
    url: '/member/store-pay-config/export-excel',
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