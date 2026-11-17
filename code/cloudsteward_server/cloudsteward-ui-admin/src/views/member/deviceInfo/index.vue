<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="设备sn" prop="deviceSn">
        <el-input v-model="queryParams.deviceSn" placeholder="请输入设备sn" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="设备类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择设备类型" clearable size="small">
          <el-option v-for="dict in deviceTypes" :key="dict.value" :label="dict.label"
            :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="门店" prop="storeId">
        <el-select v-model="queryParams.storeId" placeholder="请选择门店" clearable size="small" @change="loadRoomList">
          <el-option v-for="item in storeList" :key="item.value" :label="item.key" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="房间" prop="roomId">
        <el-select v-model="queryParams.roomId" placeholder="请选择房间" clearable size="small">
          <el-option v-for="item in roomList" :key="item.value" :label="item.key" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option label="在线" value="1" />
          <el-option label="离线" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="共用设备" prop="share">
        <el-select v-model="queryParams.share" placeholder="请选择" clearable size="small">
          <el-option label="是" value="1" />
          <el-option label="否" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss"
          type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"
          :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['member:device-info:create']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" :loading="exportLoading"
          v-hasPermi="['member:device-info:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="设备id" align="center" prop="deviceId" />
      <el-table-column label="设备sn" align="center" prop="deviceSn" />
      <el-table-column label="设备类型" align="center" prop="type">
        <template v-slot="scope">
          <span>{{ deviceTypeLabel(scope.row.type) }}</span>
        </template>
      </el-table-column>
      <input id="storeId" type="hidden" />
      <input id="roomId" type="hidden" />
      <el-table-column label="门店名称" align="center" prop="storeName" />
      <el-table-column label="房间名称" align="center" prop="roomName" />
      <el-table-column label="状态" align="center" :formatter="statusFomat" />
      <el-table-column label="共用设备" align="center" prop="share">
        <template v-slot="scope">
          <span>{{ scope.row.share === 1 ? '是' : '否' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="控制" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" v-if="[1, 2].includes(Number(scope.row.type))"  @click="handleOpen(scope.row)"
          v-hasPermi="['member:device-info:update']">打开</el-button>
          <el-button size="mini" type="text" v-if="Number(scope.row.type) === 3"  @click="handleSound(scope.row)"
          v-hasPermi="['member:device-info:update']">测试播报</el-button>
          <el-button size="mini" type="text" v-if="[1, 2].includes(Number(scope.row.type))"  @click="handleClose(scope.row)"
            v-hasPermi="['member:device-info:update']">关闭</el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-wifi" @click="handleConfig(scope.row)"
            v-if="Number(scope.row.type) === 18"
            :loading="pairingDeviceId === scope.row.deviceId"
            :disabled="syncingDeviceId !== null || pairingDeviceId !== null"
            v-hasPermi="['member:device-info:update']">配网</el-button>
          <el-button size="mini" type="text" icon="el-icon-refresh" @click="handleSync(scope.row)"
            v-if="Number(scope.row.type) === 18"
            :loading="syncingDeviceId === scope.row.deviceId"
            :disabled="syncingDeviceId !== null || pairingDeviceId !== null"
            v-hasPermi="['member:device-info:update']">同步设备</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleBindStore(scope.row)" v-if="Number(scope.row.type) !== 18"
            v-hasPermi="['member:device-info:update']">绑定房间</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['member:device-info:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <el-dialog title="网关子设备配网" :visible.sync="pairingVisible" width="min(520px, 94vw)"
      :close-on-click-modal="false" @close="stopPairing">
      <el-alert title="设备平台已接受配网指令" type="success" :closable="false" show-icon />
      <p>请将需要添加的子设备置于配网状态。页面将自动同步设备两分钟；新设备归属当前网关的门店，房间需手动绑定。</p>
      <p role="status" aria-live="polite">{{ pairingMessage }}</p>
      <p>关闭此窗口会停止页面自动同步。之后可点击“同步设备”获取最新设备。</p>
      <span slot="footer"><el-button @click="pairingVisible = false">关闭</el-button></span>
    </el-dialog>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="设备sn" prop="deviceSn">
          <el-input v-model="form.deviceSn" placeholder="请输入设备sn" />
        </el-form-item>
        <el-form-item label="设备类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择设备类型" @change="handleDeviceTypeChange">
            <el-option v-for="dict in deviceTypes" :key="dict.value"
              :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="Number(form.type) !== 18" label="多房间共用设备" prop="share" label-width="150px">
          <el-radio-group v-model="form.share">
            <el-radio :key="0" :label="0">否</el-radio>
            <el-radio :key="1" :label="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        注:表示多个房间或台桌共用，常用于中央空调、台球厅喇叭等
        
        <el-form-item label="门店" prop="storeId">
          <el-select v-model="form.storeId" placeholder="请选择门店" clearable size="small" @change="loadRoomList"
            required="true" :rules="rules">
            <el-option v-for="item in storeList" :key="item.value" :label="item.key" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="Number(form.type) !== 18" label="房间" prop="roomId">
          <el-select v-model="form.roomId" placeholder="请选择房间" clearable size="small">
            <el-option v-for="item in roomList" :key="item.value" :label="item.key" :value="item.value" />
          </el-select>
        </el-form-item>
        注：网关、人脸识别机、前台云喇叭、门店大门门禁等设备不需要绑定房间，只绑定门店即可
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 对话框(绑定门店) -->
    <el-dialog :title="title" :visible.sync="bindStore" width="500px" v-dialogDrag append-to-body>
      <el-form ref="bindForm" :model="bindForm" :rules="bindrules" label-width="80px">
        <!-- <el-form-item label="设备sn" prop="deviceSn">
          <el-label v-model="form.deviceSn" readonly />
        </el-form-item> -->
        <el-form-item label="门店" prop="storeId">
          <el-select v-model="bindForm.storeId" placeholder="所属门店" disabled size="small"
            required="true" :rules="bindrules">
            <el-option v-for="item in storeList" :key="item.value" :label="item.key" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间" prop="roomId">
          <el-select v-model="bindForm.roomId" placeholder="请选择房间" clearable size="small">
            <el-option v-for="item in roomList" :key="item.value" :label="item.key" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitBindForm">确 定</el-button>
        <el-button @click="cancelBind">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 对话框(设置自动关锁) -->
    <el-dialog :title="title" :visible.sync="LockShow" width="500px" v-dialogDrag append-to-body>
      <el-form ref="LockForm" :model="LockForm" :rules="Lockrules" label-width="80px">
        <input type="hidden"  prop="deviceId" v-model="LockForm.deviceId"/>
        <el-form-item label="设备sn" prop="deviceSn">
          <el-input v-model="LockForm.deviceSn"  readonly/>
        </el-form-item>
        <el-form-item label="关锁时间" prop="ssid">
          <el-input-number v-model="LockForm.secend"  :step="5" :min="0" required="true"/>（秒）
          <p>0表示开锁后一直打开，5表示开锁后5秒自动关锁</p>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitLockForm">确 定</el-button>
        <el-button @click="cancelLock">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { createDeviceInfo, updateDeviceInfo, deleteDeviceInfo, getDeviceInfo, getDeviceInfoPage, exportDeviceInfoExcel, getStoreList, getRoomList,bind,pairGateway,syncSubDevices,setAutoLock,control } from "@/api/member/deviceInfo";
import { DICT_TYPE, getDictDatas} from "@/utils/dict";
export default {
  name: "DeviceInfo",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 设备管理列表
      list: [],
      storeList: [],
      roomList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      bindStore: false,
      bindRoom: false,
      syncingDeviceId: null,
      pairingDeviceId: null,
      pairingVisible: false,
      pairingMessage: '',
      pairingTimer: null,
      pairingSession: 0,
      deviceTypes: [
        { value: '2', label: '电控' },
        { value: '3', label: '音箱' },
        { value: '1', label: '门禁开关' },
        { value: '18', label: '网关' },
      ],
      LockShow: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        deviceSn: null,
        type: null,
        share: null,
        storeId: null,
        roomId: null,
        status: null,
        share: null,
        createTime: [],
      },
      // 表单参数
      form: {
        share: 0,
      },
      bindForm: {
        deviceSn:null,
        storeId:null,
        roomId:null
      },
      LockForm: {
        deviceSn:null,
        secend:0,
      },
      // 表单校验
      rules: {
        deviceSn: [{ required: true, message: "设备sn不能为空", trigger: "blur" }],
        type: [{ required: true, message: "设备类型不能为空", trigger: "change" }],
        storeId: [{ required: true, message: "门店不能为空", trigger: "blur" }],
      },
      bindrules: {
        storeId: [{ required: true, message: "门店不能为空", trigger: "blur" }],
      },
      Lockrules: {
        secend: [{ required: true, message: "自动关锁时间不能为空", trigger: "blur" }],
      },
      optionsStas: [
        {
          name: "离线",
          id: 0,
        },
        {
          name: "在线",
          id: 1,
        },
      ],

    };
  },
  created() {
    this.getList();
    // 执行查询
    getStoreList().then(response => {
      this.storeList = response.data;
    });
  },
  beforeDestroy() {
    this.stopPairing();
  },
  deactivated() {
    this.pairingVisible = false;
    this.stopPairing();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      return getDeviceInfoPage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
      }).finally(() => {
        this.loading = false;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    cancelBind(){
      this.bindStore = false;
      this.bindForm={
        deviceId: undefined,
        storeId: undefined,
        roomId: undefined,
      }
    },
    cancelLock(){
      this.LockShow = false;
      this.LockForm={
        deviceSn:null,
        deviceId:null,
        secend:0
      }
    },
    /** 表单重置 */
    reset() {
      this.form = {
        deviceId: undefined,
        deviceSn: undefined,
        type: undefined,
        share: 0,
      };
      this.resetForm("form");
    },
    resetLock() {
      this.LockForm={
        deviceSn:null,
        deviceId:null,
        secend:null
      },
      this.resetForm("LockForm");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    handleOpen(row){
      const data={
        deviceId: row.deviceId,
        cmd: 'on'
      };
      control(data).then(response => {
        this.$modal.msgSuccess("指令发送成功");
      });
    },
    handleSound(row){
      const data={
        deviceId: row.deviceId,
        cmd: '1'
      };
      control(data).then(response => {
        this.$modal.msgSuccess("指令发送成功");
      });
    },
    handleClose(row){
      const data={
        deviceId: row.deviceId,
        cmd: 'off'
      };
      control(data).then(response => {
        this.$modal.msgSuccess("指令发送成功");
      });
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加设备";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const deviceId = row.deviceId;
      getDeviceInfo(deviceId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改设备";
      });
    },
    deviceTypeLabel(type) {
      const deviceType = this.deviceTypes.find(item => Number(item.value) === Number(type));
      return deviceType ? deviceType.label : '暂不支持的设备类型';
    },
    handleDeviceTypeChange(type) {
      if (Number(type) === 18) {
        this.form.roomId = null;
        this.form.share = 0;
      }
    },
    syncMessage(result) {
      let message = `发现 ${result.discovered} 个设备，新增 ${result.added} 个，已存在 ${result.existing} 个，跳过 ${result.skipped} 个`;
      if (result.conflicts) message += `，${result.conflicts} 个设备已绑定其他门店，未变更`;
      if (result.unsupportedUiids && result.unsupportedUiids.length) {
        message += `；暂未配置的 UIID：${result.unsupportedUiids.join('、')}`;
      }
      return message;
    },
    stopPairing() {
      this.pairingSession++;
      clearTimeout(this.pairingTimer);
      this.pairingTimer = null;
      this.pairingDeviceId = null;
    },
    /** 先开启子设备入网，再定期同步，不能把查询成功当作配网成功。 */
    async handleConfig(row) {
      if (this.syncingDeviceId !== null || this.pairingDeviceId !== null) return;
      this.pairingDeviceId = row.deviceId;
      const session = ++this.pairingSession;
      try {
        await pairGateway(row.deviceId);
        if (session !== this.pairingSession) return;
        this.pairingMessage = '正在获取子设备…';
        this.pairingVisible = true;
        await this.pollPairing(row, session, Date.now() + 120000);
      } catch (error) {
        if (session === this.pairingSession) this.stopPairing();
      }
    },
    async pollPairing(row, session, deadline) {
      if (session !== this.pairingSession) return;
      try {
        const response = await syncSubDevices(row.deviceId);
        if (session !== this.pairingSession) return;
        this.pairingMessage = this.syncMessage(response.data);
        await this.getList();
      } catch (error) {
        if (session === this.pairingSession) {
          this.pairingMessage = '自动同步失败，请关闭窗口后点击“同步设备”重试。配网指令不会重复发送。';
          this.pairingDeviceId = null;
        }
        return;
      }
      if (session !== this.pairingSession) return;
      if (Date.now() >= deadline) {
        this.pairingMessage += '。自动同步已结束，可关闭窗口后点击“同步设备”继续查询。';
        this.pairingDeviceId = null;
        return;
      }
      this.pairingTimer = setTimeout(() => this.pollPairing(row, session, deadline), 5000);
    },
    async handleSync(row) {
      if (this.syncingDeviceId !== null || this.pairingDeviceId !== null) return;
      this.syncingDeviceId = row.deviceId;
      try {
        const response = await syncSubDevices(row.deviceId);
        const result = response.data;
        this.$message({
          message: this.syncMessage(result),
          type: result.conflicts || (result.unsupportedUiids || []).length ? 'warning' : 'success',
          duration: 8000,
          showClose: true,
        });
        await this.getList();
      } catch (error) {
        // The request layer displays the server's error; always release the button.
      } finally {
        this.syncingDeviceId = null;
      }
    },
    /** 配置自动关锁 */
    handleAutoLock(row) {
      this.resetLock();
        this.LockForm = {
          deviceId: row.deviceId,
          deviceSn: row.deviceSn,
          secend: 0,
        };
        this.LockShow = true;
        this.title = "修改自动关锁设置";
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        if (this.form.deviceId != null) {
          updateDeviceInfo(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createDeviceInfo(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    submitBindForm() {
      this.$refs["bindForm"].validate(valid => {
        if (!valid) {
          return;
        }
        bind(this.bindForm).then(response => {
          this.$modal.msgSuccess("操作成功");
          this.bindStore = false;
          this.getList();
          this.bindForm={
            deviceId: undefined,
            storeId: undefined,
            roomId: undefined,
          }
        });
      });
    },
    submitLockForm() {
      this.$modal.confirm('需要配置了网关，才能修改此设置。是否确认修改?').then(function () {
      }).then(() => {
        setAutoLock(this.LockForm).then(response => {
          this.$modal.msgSuccess("操作成功");
          this.LockShow = false;
          this.LockForm={
            deviceId:null,
            deviceSn:null,
            secend: 0
          }
        });
      }).catch(() => { });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const deviceId = row.deviceId;
      this.$modal.confirm('是否确认删除设备管理编号为"' + deviceId + '"的数据项?').then(function () {
        return deleteDeviceInfo(deviceId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => { });
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = { ...this.queryParams };
      params.pageNo = undefined;
      params.pageSize = undefined;
      this.$modal.confirm('是否确认导出所有设备管理数据项?').then(() => {
        this.exportLoading = true;
        return exportDeviceInfoExcel(params);
      }).then(response => {
        this.$download.excel(response, '设备管理.xls');
        this.exportLoading = false;
      }).catch(() => { });
    },
    loadRoomList(storeId) {
      if (storeId) {
        getRoomList(storeId).then(response => {
          this.roomList = response.data;
        });
      }
    },
    statusFomat(row, column) {
      if (row.status == 0) {
        return "离线";
      } else if (row.status == 1) {
        return "在线";
      }
    },
    /** 绑定门店 */
    handleBindStore(row) {
      this.bindForm.deviceId = row.deviceId;
      this.bindForm.storeId = row.storeId;
      this.bindForm.roomId = row.roomId;
      this.loadRoomList(row.storeId)
      this.bindStore = true;
      this.title = "修改设备绑定";
    }
  }
};
</script>
