<template>
  <el-dialog :title="platformName + '团购授权'" :visible="visible" width="min(540px, 94vw)"
    append-to-body :close-on-click-modal="false" @close="$emit('update:visible', false)"
    :before-close="close">
    <p>当前门店：{{ storeName }}（ID：{{ storeId }}）</p>
    <p v-if="loading" role="status"><i class="el-icon-loading" aria-hidden="true" /> 正在获取{{ platformName }}授权链接…</p>
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon role="alert" />
    <template v-if="url && !loading">
      <p>请前往{{ platformName }}页面登录商户账号并完成授权，完成后返回本系统。</p>
      <a :href="url" target="_blank" rel="noopener noreferrer" class="el-button el-button--primary">打开{{ platformName }}授权页面 <i class="el-icon-top-right" aria-hidden="true" /></a>
    </template>
    <span slot="footer"><el-button @click="close">关闭</el-button>
      <el-button v-if="error" type="primary" :loading="loading" @click="load()">重新获取链接</el-button></span>
  </el-dialog>
</template>
<script>
import { getGroupBuyAuthUrl } from '@/api/member/storeInfo'
export default {
  name: 'GroupBuyAuthorization',
  props: { visible: Boolean, storeId: [String, Number], storeName: String, platform: Number },
  data() { return { loading: false, error: '', url: '', version: 0 } },
  computed: {
    platformName() { return this.platform === 1 ? '美团' : '抖音' },
    requestKey() { return this.visible ? `${this.storeId}:${this.platform}` : null }
  },
  watch: {
    requestKey(value) { if (value) this.load(true); else this.version++ }
  },
  beforeDestroy() { this.version++ },
  methods: {
    close() { this.$emit('update:visible', false) },
    async load(force = false) {
      if (this.loading && !force) return
      const version = ++this.version
      this.loading = true; this.error = ''; this.url = ''
      try {
        const response = await getGroupBuyAuthUrl({ storeId: this.storeId, groupPayType: this.platform })
        if (version !== this.version) return
        const url = new URL(response.data)
        if (url.protocol !== 'https:') throw new Error('授权链接无效，请重试')
        this.url = url.href
      } catch (error) { if (version === this.version) this.error = error.message || '获取授权链接失败，请重试' }
      finally { if (version === this.version) this.loading = false }
    }
  }
}
</script>
