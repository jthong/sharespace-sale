<template>
  <WorkspaceHome v-if="!$store.getters.roles.includes('super_admin')" />
  <div v-else style="padding: 0 30px;">
    <h1>云掌柜共享空间管理平台 © 2026 版权所有
       <el-button v-hasRole="['super_admin']" size="medium" @click="updateDatabase" style="width:20%;color:#ff4d4f;">数据库更新同步最新版本</el-button></h1>
    <div v-hasRole="['super_admin']" class="info">
      <div class="title">当前是超级管理员账号，只使用【租户管理】、【文件配置】菜单</div>
      <div class="cf">
        <el-form ref="configForm" :model="configForm" :rules="configRules" label-width="150px" class="config-form">
          <el-form-item label="接口域名" prop="hostname" style="width:50%;">
            <el-input v-model="configForm.hostname" type="text" auto-complete="off" placeholder="接口域名,格式：(api.xxxx.com)">
            </el-input>
            <div>格式为api.xxxxxx.com，前面没有https后面也没有/，注意检查格式</div>
          </el-form-item>
          <el-form-item label="clientId" prop="clientId" style="width:50%;">
            <el-input v-model="configForm.clientId" type="text" auto-complete="off" placeholder="微信公众服务号【后羿物联】申请的clientId">
            </el-input>
            <div>如果不使用我们的硬件设备或者统一团购核销服务，则无需填写clientId和secret</div>
          </el-form-item>
          <el-form-item label="secret" prop="secret" style="width:50%;">
            <el-input v-model="configForm.secret" type="text" auto-complete="off" placeholder="微信公众服务号【后羿物联】申请的secret">
            </el-input>
            <div>如果不使用我们的硬件设备或者统一团购核销服务，则无需填写clientId和secret</div>
          </el-form-item>
         <el-form-item label="统一团购核销服务" prop="groupPay">
          <el-radio-group v-model="groupPay">
            <el-radio key="1" label="1">是</el-radio>
            <el-radio key="0" label="0">否</el-radio>
          </el-radio-group>
          <div>自己注册美团、抖音团购开发者的，选否。 使用我们核销服务的选是，并在微信服务号【后羿物联】开通和付费</div>
         </el-form-item>
        
          <el-form-item label="抖音appKey" prop="dyappKey" style="width:50%;"  v-show="groupPay=='0'">
            <el-input v-model="configForm.dyappKey" type="text" auto-complete="off" placeholder="抖音团购开发者appKey">
            </el-input>
            <a target="_blank" style="color: blue;" href="https://developer.open-douyin.com/docs/resource/zh-CN/local-life/connect/developer/basic-config/dev-info">跳转注册抖音团购开发者地址</a>
          </el-form-item>
          <el-form-item label="抖音secret" prop="dysecret" style="width:50%;"  v-show="groupPay=='0'">
            <el-input v-model="configForm.dysecret" type="text" auto-complete="off" placeholder="抖音团购开发者secret">
            </el-input>
          </el-form-item>
          <el-form-item label="美团appKey" prop="mtappKey" style="width:50%;"  v-show="groupPay=='0'">
            <el-input v-model="configForm.mtappKey" type="text" auto-complete="off" placeholder="美团团购开发者appKey">
            </el-input>
            <a target="_blank" style="color: blue;" href="https://developer.meituan.com/settleIn">跳转注册美团团购开发者地址</a>
          </el-form-item>
          <el-form-item label="美团secret" prop="mtsecret" style="width:50%;"  v-show="groupPay=='0'">
            <el-input v-model="configForm.mtsecret" type="text" auto-complete="off" placeholder="美团团购开发者secret">
            </el-input>
          </el-form-item>
          <el-form-item style="width:100%;">
            <el-button size="medium" type="primary" @click="submitForm" style="width:20%;" >保存当前修改</el-button>
          </el-form-item> 
        </el-form>

      </div>
      
    </div>
  </div>
</template>

<style lang="scss" scoped>
.h1 {
  text-align: center;
}

.info .title{
  font-size: 30px;
  color: red;
}

.cf {
  margin-top: 20px;
}


</style>
<script>
import WorkspaceHome from '@/views/member/workspace/index'
import { getAppConfig, saveAppConfig, } from "@/api/index";

export default {
  name: "DeviceInfo",
  components: { WorkspaceHome },
  data() {
    return {
      groupPay: "",
      configForm: {
        hostname: "",
        clientId: "",
        secret: "",
        dyappKey: "",
        dysecret: "",
        mtappKey: "",
        mtsecret: "",
      },
      configRules: {
        hostname: [
          { required: true, trigger: "blur", message: "接口域名地址不能为空" }
        ],
        
      },
    }
  },
  created() {
    if(this.$auth.hasPermi("infra:config:update")){
      getAppConfig().then(response => {
        this.groupPay = response.data.groupPay;
        this.configForm = {
          hostname: response.data.hostname,
          clientId: response.data.clientId,
          secret: response.data.secret,
          dyappKey: response.data.dyappKey,
          dysecret: response.data.dysecret,
          mtappKey: response.data.mtappKey,
          mtsecret: response.data.mtsecret,
        }
      })
    }
    
  },
  methods: {
    submitForm() {
      this.$refs["configForm"].validate(valid => {
        if (!valid) {
          return;
        }
        const data = this.configForm;
        data.groupPay = this.groupPay;
        this.$modal.confirm('请认真核对所填写数据是否无误？填写错误将影响系统正常使用').then(function() {
          return saveAppConfig(data);
        }).then(() => {
          this.$modal.msgSuccess("保存成功");
        }).catch(() => {});
      })
    },
    updateDatabase(){
      this.$modal.confirm('预留功能，暂未发布').then(function() {
        return;
      }).then(() => {
        // this.$modal.msgSuccess("操作成功，请等待5分钟后测试");
      }).catch(() => {});
    },
  }
};
</script>



