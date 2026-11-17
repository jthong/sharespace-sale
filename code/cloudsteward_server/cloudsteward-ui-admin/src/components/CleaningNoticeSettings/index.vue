<template>
  <el-dialog :title="storeName ? storeName + ' · 保洁通知' : '保洁通知'" :visible="visible"
    width="min(640px, 94vw)" append-to-body :close-on-click-modal="false" :before-close="close">
    <el-alert v-if="loadError" :title="loadError" type="error" :closable="false" show-icon role="alert" />
    <p v-if="loading" role="status">正在读取保洁通知配置…</p>
    <template v-if="settings">
      <el-form label-position="top" :disabled="Boolean(saving)" @submit.native.prevent="saveChannel">
        <el-form-item label="订单结束通知渠道" :error="channelError" for="cleaning-channel">
          <el-select id="cleaning-channel" v-model="channel" @change="channelError = ''">
            <el-option label="企业微信群机器人" value="wecom" />
            <el-option label="微信小程序订阅消息" value="miniapp" />
          </el-select>
          <div v-if="channel === 'wecom'" class="notice-help">
            {{ settings.wecomConfigured ? '已配置门店企业微信群机器人。' : '请在修改门店时填写「企业微信webhook」。' }}
          </div>
          <div v-else class="notice-help">保洁员需在小程序的保洁任务页订阅提醒。</div>
        </el-form-item>
      </el-form>
      <el-divider content-position="left">微信小程序订阅模板</el-divider>
      <p class="notice-help" v-if="!settings.appId">门店所属租户尚未配置小程序 AppID。</p>
      <template v-else>
        <p class="notice-help">使用同一 AppID 的门店共用模板（{{ settings.appId }}）。</p>
        <p v-if="!settings.canEditTemplate" class="notice-help">
          {{ settings.templateId ? '订阅模板已配置。' : '订阅模板尚未配置。' }} 请联系超级管理员修改模板。
        </p>
        <el-form v-else label-position="top" :disabled="Boolean(saving)" @submit.native.prevent="saveTemplate">
          <el-form-item label="订阅模板 ID" for="cleaning-template-id" :error="templateIdError">
            <el-input id="cleaning-template-id" v-model.trim="templateId" maxlength="100"
              placeholder="填写微信公众平台的模板 ID" @input="templateIdError = ''" />
          </el-form-item>
          <div v-for="(field, index) in fields" :key="index" class="notice-field">
            <el-form-item label="微信字段编号" :for="'cleaning-key-' + index" :error="fieldErrors[index]">
              <el-input :id="'cleaning-key-' + index" v-model.trim="field.key" maxlength="32"
                placeholder="如 thing1" @input="fieldErrors = []" />
            </el-form-item>
            <el-form-item label="填入内容" :for="'cleaning-source-' + index">
              <el-select :id="'cleaning-source-' + index" v-model="field.value" placeholder="请选择内容"
                @change="fieldErrors = []">
                <el-option v-for="option in sources" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
            <el-button type="text" :disabled="fields.length === 1" @click="removeField(index)">删除</el-button>
          </div>
          <div class="notice-template-actions">
            <el-button size="small" :disabled="fields.length >= 10" @click="addField">添加字段</el-button>
            <el-button size="small" :loading="saving === 'template'" @click="saveTemplate">保存模板</el-button>
          </div>
          <p v-if="templateError" class="notice-error" role="alert">{{ templateError }}</p>
        </el-form>
      </template>
    </template>
    <span slot="footer">
      <el-button :disabled="Boolean(saving)" @click="close">关闭</el-button>
      <el-button type="primary" :disabled="!settings || saving === 'template'" :loading="saving === 'channel'"
        @click="saveChannel">保存渠道</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { getCleaningNotice, saveCleaningNoticeChannel, saveCleaningNoticeTemplate } from '@/api/member/storeInfo'

const sources = [
  { label: '房间名称', value: 'roomName', keyPattern: /^thing\d+$/ },
  { label: '门店名称', value: 'storeName', keyPattern: /^thing\d+$/ },
  { label: '订单结束时间', value: 'finishedAt', keyPattern: /^(thing|time|date)\d+$/ },
  { label: '下一单预约开始时间', value: 'nextBookingAt', keyPattern: /^(thing|time|date)\d+$/ },
  { label: '订单号', value: 'orderNo', keyPattern: /^(thing|character_string)\d+$/ },
  { label: '保洁状态', value: 'status', keyPattern: /^(thing|phrase)\d+$/ },
  { label: '保洁提示', value: 'remark', keyPattern: /^thing\d+$/ }
]

export default {
  name: 'CleaningNoticeSettings',
  props: { visible: Boolean, storeId: [Number, String], storeName: String },
  data() {
    return {
      sources, settings: null, loading: false, saving: '', loadError: '', channelError: '',
      channel: 'wecom', templateId: '', templateIdError: '', fields: [], fieldErrors: [], templateError: ''
    }
  },
  created() { this.load() },
  methods: {
    close() {
      if (!this.saving) this.$emit('update:visible', false)
    },
    async load() {
      this.loading = true
      try {
        const { data } = await getCleaningNotice(this.storeId)
        this.channel = data.channel
        this.templateId = data.templateId || ''
        this.fields = Object.entries(data.fields).map(([key, value]) => ({ key, value }))
        if (!this.fields.length) this.addField()
        this.settings = data
      } catch (error) {
        this.loadError = error.message || '读取配置失败，请重新打开'
      } finally {
        this.loading = false
      }
    },
    addField() {
      this.fields.push({ key: '', value: '' })
    },
    removeField(index) {
      this.fields.splice(index, 1)
      this.fieldErrors = []
    },
    async saveChannel() {
      if (this.saving || !this.settings) return
      this.channelError = ''
      if (this.channel === 'miniapp' && !this.settings.templateId) {
        this.channelError = '请先保存微信小程序订阅模板'
        return
      }
      this.saving = 'channel'
      try {
        await saveCleaningNoticeChannel({ storeId: this.storeId, channel: this.channel })
        this.$message.success('保洁通知渠道已保存')
        this.$emit('update:visible', false)
      } catch (error) {
        this.channelError = error.message || '保存渠道失败，请重试'
      } finally {
        this.saving = ''
      }
    },
    async saveTemplate() {
      if (this.saving || !this.settings || !this.settings.canEditTemplate) return
      this.templateError = ''
      this.templateIdError = /^[A-Za-z0-9_-]{20,100}$/.test(this.templateId) ? '' : '请输入有效的微信订阅模板 ID'
      const keys = new Set()
      this.fieldErrors = this.fields.map(({ key, value }) => {
        if (!key || !value) return '请填写字段编号并选择内容'
        if (keys.has(key)) return '字段编号重复'
        keys.add(key)
        const source = this.sources.find(option => option.value === value)
        return source && source.keyPattern.test(key) ? '' : '字段类型与内容不匹配'
      })
      if (this.templateIdError || this.fieldErrors.some(Boolean)) return
      const fields = {}
      this.fields.forEach(({ key, value }) => { fields[key] = value })
      this.saving = 'template'
      try {
        await saveCleaningNoticeTemplate({ storeId: this.storeId, templateId: this.templateId, fields })
        this.settings.templateId = this.templateId
        this.channelError = ''
        this.$message.success('保洁订阅模板已保存')
      } catch (error) {
        this.templateError = error.message || '保存模板失败，请重试'
      } finally {
        this.saving = ''
      }
    }
  }
}
</script>

<style scoped>
.notice-help { color: #606266; margin: 8px 0; line-height: 1.5; }
.notice-field { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) auto; gap: 12px; align-items: start; }
.notice-field > .el-button { margin-top: 27px; }
.notice-template-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.notice-template-actions .el-button + .el-button { margin-left: 0; }
.notice-error { color: #c03639; }
</style>
