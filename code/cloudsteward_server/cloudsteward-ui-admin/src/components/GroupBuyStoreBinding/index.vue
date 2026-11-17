<template>
  <el-dialog title="绑定团购门店 ID" :visible="visible" width="min(540px, 94vw)" append-to-body
    :close-on-click-modal="false" :before-close="close">
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon role="alert" />
    <el-form ref="form" :model="form" :rules="rules" label-position="top" @submit.native.prevent="save">
      <el-form-item label="系统门店"><span>{{ storeName || '当前门店' }}（ID：{{ storeId }}）</span></el-form-item>
      <el-form-item label="团购服务商门店 ID" prop="shopId">
        <el-input v-model.trim="form.shopId" :disabled="loading || saving || !loaded" maxlength="64"
          placeholder="请输入团购服务商提供的门店 ID" autocomplete="off" @keyup.enter.native="save" />
      </el-form-item>
      <p>一个团购服务商门店 ID 仅能绑定一个系统门店。授权、验券和核销将使用此 ID。</p>
      <p>保存后，请在门店列表点击“美团团购授权”或“抖音团购授权”继续操作。</p>
      <p v-if="loading" role="status">正在读取当前绑定…</p>
    </el-form>
    <span slot="footer"><el-button :disabled="saving" @click="close">取消</el-button>
      <el-button type="primary" :loading="saving" :disabled="loading || !loaded" @click="save">保存绑定</el-button></span>
  </el-dialog>
</template>

<script>
import { getGroupBuyBinding, bindGroupBuyStore } from '@/api/member/storeInfo'
export default {
  name: 'GroupBuyStoreBinding',
  props: { visible: Boolean, storeId: [Number, String], storeName: String },
  data() {
    return { form: { shopId: '' }, loaded: false, loading: false, saving: false, error: '', version: 0,
      rules: { shopId: [{ required: true, message: '请输入团购服务商门店 ID', trigger: 'blur' },
        { pattern: /^[1-9]\d{0,63}$/, message: '请输入 1 至 64 位数字，不能以 0 开头', trigger: 'blur' }] } }
  },
  watch: {
    visible(value) { if (value) this.load(); else this.version++ },
    storeId() { if (this.visible) this.load() }
  },
  methods: {
    close() { if (!this.saving) this.$emit('update:visible', false) },
    async load() {
      const version = ++this.version
      this.form.shopId = ''; this.error = ''; this.loaded = false; this.loading = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
      try {
        const response = await getGroupBuyBinding(this.storeId)
        if (version !== this.version) return
        this.form.shopId = response.data || ''; this.loaded = true
      } catch (error) { if (version === this.version) this.error = error.message || '读取绑定失败，请关闭后重试' }
      finally { if (version === this.version) this.loading = false }
    },
    async save() {
      if (this.saving || this.loading || !this.loaded) return
      this.saving = true
      const version = this.version
      const data = { storeId: this.storeId, shopId: this.form.shopId }
      const valid = await this.$refs.form.validate().catch(() => false)
      if (!valid) { this.saving = false; return }
      this.error = ''
      try {
        await bindGroupBuyStore(data)
        if (version !== this.version) return
        this.$message.success('团购门店 ID 绑定成功')
        this.$emit('saved'); this.$emit('update:visible', false)
      } catch (error) { if (version === this.version) this.error = error.message || '保存绑定失败，请重试' }
      finally { this.saving = false }
    }
  }
}
</script>
