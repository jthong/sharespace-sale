<template>
  <el-dialog title="微信服务商收款配置" :visible="visible" width="min(540px, 94vw)" append-to-body
    :close-on-click-modal="false" :before-close="close">
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon role="alert" />
    <el-form ref="form" :model="form" :rules="rules" label-position="top" @submit.native.prevent="save">
      <el-form-item label="系统门店"><span>{{ storeName || '当前门店' }}（ID：{{ storeId }}）</span></el-form-item>
      <el-form-item label="微信特约商户号" prop="mchId">
        <el-input v-model.trim="form.mchId" :disabled="loading || saving || !loaded" maxlength="32"
          placeholder="请输入服务商进件后的特约商户号" autocomplete="off" @keyup.enter.native="save" />
      </el-form-item>
      <p>支付模式：微信支付服务商。每个门店仅需配置对应的特约商户号，服务商凭证由系统统一管理。</p>
      <p>更换商户号仅影响新支付单；历史支付和退款沿用原商户。</p>
      <p v-if="loading" role="status">正在读取当前绑定…</p>
    </el-form>
    <span slot="footer"><el-button :disabled="saving" @click="close">取消</el-button>
      <el-button type="primary" :loading="saving" :disabled="loading || !loaded" @click="save">保存绑定</el-button></span>
  </el-dialog>
</template>

<script>
import { getWechatMerchant, bindWechatMerchant } from '@/api/member/storeInfo'
export default {
  name: 'WechatMerchantBinding',
  props: { visible: Boolean, storeId: [Number, String], storeName: String },
  data() {
    return { form: { mchId: '' }, loaded: false, loading: false, saving: false, error: '', version: 0,
      rules: { mchId: [{ required: true, message: '请输入微信特约商户号', trigger: 'blur' },
        { pattern: /^\d{8,32}$/, message: '请输入 8 至 32 位数字', trigger: 'blur' }] } }
  },
  watch: {
    visible(value) { if (value) this.load(); else this.version++ },
    storeId() { if (this.visible) this.load() }
  },
  methods: {
    close() { if (!this.saving) this.$emit('update:visible', false) },
    async load() {
      const version = ++this.version
      this.form.mchId = ''; this.error = ''; this.loaded = false; this.loading = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
      try {
        const response = await getWechatMerchant(this.storeId)
        if (version !== this.version) return
        this.form.mchId = response.data || ''; this.loaded = true
      } catch (error) { if (version === this.version) this.error = error.message || '读取绑定失败，请关闭后重试' }
      finally { if (version === this.version) this.loading = false }
    },
    async save() {
      if (this.saving || this.loading || !this.loaded) return
      this.saving = true
      const version = this.version
      const data = { storeId: this.storeId, mchId: this.form.mchId }
      const valid = await this.$refs.form.validate().catch(() => false)
      if (!valid) { this.saving = false; return }
      this.error = ''
      try {
        await bindWechatMerchant(data)
        if (version !== this.version) return
        this.$message.success('微信商户号保存成功')
        this.$emit('saved'); this.$emit('update:visible', false)
      } catch (error) { if (version === this.version) this.error = error.message || '保存绑定失败，请重试' }
      finally { this.saving = false }
    }
  }
}
</script>
