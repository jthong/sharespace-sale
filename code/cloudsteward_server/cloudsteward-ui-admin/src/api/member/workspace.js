import request from '@/utils/request'
export const workspacePage = (resource, data) => request({ url: '/member/workspace/page/' + resource, method: 'post', data })
export const workspaceSave = (resource, data) => request({ url: '/member/workspace/save/' + resource, method: 'post', data, headers: { inlineError: true } })
export const workspaceAction = (action, data) => request({ url: '/member/workspace/action/' + action, method: 'post', data, headers: { inlineError: true } })
export const workspaceOverview = storeId => request({ url: '/member/workspace/overview', method: 'get', params: { storeId } })
