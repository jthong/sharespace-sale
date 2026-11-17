<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="房间名称" prop="roomName">
        <el-input v-model="queryParams.roomName" placeholder="请输入房间名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="门店id" prop="storeId">
        <el-input v-model="queryParams.storeId" placeholder="请输入门店id" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="房间类型" prop="roomClass">
        <el-select v-model="queryParams.roomClass" placeholder="请选择房间类型" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.MEMBER_ROOM_CLASS)"
                     :key="dict.value" :label="dict.label" :value="parseInt(dict.value)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="包间类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择包间类型" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.MEMBER_ROOM_TYPE)"
                     :key="dict.value" :label="dict.label" :value="parseInt(dict.value)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.MEMBER_ROOM_STATUS)"
                     :key="dict.value" :label="dict.label" :value="parseInt(dict.value)"/>
        </el-select>
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
                   v-hasPermi="['member:room-info:create']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" :loading="exportLoading"
                   v-hasPermi="['member:room-info:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="房间id" align="center" prop="roomId" width="70" />
      <el-table-column label="房间名称" align="center" prop="roomName" />
      <el-table-column label="房间类型" align="center" prop="roomClass" width="90">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.MEMBER_ROOM_CLASS" :value="scope.row.roomClass" />
        </template>
      </el-table-column>
      <el-table-column label="包间类型" align="center" prop="type" width="90">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.MEMBER_ROOM_TYPE" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column label="时价(元/时)" align="center" prop="price" width="90" />
      <el-table-column label="工作日价" align="center" prop="workPrice" width="90" />
      <el-table-column label="押金" align="center" prop="deposit" width="80" />
      <el-table-column label="通宵价" align="center" prop="tongxiaoPrice" width="80" />
      <el-table-column label="标签" align="center" prop="label" width="160">
        <template v-slot="scope">
          <span v-for="(v, i) in (scope.row.label || '').split(',').filter(Boolean)" :key="i">
            <el-tag v-if="v" size="small" type="success">{{ v }}</el-tag>
            <span v-if="i < (scope.row.label || '').split(',').filter(Boolean).length - 1"> </span>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sortId" width="60" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.MEMBER_ROOM_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['member:room-info:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['member:room-info:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="680px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="房间名称" prop="roomName">
              <el-input v-model="form.roomName" placeholder="请输入房间名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="呼叫名称" prop="roomCallName">
              <el-input v-model="form.roomCallName" placeholder="请输入呼叫名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="门店id" prop="storeId">
              <el-input v-model="form.storeId" placeholder="请输入门店id(默认18)" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房间类型" prop="roomClass">
              <el-select v-model="form.roomClass" placeholder="棋牌/台球/自习室" clearable>
                <el-option v-for="dict in this.getDictDatas(DICT_TYPE.MEMBER_ROOM_CLASS)"
                           :key="dict.value" :label="dict.label" :value="parseInt(dict.value)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="包间类型" prop="type">
              <el-select v-model="form.type" placeholder="特价包/小包/中包等" clearable>
                <el-option v-for="dict in this.getDictDatas(DICT_TYPE.MEMBER_ROOM_TYPE)"
                           :key="dict.value" :label="dict.label" :value="parseInt(dict.value)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" clearable>
                <el-option v-for="dict in this.getDictDatas(DICT_TYPE.MEMBER_ROOM_STATUS)"
                           :key="dict.value" :label="dict.label" :value="parseInt(dict.value)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时价(元/小时)" prop="price">
              <el-input v-model="form.price" placeholder="每小时价格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作日价格" prop="workPrice">
              <el-input v-model="form.workPrice" placeholder="工作日每小时价格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="押金" prop="deposit">
              <el-input v-model="form.deposit" placeholder="押金金额" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="通宵价格" prop="tongxiaoPrice">
              <el-input v-model="form.tongxiaoPrice" placeholder="通宵价格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低消费" prop="minCharge">
              <el-input v-model="form.minCharge" placeholder="最低消费金额" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预付费价格" prop="prePrice">
              <el-input v-model="form.prePrice" placeholder="预约预付费价格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预付费计价分钟" prop="preUnit">
              <el-input v-model="form.preUnit" placeholder="如60" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最小下单时长(小时)" prop="minHour">
              <el-input v-model="form.minHour" placeholder="如1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大提前(小时)" prop="leadHour">
              <el-input v-model="form.leadHour" placeholder="如24" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="禁用开始时间" prop="banTimeStart">
              <el-input v-model="form.banTimeStart" placeholder="如00:00" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="禁用结束时间" prop="banTimeEnd">
              <el-input v-model="form.banTimeEnd" placeholder="如06:00" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="包场-通宵价" prop="txPrice">
              <el-input v-model="form.txPrice" placeholder="包场通宵价格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="包场-上午价" prop="morningPrice">
              <el-input v-model="form.morningPrice" placeholder="包场上午价格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="包场-下午价" prop="afternoonPrice">
              <el-input v-model="form.afternoonPrice" placeholder="包场下午价格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="包场-夜间价" prop="nightPrice">
              <el-input v-model="form.nightPrice" placeholder="包场夜间价格" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="房间标签(设施)" prop="label">
              <el-select v-model="labelList" multiple placeholder="多选设施标签" clearable>
                <el-option v-for="dict in this.getDictDatas(DICT_TYPE.MEMBER_ROOM_LABEL)"
                           :key="dict.value" :label="dict.label" :value="dict.label" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="房间图片" prop="imageUrls">
              <imageUpload v-model="form.imageUrls" :limit="6" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序位置" prop="sortId">
              <el-input v-model="form.sortId" placeholder="排序" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="喇叭声音" prop="yunlabaSound">
              <el-radio-group v-model="form.yunlabaSound">
                <el-radio :label="1">开</el-radio>
                <el-radio :label="0">关</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="跳过清洁" prop="jumpClear">
              <el-radio-group v-model="form.jumpClear">
                <el-radio :label="true">是</el-radio>
                <el-radio :label="false">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否可预定" prop="reserve">
              <el-radio-group v-model="form.reserve">
                <el-radio :label="true">可预定</el-radio>
                <el-radio :label="false">不可预定</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否小时下单" prop="xiaoshiSubmit">
              <el-radio-group v-model="form.xiaoshiSubmit">
                <el-radio :label="true">是</el-radio>
                <el-radio :label="false">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { createRoomInfo, updateRoomInfo, deleteRoomInfo, getRoomInfo, getRoomInfoPage, exportRoomInfoExcel } from "@/api/member/roomInfo";
import ImageUpload from '@/components/ImageUpload';

export default {
  name: "RoomInfo",
  components: {
    ImageUpload,
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
      // 房间管理列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 设施标签多选
      labelList: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        roomName: null,
        storeId: null,
        roomClass: null,
        type: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        roomName: [{ required: true, message: "房间名称不能为空", trigger: "blur" }],
        storeId: [{ required: true, message: "门店id不能为空", trigger: "blur" }],
        type: [{ required: true, message: "包间类型不能为空", trigger: "change" }],
        price: [{ required: true, message: "时价不能为空", trigger: "blur" }],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getRoomInfoPage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        roomId: undefined,
        roomName: undefined,
        roomCallName: undefined,
        roomClass: 0,
        type: undefined,
        storeId: 18,
        workPrice: undefined,
        price: undefined,
        deposit: 0,
        tongxiaoPrice: undefined,
        label: undefined,
        imageUrls: undefined,
        sortId: undefined,
        yunlabaSound: 1,
        banTimeStart: undefined,
        banTimeEnd: undefined,
        leadHour: undefined,
        minHour: 1,
        jumpClear: false,
        reserve: true,
        prePrice: undefined,
        preUnit: 60,
        minCharge: undefined,
        txPrice: undefined,
        morningPrice: undefined,
        afternoonPrice: undefined,
        nightPrice: undefined,
        nfcUrl: undefined,
        xiaoshiSubmit: true,
        status: 1,
      };
      this.labelList = [];
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
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
      this.title = "添加房间";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const roomId = row.roomId;
      getRoomInfo(roomId).then(response => {
        this.form = response.data;
        // 标签转多选
        this.labelList = (this.form.label || '').split(',').filter(Boolean);
        this.open = true;
        this.title = "修改房间";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 标签数组转逗号分隔
        this.form.label = (this.labelList || []).join(',');
        // 修改的提交
        if (this.form.roomId != null) {
          updateRoomInfo(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createRoomInfo(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const roomId = row.roomId;
      this.$modal.confirm('是否确认删除房间编号为"' + roomId + '"的数据项?').then(function() {
          return deleteRoomInfo(roomId);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams};
      params.pageNo = undefined;
      params.pageSize = undefined;
      this.$modal.confirm('是否确认导出所有房间数据项?').then(() => {
          this.exportLoading = true;
          return exportRoomInfoExcel(params);
        }).then(response => {
          this.$download.excel(response, '房间管理.xls');
          this.exportLoading = false;
        }).catch(() => {});
    }
  }
};
</script>
