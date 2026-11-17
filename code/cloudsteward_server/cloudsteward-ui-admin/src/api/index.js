import request from '@/utils/request'


//获取系统配置
export function getAppConfig() {
  return request({
    url: '/index/getAppConfig',
    method: 'post'
  })
}


export function saveAppConfig(data) {
  return request({
    url: '/index/saveAppConfig',
    method: 'post',
    data: data
  })
}
