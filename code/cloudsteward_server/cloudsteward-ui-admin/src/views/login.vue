<template>
  <main class="space-login">
    <section class="space-login__story" aria-label="共享空间管理平台">
      <div class="space-login__brand"><strong>云掌柜共享空间</strong></div>
      <div class="space-login__statement"><h1>让每一处空间，<br>经营有序。</h1><div class="space-login__accent"></div><p>空间 · 预约 · 设备 · 会员</p></div>
    </section>
    <section class="space-login__access">
      <div class="space-login__main">
        <p class="space-login__eyebrow">共享空间管理后台</p>
        <h2>{{ loginForm.loginType === 'uname' ? '欢迎回来' : '短信登录' }}</h2>
        <el-form ref="loginForm" :model="loginForm" :rules="rules" label-position="top" @submit.native.prevent="getCode">
          <template v-if="loginForm.loginType === 'uname'">
            <el-form-item label="账号" prop="username" for="login-username"><el-input id="login-username" v-model.trim="loginForm.username" placeholder="请输入账号" autocomplete="username" prefix-icon="el-icon-user" :disabled="loading" /></el-form-item>
            <el-form-item label="密码" prop="password" for="login-password"><el-input id="login-password" v-model="loginForm.password" placeholder="请输入密码" autocomplete="current-password" type="password" show-password prefix-icon="el-icon-lock" :disabled="loading" @keyup.enter.native="getCode" /></el-form-item>
          </template>
          <template v-else>
            <el-form-item label="手机号" prop="mobile"><el-input v-model.trim="loginForm.mobile" placeholder="请输入手机号" autocomplete="tel" :disabled="loading" /></el-form-item>
            <el-form-item label="短信验证码" prop="mobileCode"><el-input v-model="loginForm.mobileCode" placeholder="请输入验证码" autocomplete="one-time-code" @keyup.enter.native="getCode"><el-button slot="append" :disabled="mobileCodeTimer > 0 || sendingCode" @click="getSmsCode">{{ mobileCodeTimer > 0 ? mobileCodeTimer + ' 秒后重试' : '获取验证码' }}</el-button></el-input></el-form-item>
          </template>
          <el-form-item v-if="organizationRequired || loginForm.loginType === 'sms'" label="所属组织" prop="organizationName"><el-input v-model.trim="loginForm.organizationName" placeholder="请输入所属组织名称" autocomplete="organization" /><p class="space-login__field-help">{{ loginForm.loginType === 'sms' ? '短信登录需确认所属组织。' : '账号存在于多个组织，请补充名称以确认登录位置。' }}</p></el-form-item>
          <div v-if="errorMessage" class="space-login__error" role="alert"><i class="el-icon-warning-outline" aria-hidden="true"></i>{{ errorMessage }}</div>
          <div class="space-login__options"><el-checkbox v-model="loginForm.rememberMe">记住账号</el-checkbox><button type="button" class="space-login__link" @click="showHelp">忘记密码？联系管理员</button></div>
          <el-button class="space-login__submit" type="primary" :loading="loading" native-type="submit">{{ loading ? '正在登录' : '登录' }}</el-button>
          <p class="space-login__hint">使用账号即可登录所属组织。</p>
          <button type="button" class="space-login__switch" :disabled="loading" @click="switchMode">{{ loginForm.loginType === 'uname' ? '短信验证码登录' : '返回账号密码登录' }}<i class="el-icon-arrow-right" aria-hidden="true"></i></button>
        </el-form>
      </div>
      <footer class="space-login__footer">© {{ currentYear }} 云掌柜共享空间</footer>
    </section>
    <Verify ref="verify" captcha-type="blockPuzzle" :img-size="{width:'400px',height:'200px'}" @success="handleLogin" />
  </main>
</template>
<script>
import Verify from '@/components/Verifition/Verify'
import { sendSmsCode } from '@/api/login'
import { getTenantIdByName } from '@/api/system/tenant'
import { getCaptchaEnable } from '@/utils/ruoyi'
import { getUsername, getRememberMe, getTenantName, setUsername, setRememberMe, removeUsername, removeRememberMe, removePassword, setTenantId } from '@/utils/auth'
export default {
  name: 'Login', components: { Verify },
  data() {
    return {
      currentYear: new Date().getFullYear(), loading: false, sendingCode: false, mobileCodeTimer: 0, smsTimer: null, errorMessage: '', organizationRequired: false,
      loginForm: { loginType: 'uname', username: getUsername() || '', password: '', mobile: '', mobileCode: '', rememberMe: getRememberMe(), organizationName: '', captchaVerification: '' },
      rules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }], password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }],
        mobileCode: [{ required: true, message: '请输入短信验证码', trigger: 'blur' }], organizationName: [{ required: true, message: '请输入所属组织名称', trigger: 'blur' }]
      }
    }
  },
  created() { removePassword() }, beforeDestroy() { clearInterval(this.smsTimer) },
  methods: {
    showHelp() { this.$alert('请联系为你开通账号的组织管理员，核对账号、重置密码或确认权限。为保护账号安全，此处不提供未经验证的密码重置。', '登录帮助', { confirmButtonText: '知道了' }) },
    switchMode() {
      this.loginForm.loginType = this.loginForm.loginType === 'uname' ? 'sms' : 'uname'
      if (this.loginForm.loginType === 'sms') this.loginForm.organizationName = getTenantName() || ''
      this.errorMessage = ''; this.$nextTick(() => this.$refs.loginForm.clearValidate())
    },
    async getCode() {
      if (this.loading) return
      const valid = await this.$refs.loginForm.validate().catch(() => false)
      if (!valid) return
      if (this.loginForm.loginType === 'uname' && getCaptchaEnable()) this.$refs.verify.show()
      else this.handleLogin({})
    },
    async resolveSmsOrganization() {
      const result = await getTenantIdByName(this.loginForm.organizationName)
      if (result.data === null || result.data === undefined) throw new Error('所属组织不存在，请联系管理员确认')
      setTenantId(result.data)
    },
    async handleLogin(captcha = {}) {
      if (this.loading) return
      this.loading = true; this.errorMessage = ''
      try {
        if (this.loginForm.loginType === 'sms') await this.resolveSmsOrganization()
        this.loginForm.captchaVerification = captcha.captchaVerification || ''
        await this.$store.dispatch(this.loginForm.loginType === 'sms' ? 'SmsLogin' : 'Login', this.loginForm)
        if (this.loginForm.rememberMe) { setUsername(this.loginForm.username); setRememberMe(true) } else { removeUsername(); removeRememberMe() }
        let target = this.$route.query.redirect || '/index'
        try { target = decodeURIComponent(target) } catch (_) { target = '/index' }
        if (!target.startsWith('/') || target.startsWith('//') || target.startsWith('/login')) target = '/index'
        await this.$router.replace(target)
      } catch (error) {
        this.errorMessage = error.message || '登录失败，请核对账号及密码后重试'
        if (error.code === 1002999001) this.organizationRequired = true
      } finally { this.loading = false }
    },
    async getSmsCode() {
      if (this.mobileCodeTimer || this.sendingCode) return
      this.errorMessage = ''
      try {
        await Promise.all(['mobile', 'organizationName'].map(field => new Promise((resolve, reject) => this.$refs.loginForm.validateField(field, error => error ? reject(new Error(error)) : resolve()))))
        this.sendingCode = true; await this.resolveSmsOrganization(); await sendSmsCode(this.loginForm.mobile, 21)
        this.mobileCodeTimer = 60; clearInterval(this.smsTimer)
        this.smsTimer = setInterval(() => { if (--this.mobileCodeTimer <= 0) clearInterval(this.smsTimer) }, 1000)
        this.$message.success('验证码已发送')
      } catch (error) { this.errorMessage = error.message || '验证码发送失败，请稍后重试' } finally { this.sendingCode = false }
    }
  }
}
</script>
<style lang="scss" scoped>
.space-login { --login-ink: #101f35; --login-muted: #667085; --login-blue: #165dff; min-height: 100vh; display: grid; grid-template-columns: 51.5% 48.5%; background: #fff; color: var(--login-ink); font-family: 'PingFang SC','Microsoft YaHei',sans-serif; }
.space-login__story { min-height: 100vh; position: relative; background: #eaf0f8 url('~@/assets/images/shared-space-login.jpg') center center / cover no-repeat; padding: clamp(44px,5.3vw,80px); }
.space-login__brand { display: flex; flex-direction: column; gap: 8px; strong { font-size: 34px; font-weight: 750; letter-spacing: 2px; } span { font-size: 19px; } }
.space-login__statement { margin-top: 66px; h1 { font-size: clamp(36px,4.1vw,60px); font-weight: 750; line-height: 1.34; margin: 0; letter-spacing: -1px; } p { font-size: 25px; letter-spacing: 3px; margin-top: 30px; } }
.space-login__accent { width: 70px; height: 7px; border-radius: 4px; background: #2bc79b; margin-top: 30px; }
.space-login__access { min-height: 100vh; padding: clamp(40px,8.8vh,112px) clamp(40px,6vw,88px) 32px; display: flex; flex-direction: column; }
.space-login__main { width: 100%; max-width: 520px; margin: 48px auto 60px; }
.space-login__eyebrow { border-left: 5px solid var(--login-blue); padding-left: 18px; font-size: 22px; line-height: 25px; margin: 0 0 32px; }
.space-login h2 { margin: 0 0 48px; font-size: 56px; font-weight: 750; line-height: 1.2; }
.space-login ::v-deep .el-form-item { margin-bottom: 28px; }
.space-login ::v-deep .el-form-item__label { font-size: 18px; color: var(--login-ink); font-weight: 600; padding-bottom: 12px; line-height: 24px; }
.space-login ::v-deep .el-form-item.is-required .el-form-item__label:before { display: none; }
.space-login ::v-deep .el-input__inner { height: 56px; border-radius: 6px; border-color: #b8c1ce; font-size: 18px !important; color: var(--login-ink); }
.space-login ::v-deep .el-input__inner::placeholder { color: #7e8794; }
.space-login ::v-deep .el-input__icon { line-height: 56px; font-size: 20px; }
.space-login__options { display: flex; align-items: center; justify-content: space-between; gap: 15px; margin: 36px 0 46px; flex-wrap: wrap; }
.space-login ::v-deep .el-checkbox__label { color: #283648; font-size: 17px; }
.space-login__link, .space-login__switch { border: 0; padding: 4px 0; background: transparent; color: #1653d8; cursor: pointer; font-size: 15px; line-height: 1.5; }
.space-login__submit { width: 100%; height: 58px; border-radius: 6px; background: var(--login-blue) !important; border-color: var(--login-blue) !important; font-size: 20px; font-weight: 600; }
.space-login__hint { text-align: center; font-size: 16px; color: var(--login-muted); margin: 25px 0 28px; line-height: 1.6; }
.space-login__switch { display: block; margin: auto; font-size: 17px; i { margin-left: 10px; } }
.space-login__error { padding: 12px 14px; background: #fff1f0; border: 1px solid #ffc9c4; border-radius: 6px; color: #ad2424; font-size: 14px; line-height: 1.6; i { margin-right: 8px; } }
.space-login__field-help { font-size: 13px; line-height: 1.6; color: var(--login-muted); margin: 6px 0 0; }
.space-login__footer { margin-top: auto; border-top: 1px solid #e5e9ef; padding-top: 20px; text-align: center; color: #737e8d; font-size: 13px; line-height: 1.8; a { display: block; color: #667085; } }
.space-login button:focus-visible, .space-login a:focus-visible { outline: 3px solid #75a5ff; outline-offset: 4px; }
@media (max-width: 1050px) { .space-login { grid-template-columns: 45% 55%; } .space-login__story { padding: 38px; } .space-login__statement h1 { font-size: 37px; } .space-login__statement p { font-size: 19px; letter-spacing: 1px; } .space-login__access { padding: 50px 38px 24px; } .space-login h2 { font-size: 42px; } }
@media (max-width: 700px) { .space-login { display: block; } .space-login__story { min-height: 0; padding: 24px; background: #edf3fb; } .space-login__brand { flex-direction: row; align-items: baseline; flex-wrap: wrap; strong { font-size: 24px; } span { font-size: 14px; } } .space-login__statement { display: none; } .space-login__access { min-height: calc(100vh - 84px); padding: 35px 24px 20px; } .space-login__main { margin: 0 auto 45px; } .space-login__eyebrow { font-size: 17px; margin-bottom: 24px; } .space-login h2 { font-size: 36px; margin-bottom: 32px; } .space-login__options { margin: 26px 0 30px; } }
</style>
