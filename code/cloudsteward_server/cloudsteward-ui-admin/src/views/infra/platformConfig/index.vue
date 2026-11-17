<template>
  <div style="padding: 20px 30px;">
    <el-card>
      <div slot="header">
        <span style="font-size: 16px; font-weight: bold;">平台参数配置</span>
        <span style="color: #909399; font-size: 13px; margin-left: 10px;">第三方 ISP/IoT + 团购核销 统一配置</span>
      </div>

      <el-form ref="configForm" :model="configForm" :rules="configRules" label-width="120px" style="max-width: 600px;">
        <el-form-item label="接口域名" prop="hostname">
          <el-input v-model="configForm.hostname" placeholder="api.xxxxxx.com" />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            格式为 api.xxxxxx.com，前面没有 https 后面也没有 /，用于微信支付回调等
          </div>
        </el-form-item>

        <el-form-item label="应用ID" prop="clientId">
          <el-input v-model="configForm.clientId" placeholder="集成服务商应用ID" />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            第三方 ISP 平台的应用ID，用于硬件设备控制和团购核销
          </div>
        </el-form-item>

        <el-form-item label="密钥" prop="secret">
          <el-input v-model="configForm.secret" type="password" show-password placeholder="集成服务商密钥" />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            第三方 ISP 平台的密钥，与应用ID配套使用
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="saving">保存配置</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getAppConfig, saveAppConfig } from '@/api/index'

export default {
  name: 'PlatformConfig',
  data() {
    return {
      saving: false,
      configForm: {
        hostname: '',
        clientId: '',
        secret: ''
      },
      configRules: {
        hostname: [
          { required: true, message: '接口域名不能为空', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\.?$/, message: '请输入正确的域名格式', trigger: 'blur' }
        ],
        clientId: [
          { required: true, message: '应用ID不能为空', trigger: 'blur' }
        ],
        secret: [
          { required: true, message: '密钥不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadConfig()
  },
  methods: {
    loadConfig() {
      getAppConfig().then(response => {
        this.configForm = {
          hostname: response.data.hostname || '',
          clientId: response.data.clientId || '',
          secret: response.data.secret || ''
        }
      }).catch(() => {
        this.$message.error('加载配置失败')
      })
    },
    submitForm() {
      this.$refs['configForm'].validate(valid => {
        if (!valid) return
        this.$confirm('保存后配置立即生效，请确认填写无误', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.saving = true
          return saveAppConfig(this.configForm)
        }).then(() => {
          this.$message.success('保存成功')
          this.saving = false
        }).catch(() => {
          this.saving = false
        })
      })
    },
    resetForm() {
      this.loadConfig()
    }
  }
}
</script>

<style scoped>
.el-card {
  margin-bottom: 20px;
}
</style>
