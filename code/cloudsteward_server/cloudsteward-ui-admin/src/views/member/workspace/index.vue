<template>
  <div class="workspace-page">
    <header class="workspace-header"><div><h1>{{ config.title }}</h1><p>{{ config.description }}</p></div><div class="workspace-context"><span>当前门店</span><el-select v-model="storeId" filterable placeholder="请选择门店" aria-label="当前门店" @change="changeStore"><el-option v-for="store in stores" :key="store.storeId" :label="store.storeName" :value="store.storeId" /></el-select></div></header>
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="workspace-alert" />
    <el-empty v-if="!stores.length && !initializing" description="暂无可管理的门店，请联系组织管理员配置门店权限。" />
    <template v-else-if="storeId">
      <template v-if="resource === 'overview'">
        <section class="workspace-metrics" v-loading="loading"><div v-for="metric in metrics" :key="metric.key"><span>{{ metric.label }}</span><strong>{{ metric.money ? '¥ ' : '' }}{{ overview[metric.key] == null ? '—' : overview[metric.key] }}</strong></div></section>
        <section class="workspace-trend"><h2>近 14 天订单净收</h2><p>订单实付减退款，单位：元。</p><div ref="trend" class="workspace-chart"></div></section>
      </template>
      <template v-else>
        <div class="workspace-toolbar">
          <el-form :inline="true" size="small" @submit.native.prevent="search"><el-form-item label="搜索"><el-input v-model.trim="query.keyword" placeholder="名称、手机号或编号" clearable @keyup.enter.native="search" /></el-form-item><el-form-item><el-button type="primary" icon="el-icon-search" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item></el-form>
          <div class="workspace-tools"><el-button v-if="config.fields && !config.singleton" v-hasPermi="['member:workspace:manage']" type="primary" size="small" icon="el-icon-plus" @click="edit()">新增</el-button><el-button v-for="key in config.topActions" :key="key" v-hasPermi="['member:workspace:manage']" size="small" @click="runAction(key)">{{ actionDefinitions[key].label }}</el-button><el-button size="small" :disabled="!rows.length" @click="exportPage">导出当前页</el-button><el-button size="small" icon="el-icon-refresh" aria-label="刷新数据" @click="load" /></div>
        </div>
        <section v-if="config.cards" v-loading="loading" class="workspace-spaces"><article v-for="row in rows" :key="row.roomId" class="workspace-space"><div class="workspace-space__heading"><h2>{{ row.roomName }}</h2><el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'info'">{{ spaceStates[row.status] || '未知状态' }}</el-tag></div><p>¥ {{ row.price || 0 }} <span>/ 小时</span></p><div class="workspace-space__actions"><el-button size="small" @click="showDetail(row)">详情</el-button><el-button v-if="config.fields" v-hasPermi="['member:workspace:manage']" size="small" @click="edit(row)">配置</el-button><el-button v-for="key in visibleActions(row)" :key="key" v-hasPermi="['member:workspace:manage']" size="small" :type="key === 'openSpace' ? 'primary' : 'default'" @click="runAction(key, row)">{{ actionDefinitions[key].label }}</el-button></div></article></section>
        <el-table v-else v-loading="loading" :data="rows" border stripe :empty-text="loading ? '正在加载…' : '暂无记录'" :row-key="idKey" style="width:100%">
          <el-table-column v-for="column in config.columns" :key="column.key" :prop="column.key" :label="column.label" :min-width="column.key.toLowerCase().includes('time') ? 165 : 120" show-overflow-tooltip><template slot-scope="scope"><el-tag v-if="column.key === 'status' && config.states" size="small">{{ display(scope.row, column.key) }}</el-tag><span v-else>{{ display(scope.row, column.key) }}</span></template></el-table-column>
          <el-table-column label="操作" min-width="200" fixed="right"><template slot-scope="scope"><el-button type="text" size="small" @click="showDetail(scope.row)">详情</el-button><el-button v-if="config.fields && !config.noEdit" v-hasPermi="['member:workspace:manage']" type="text" size="small" @click="edit(scope.row)">编辑</el-button><el-dropdown v-if="visibleActions(scope.row).length" v-hasPermi="['member:workspace:manage']" @command="key => runAction(key, scope.row)"><el-button type="text" size="small">更多操作 <i class="el-icon-arrow-down" aria-hidden="true"></i></el-button><el-dropdown-menu slot="dropdown"><el-dropdown-item v-for="key in visibleActions(scope.row)" :key="key" :command="key">{{ actionDefinitions[key].label }}</el-dropdown-item></el-dropdown-menu></el-dropdown></template></el-table-column>
        </el-table>
        <pagination v-if="total > 0" :total="total" :page.sync="query.pageNo" :limit.sync="query.pageSize" @pagination="load" />
      </template>
    </template>
    <el-dialog :title="dialogTitle" :visible.sync="dialogOpen" width="min(640px, 94vw)" append-to-body :close-on-click-modal="!saving" @closed="clearDialog">
      <el-alert v-if="dialogHelp" :title="dialogHelp" type="info" :closable="false" show-icon class="workspace-alert" />
      <el-alert v-if="dialogError" :title="dialogError" type="error" :closable="false" show-icon class="workspace-alert" />
      <el-form ref="editor" :model="form" label-position="top" :rules="formRules" @submit.native.prevent="save">
        <el-form-item v-for="field in dialogFields" :key="field.key" :label="field.label" :prop="field.key">
          <el-input v-if="['text','phone','textarea'].includes(field.type)" v-model="form[field.key]" :type="field.type === 'textarea' ? 'textarea' : 'text'" :rows="3" :maxlength="field.type === 'phone' ? 11 : field.type === 'textarea' ? 4000 : 200" :disabled="saving" />
          <el-input-number v-else-if="['money','number'].includes(field.type)" v-model="form[field.key]" :min="0" :max="99999999" :precision="field.type === 'money' ? 2 : 0" :disabled="saving" controls-position="right" />
          <el-date-picker v-else-if="field.type === 'datetime'" v-model="form[field.key]" type="datetime" value-format="timestamp" placeholder="选择日期和时间" :disabled="saving" style="width:100%" />
          <el-switch v-else-if="field.type === 'boolean'" v-model="form[field.key]" :disabled="saving" />
          <el-select v-else-if="field.type === 'select'" v-model="form[field.key]" :disabled="saving"><el-option v-for="(label, value) in field.options" :key="value" :value="numeric(value)" :label="label" /></el-select>
          <el-select v-else-if="['space','spaces','coupon','category','week'].includes(field.type)" v-model="form[field.key]" :multiple="['spaces','week'].includes(field.type)" filterable clearable :disabled="saving" style="width:100%"><el-option v-for="item in optionsFor(field.type)" :key="item.value" :label="item.label" :value="item.value" /></el-select>
          <div v-else-if="field.type === 'goods'" class="workspace-goods"><div v-for="(good, index) in form[field.key]" :key="index"><el-select v-model="good.goodsId" placeholder="寄存商品" filterable><el-option v-for="item in lookups.inventoryGoods" :key="item.id" :value="item.id" :label="item.goodsName + '（' + item.units + '）'" /></el-select><el-input-number v-model="good.num" :min="1" :max="99999" /><el-button icon="el-icon-delete" aria-label="移除此商品" @click="form[field.key].splice(index, 1)" /></div><el-button size="small" icon="el-icon-plus" @click="form[field.key].push({ goodsId: null, num: 1 })">添加商品</el-button></div>
        </el-form-item>
      </el-form>
      <template slot="footer"><el-button :disabled="saving" @click="dialogOpen = false">取消</el-button><el-button :type="activeAction && activeAction.danger ? 'danger' : 'primary'" :loading="saving" @click="save">确认{{ activeAction ? '操作' : '保存' }}</el-button></template>
    </el-dialog>
    <el-drawer title="记录详情" :visible.sync="detailOpen" size="min(620px, 95vw)" append-to-body><dl class="workspace-detail"><template v-for="column in detailColumns"><dt :key="column.key + '-label'">{{ column.label }}</dt><dd :key="column.key">{{ display(detail || {}, column.key) }}</dd></template></dl><div v-if="detail && (detail.qrCode || detail.renewCode)" class="workspace-codes"><div v-for="(url, label) in { 预约码: detail.qrCode, 续时码: detail.renewCode }" v-show="url" :key="label"><p>{{ label }}</p><el-image :src="url" :preview-src-list="[url]" fit="contain" /></div></div></el-drawer>
    <el-dialog title="平台授权" :visible.sync="authOpen" width="min(500px, 94vw)"><p>请前往平台完成授权，完成后返回刷新此页面。</p><a :href="authUrl" target="_blank" rel="noopener noreferrer" class="workspace-auth-link">打开平台授权页面 <i class="el-icon-top-right" aria-hidden="true"></i></a></el-dialog>
  </div>
</template>
<script>
import { pages, actions, spaceStates } from './config'
import { workspacePage, workspaceSave, workspaceAction, workspaceOverview } from '@/api/member/workspace'
import { getStoreInfoPage } from '@/api/member/storeInfo'
import * as echarts from 'echarts'
const IDS = { spaces: 'roomId', orders: 'orderId', rules: 'discountId', packages: 'pkgId', coupons: 'couponId', vip: 'vipId', cleaning: 'clearId', sounds: 'soundId', settings: 'storeId', productOrders: 'orderId' }
export default {
  name: 'SharedSpaceWorkspace',
  data() { return { spaceStates, actionDefinitions: actions, stores: [], storeId: null, initializing: true, loading: false, error: '', rows: [], total: 0, query: { pageNo: 1, pageSize: 20, keyword: '' }, requestVersion: 0, overview: {}, metrics: [{ key: 'todayNet', label: '今日订单净收（元）', money: true }, { key: 'todayOrders', label: '今日新增预约' }, { key: 'activeOrders', label: '进行中预约' }, { key: 'availableSpaces', label: '空闲空间' }], chart: null, dialogOpen: false, dialogTitle: '', dialogHelp: '', dialogError: '', dialogFields: [], form: {}, targetRow: null, activeAction: null, saving: false, lookups: { spaces: [], coupons: [], categories: [], inventoryGoods: [] }, detailOpen: false, detail: null, authOpen: false, authUrl: '' } },
  computed: {
    resource() { return this.$route.path === '/index' ? 'overview' : this.$route.path.split('/').filter(Boolean).pop().replace(/^workspace-/, '') },
    config() { return pages[this.resource] || pages.overview }, idKey() { return IDS[this.resource] || 'id' },
    formRules() { const rules = {}; this.dialogFields.forEach(f => { rules[f.key] = [{ required: !!f.required, message: '请填写' + f.label, trigger: 'change' }]; if (f.type === 'phone') rules[f.key].push({ pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }) }); return rules },
    detailColumns() { const list = this.config.columns.slice(); const known = new Set(list.map(c => c.key)); Object.keys(this.detail || {}).forEach(key => { if (!known.has(key)) list.push({ key, label: ({ createTime: '创建时间', qrCode: '预约码地址', renewCode: '续时码地址', wifiInfo: 'Wi-Fi 名称', wifiPwd: 'Wi-Fi 密码', price: '金额', orderNo: '订单号', roomId: '空间编号', productInfo: '商品明细', remark: '说明', imgs: '交付照片' })[key] || key }) }); return list }
  },
  watch: { '$route.path'() { if (this.chart) { this.chart.dispose(); this.chart = null } this.query.pageNo = 1; this.query.keyword = ''; this.dialogOpen = false; this.load() } },
  async created() {
    try {
      let pageNo = 1, all = [], total = 0
      do { const r = await getStoreInfoPage({ pageNo, pageSize: 100 }); all = all.concat(r.data.list); total = r.data.total; pageNo++ } while (all.length < total)
      this.stores = all; const remembered = Number(sessionStorage.getItem('workspace-store')); this.storeId = all.some(s => s.storeId === remembered) ? remembered : all.length ? all[0].storeId : null
      await this.load()
    } catch (error) { this.error = error.message || '门店加载失败，请稍后重试。' } finally { this.initializing = false }
    window.addEventListener('resize', this.resizeChart)
  },
  beforeDestroy() { window.removeEventListener('resize', this.resizeChart); if (this.chart) this.chart.dispose() },
  methods: {
    numeric(v) { return /^\d+$/.test(v) ? Number(v) : v },
    async changeStore() { sessionStorage.setItem('workspace-store', this.storeId); this.query.pageNo = 1; this.dialogOpen = false; this.detailOpen = false; await this.load() },
    async load() {
      if (!this.storeId) return
      const version = ++this.requestVersion; const resource = this.resource; this.error = ''; this.loading = true
      try {
        if (resource === 'overview') { const r = await workspaceOverview(this.storeId); if (version !== this.requestVersion) return; this.overview = r.data; this.$nextTick(this.renderChart) }
        else { const r = await workspacePage(resource, { ...this.query, storeId: this.storeId }); if (version !== this.requestVersion) return; this.rows = r.data.list; this.total = r.data.total }
      } catch (error) { if (version === this.requestVersion) { this.rows = []; this.error = error.message || '数据加载失败，请刷新重试。' } } finally { if (version === this.requestVersion) this.loading = false }
    },
    search() { this.query.pageNo = 1; this.load() }, reset() { this.query.keyword = ''; this.search() },
    display(row, key) {
      const value = row[key]; if (value === null || value === undefined || value === '') return '—'
      if (key === 'status' && this.config.states) return this.config.states[value] || String(value)
      if (this.config.enums && this.config.enums[key]) return this.config.enums[key][value] || String(value)
      if ((this.config.cents || []).includes(key)) return (Number(value) / 100).toFixed(2)
      if (typeof value === 'boolean' || ['enable', 'balanceBuy', 'isShow', 'payStatus'].includes(key)) return value === true || value === 1 ? '是' : '否'
      if (/Time$/.test(key)) { const date = new Date(value); return Number.isNaN(date.getTime()) ? String(value) : this.parseTime(date) }
      if (typeof value === 'object') return JSON.stringify(value)
      return String(value)
    },
    visibleActions(row) { return (this.config.rowActions || []).filter(key => (!actions[key].states || actions[key].states.includes(row.status)) && (key !== 'settleCleaner' || row.type === 14)) },
    showDetail(row) { this.detail = row; this.detailOpen = true },
    async loadLookups() { const result = await Promise.all(['spaces', 'coupons', 'categories', 'inventoryGoods'].map(async key => [key, (await workspacePage(key, { storeId: this.storeId, pageNo: 1, pageSize: 100 })).data.list])); result.forEach(([key, list]) => { this.lookups[key] = list }) },
    optionsFor(type) { if (type === 'week') return [1, 2, 3, 4, 5, 6, 7].map(n => ({ value: n, label: '星期' + ['一', '二', '三', '四', '五', '六', '日'][n - 1] })); const key = type === 'space' || type === 'spaces' ? 'spaces' : type === 'coupon' ? 'coupons' : 'categories'; return this.lookups[key].map(row => ({ value: row.roomId || row.couponId || row.id, label: row.roomName || row.couponName || row.name })) },
    async edit(row) { this.targetRow = row || null; this.activeAction = null; this.dialogFields = this.config.fields; this.dialogTitle = (row ? '编辑' : '新增') + this.config.title; this.dialogHelp = ''; this.openEditor(row || {}); await this.loadLookups() },
    openEditor(row) {
      const values = {}; this.dialogFields.forEach(f => { let value = row[f.key]; if (value == null) value = f.default !== undefined ? f.default : ['spaces', 'week', 'goods'].includes(f.type) ? [] : f.type === 'number' || f.type === 'money' ? 0 : f.type === 'boolean' ? false : ''; if (f.type === 'datetime' && value) value = new Date(value).getTime(); if (f.type === 'boolean') value = !!value; if (['spaces', 'week'].includes(f.type) && typeof value === 'string') { try { value = JSON.parse(value) } catch (_) { value = value.split(',').filter(Boolean).map(Number) } } values[f.key] = value }); this.form = values; this.dialogError = ''; this.dialogOpen = true; this.$nextTick(() => this.$refs.editor && this.$refs.editor.clearValidate())
    },
    async runAction(key, row = null) {
      const operation = actions[key]; this.targetRow = row; this.activeAction = operation
      if (operation.fields.length) { this.dialogFields = operation.fields; this.dialogTitle = operation.label; this.dialogHelp = operation.help || ''; this.openEditor(row || {}); await this.loadLookups(); return }
      try { await this.$confirm(operation.confirm || '确认执行' + operation.label + '？', operation.label, { type: operation.danger ? 'warning' : 'info' }); this.loading = true; await this.performAction(operation, row, {}); this.$message.success('操作成功'); await this.load() } catch (error) { if (error !== 'cancel' && error !== 'close') this.error = error.message || '操作失败，请刷新后重试。' } finally { this.loading = false }
    },
    actionPayload(operation, row, values) {
      const data = { ...values, storeId: this.storeId }; row = row || {}; const key = operation.key
      if (['renewOrder', 'changeOrder', 'changeOrderUser', 'cancelOrder', 'refundOrder', 'depositRefund'].includes(key)) { data.orderId = row.orderId; if (key === 'renewOrder') { data.orderNo = row.orderNo; data.payType = 0 } }
      if (key.endsWith('Space')) data.roomId = row.roomId
      if (['recharge', 'giftCoupon', 'editMemberVip', 'settleCleaner'].includes(key)) data.userId = row.userId
      if (key === 'deleteStaff') { data.id = row.id; data.userId = row.userId; data.type = row.type }
      if (key === 'cancelCleaning') data.clearId = row.clearId
      if (key.endsWith('Package')) data.pkgId = row.pkgId
      if (key === 'deleteCoupon') data.couponId = row.couponId
      if (key === 'deleteVip') data.vipId = row.vipId
      if (key === 'toggleRule') data.discountId = row.discountId
      if (['removeBlacklist', 'deleteInventoryGoods', 'deleteCategory', 'deleteProduct', 'toggleProduct', 'refundPayment'].includes(key)) data.id = row.id
      if (key === 'toggleProduct') data.isShow = row.isShow
      if (key === 'finishProductOrder') data.productOrderId = row.orderId
      if (key === 'refundPayment') { data.price = Math.round(Number(values.refundAmount) * 100); delete data.refundAmount }
      return data
    },
    async performAction(operation, row, values) { const r = await workspaceAction(operation.key, this.actionPayload(operation, row, values)); if (operation.key === 'groupAuth') { const url = new URL(r.data); if (!['https:', 'http:'].includes(url.protocol)) throw new Error('授权链接无效'); this.authUrl = url.href; this.authOpen = true } return r },
    async save() {
      if (this.saving) return
      const valid = await this.$refs.editor.validate().catch(() => false); if (!valid) return
      this.dialogError = ''; this.saving = true
      try {
        if (this.activeAction) await this.performAction(this.activeAction, this.targetRow, this.form)
        else { const data = { ...this.form, storeId: this.storeId }; if (this.targetRow) data[this.idKey] = this.targetRow[this.idKey]; await workspaceSave(this.resource, data) }
        this.dialogOpen = false; this.$message.success('已保存'); await this.load()
      } catch (error) { this.dialogError = error.message || '保存失败，请检查填写内容后重试。' } finally { this.saving = false }
    },
    clearDialog() { this.dialogError = ''; this.activeAction = null },
    exportPage() { const escape = value => '"' + String(value).replace(/^[=+@-]/, "'\u0024&").replace(/"/g, '""') + '"'; const columns = this.config.columns; const lines = [columns.map(c => escape(c.label)).join(','), ...this.rows.map(row => columns.map(c => escape(this.display(row, c.key))).join(','))]; const url = URL.createObjectURL(new Blob(['\ufeff' + lines.join('\r\n')], { type: 'text/csv;charset=utf-8;' })); const link = document.createElement('a'); link.href = url; link.download = this.config.title + '.csv'; link.click(); URL.revokeObjectURL(url) },
    renderChart() { if (!this.$refs.trend) return; if (!this.chart) this.chart = echarts.init(this.$refs.trend); const trend = this.overview.trend || []; this.chart.setOption({ tooltip: { trigger: 'axis' }, grid: { left: 55, right: 25, top: 30, bottom: 40 }, xAxis: { type: 'category', data: trend.map(x => x.date), axisTick: { show: false } }, yAxis: { type: 'value', name: '元' }, series: [{ name: '订单净收', type: 'bar', data: trend.map(x => x.amount), itemStyle: { color: '#2563eb' }, barMaxWidth: 36 }] }) },
    resizeChart() { if (this.chart) this.chart.resize() }
  }
}
</script>
<style lang="scss" scoped>
.workspace-page { padding: 28px; background: #f6f8fb; min-height: calc(100vh - 84px); color: #243247; }
.workspace-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 24px; margin-bottom: 26px; h1 { margin: 0 0 10px; font-size: 25px; font-weight: 650; } p { margin: 0; color: #64748b; font-size: 14px; line-height: 1.7; } }
.workspace-context { display: flex; align-items: center; gap: 10px; flex-shrink: 0; font-size: 14px; }
.workspace-alert { margin-bottom: 20px; }
.workspace-toolbar { background: white; padding: 20px 20px 4px; margin-bottom: 18px; border: 1px solid #e7ecf2; border-radius: 8px; display: flex; justify-content: space-between; flex-wrap: wrap; gap: 10px; }
.workspace-tools { padding-bottom: 16px; display: flex; align-items: flex-start; gap: 8px; flex-wrap: wrap; ::v-deep .el-button { margin-left: 0; } }
.workspace-spaces { display: grid; grid-template-columns: repeat(auto-fill,minmax(300px,1fr)); gap: 18px; }
.workspace-space { background: #fff; padding: 22px; border: 1px solid #e3e9f0; border-radius: 8px; h2 { font-size: 17px; line-height: 1.5; margin: 0; } > p { font-size: 26px; font-weight: 600; margin: 25px 0; span { font-size: 14px; font-weight: 400; color: #64748b; } } }
.workspace-space__heading { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; }
.workspace-space__actions { display: flex; flex-wrap: wrap; gap: 8px; ::v-deep .el-button { margin-left: 0; } }
.workspace-metrics { display: grid; grid-template-columns: repeat(4,1fr); gap: 20px; margin-bottom: 26px; > div { padding: 26px; background: white; border: 1px solid #e5ebf3; border-radius: 8px; } span { color: #64748b; font-size: 14px; } strong { display: block; margin-top: 15px; font-size: 30px; font-weight: 600; } }
.workspace-trend { background: white; padding: 25px; border: 1px solid #e5ebf3; border-radius: 8px; h2 { font-size: 18px; margin: 0 0 8px; } p { color: #64748b; font-size: 14px; } }
.workspace-chart { height: 350px; width: 100%; }
.workspace-detail { padding: 0 25px 30px; dt { font-size: 13px; color: #64748b; margin-top: 20px; } dd { margin: 8px 0 0; font-size: 15px; line-height: 1.7; word-break: break-word; white-space: pre-wrap; } }
.workspace-codes { display: flex; padding: 0 25px 25px; gap: 20px; .el-image { width: 160px; height: 160px; } }
.workspace-goods { > div { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; } }
.workspace-auth-link { display: inline-block; color: #165dff; margin: 20px 0; }
::v-deep .el-dropdown { margin-left: 12px; } ::v-deep .el-form-item__label { color: #334155; } ::v-deep .el-form-item .el-input-number { width: 100%; }
@media(max-width:1100px) { .workspace-header { flex-direction: column; } .workspace-metrics { grid-template-columns: repeat(2,1fr); } }
@media(max-width:600px) { .workspace-page { padding: 18px 12px; } .workspace-context { flex-wrap: wrap; } .workspace-metrics { grid-template-columns: 1fr 1fr; gap: 10px; > div { padding: 15px; } strong { font-size: 23px; } } .workspace-goods > div { flex-wrap: wrap; } }
</style>
