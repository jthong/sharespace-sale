import request from '@/utils/request'

// 创建门店模板
export function createStoreTemplate(data) {
  return request({
    url: '/member/store-template/create',
    method: 'post',
    data: data
  })
}

// 更新门店模板
export function updateStoreTemplate(data) {
  return request({
    url: '/member/store-template/update',
    method: 'put',
    data: data
  })
}

// 删除门店模板
export function deleteStoreTemplate(id) {
  return request({
    url: '/member/store-template/delete?id=' + id,
    method: 'delete'
  })
}

// 获得门店模板
export function getStoreTemplate(id) {
  return request({
    url: '/member/store-template/get?id=' + id,
    method: 'get'
  })
}

// 获得门店模板分页
export function getStoreTemplatePage(query) {
  return request({
    url: '/member/store-template/page',
    method: 'get',
    params: query
  })
}

// 导出门店模板 Excel
export function exportStoreTemplateExcel(query) {
  return request({
    url: '/member/store-template/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
