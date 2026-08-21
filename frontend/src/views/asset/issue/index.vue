<template>
  <div class="p-2">
    <el-card v-show="showSearch" shadow="hover" class="mb-[10px]">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="执行单号" prop="issueNo">
          <el-input v-model="queryParams.issueNo" clearable placeholder="请输入执行单号" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="执行状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="待执行" value="pending" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <template #header><right-toolbar v-model:show-search="showSearch" @query-table="getList" /></template>
      <el-table v-loading="loading" border :data="issueList">
        <el-table-column label="执行单号" prop="issueNo" min-width="205" />
        <el-table-column label="来源申请" min-width="220" show-overflow-tooltip>
          <template #default="scope">
            <div>{{ scope.row.applicationTitle || '-' }}</div>
            <span class="text-xs text-gray-400">{{ scope.row.applicationNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="领用人" prop="recipientName" width="110" />
        <el-table-column label="领用部门" prop="recipientDeptName" min-width="130" />
        <el-table-column label="状态" prop="status" width="95" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'completed' ? 'success' : 'warning'">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="执行人" prop="issuedByName" width="110" />
        <el-table-column label="执行时间" prop="issuedTime" width="165" />
        <el-table-column label="创建时间" prop="createTime" width="165" />
        <el-table-column label="操作" fixed="right" width="145" align="center">
          <template #default="scope">
            <el-button v-hasPermi="['asset:issue:query']" link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              v-hasPermi="['asset:issue:execute']"
              link
              type="success"
              icon="Select"
              @click="handleExecute(scope.row)"
              >执行</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="detailVisible" title="领用执行单详情" width="920px" append-to-body>
      <el-descriptions v-if="currentIssue" :column="3" border>
        <el-descriptions-item label="执行单号">{{ currentIssue.issueNo }}</el-descriptions-item>
        <el-descriptions-item label="来源申请">{{ currentIssue.applicationNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(currentIssue.status) }}</el-descriptions-item>
        <el-descriptions-item label="领用人">{{ currentIssue.recipientName }}</el-descriptions-item>
        <el-descriptions-item label="领用部门">{{ currentIssue.recipientDeptName }}</el-descriptions-item>
        <el-descriptions-item label="执行人">{{ currentIssue.issuedByName || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="mb-2 mt-4 font-medium">待领用资产</div>
      <el-table border :data="currentIssue?.items || []">
        <el-table-column label="资产名称" prop="itemName" min-width="160" />
        <el-table-column label="规格型号" prop="specification" min-width="140" />
        <el-table-column label="数量" prop="quantity" width="80" align="center" />
        <el-table-column label="目标部门" prop="targetDeptName" min-width="130" />
        <el-table-column label="目标地点" prop="targetLocation" min-width="150" />
      </el-table>
      <el-alert class="mt-4" type="info" :closable="false" title="执行后，资产将从“库存”变为“使用中”，并自动生成不可编辑的资产履历。" />
      <template #footer>
        <el-button
          v-if="currentIssue?.status === 'pending'"
          v-hasPermi="['asset:issue:execute']"
          type="primary"
          :loading="executeLoading"
          @click="handleExecute(currentIssue)"
          >确认执行</el-button
        >
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AssetIssueOrder" lang="ts">
import { executeAssetIssueOrder, getAssetIssueOrder, listAssetIssueOrder } from '@/api/asset/issue';
import { AssetIssueOrderQuery, AssetIssueOrderVO } from '@/api/asset/issue/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const issueList = ref<AssetIssueOrderVO[]>([]);
const currentIssue = ref<AssetIssueOrderVO>();
const loading = ref(false);
const executeLoading = ref(false);
const showSearch = ref(true);
const detailVisible = ref(false);
const total = ref(0);
const queryFormRef = ref<ElFormInstance>();
const queryParams = reactive<AssetIssueOrderQuery>({ pageNum: 1, pageSize: 10, issueNo: '', status: '' });

const statusLabel = (status: string) => ({ pending: '待执行', completed: '已完成' })[status] || status;
const getList = async () => {
  loading.value = true;
  try {
    const res = await listAssetIssueOrder(queryParams);
    issueList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};
const handleView = async (row: AssetIssueOrderVO) => {
  const res = await getAssetIssueOrder(row.issueId);
  currentIssue.value = res.data;
  detailVisible.value = true;
};
const handleExecute = async (row: AssetIssueOrderVO) => {
  await proxy?.$modal.confirm(`确认执行领用单“${row.issueNo}”吗？执行后将更新资产状态并写入履历。`);
  executeLoading.value = true;
  try {
    await executeAssetIssueOrder(row.issueId);
    proxy?.$modal.msgSuccess('领用执行成功，资产履历已生成');
    detailVisible.value = false;
    await getList();
  } finally {
    executeLoading.value = false;
  }
};

onMounted(getList);
</script>
