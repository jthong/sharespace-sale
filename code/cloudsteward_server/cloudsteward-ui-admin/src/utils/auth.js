
const AccessTokenKey = 'ACCESS_TOKEN'
const RefreshTokenKey = 'REFRESH_TOKEN'

// ========== Token 相关 ==========

export function getAccessToken() {
  return localStorage.getItem(AccessTokenKey)
}

export function getRefreshToken() {
  return localStorage.getItem(RefreshTokenKey)
}

export function setToken(token) {
  if (token.tenantId !== undefined && token.tenantId !== null) setTenantId(token.tenantId)
  if (token.tenantName) setTenantName(token.tenantName)
  localStorage.setItem(AccessTokenKey, token.accessToken)
  localStorage.setItem(RefreshTokenKey, token.refreshToken)
}

export function removeToken() {
  localStorage.removeItem(AccessTokenKey)
  localStorage.removeItem(RefreshTokenKey)
  removeTenantId()
}

// ========== 账号相关 ==========

const UsernameKey = 'USERNAME'
const PasswordKey = 'PASSWORD'
const RememberMeKey = 'REMEMBER_ME'

export function getUsername() {
  return localStorage.getItem(UsernameKey)
}

export function setUsername(username) {
  localStorage.setItem(UsernameKey, username)
}

export function removeUsername() {
  localStorage.removeItem(UsernameKey)
}

export function removePassword() {
  localStorage.removeItem(PasswordKey)
}

export function getRememberMe() {
  return localStorage.getItem(RememberMeKey) === 'true'
}

export function setRememberMe(rememberMe) {
  localStorage.setItem(RememberMeKey, rememberMe)
}

export function removeRememberMe() {
  localStorage.removeItem(RememberMeKey)
}

// ========== 租户相关 ==========

const TenantIdKey = 'TENANT_ID'
const TenantNameKey = 'TENANT_NAME'

export function getTenantName() {
  return localStorage.getItem(TenantNameKey)
}

export function setTenantName(username) {
  localStorage.setItem(TenantNameKey, username)
}

export function removeTenantName() {
  localStorage.removeItem(TenantNameKey)
}

export function getTenantId() {
  return localStorage.getItem(TenantIdKey)
}

export function setTenantId(username) {
  localStorage.setItem(TenantIdKey, username)
}

export function removeTenantId() {
  localStorage.removeItem(TenantIdKey)
}

export class getToken {
}
