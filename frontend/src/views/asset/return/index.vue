<template>
  <div class="p-2">
    <el-card v-show="showSearch" shadow="hover" class="mb-[10px]">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="归还单号" prop="returnNo">
          <el-input v-model="queryParams.returnNo" clearable placeholder="请输入归还单号" @keyup.enter="handleQuery" />
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
      <el-table v-loading="loading" border :data="returnList">
        <el-table-column label="归还单号" prop="returnNo" min-width="205" />
        <el-table-column label="来源申请" min-width="220" show-overflow-tooltip>
          <template #default="scope">
            <div>{{ scope.row.applicationTitle || '-' }}</div>
            <span class="text-xs text-gray-400">{{ scope.row.applicationNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="归还人" prop="returnerName" width="110" />
        <el-table-column label="归还部门" prop="returnDeptName" min-width="130" />
        <el-table-column label="状态" prop="status" width="95" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'completed' ? 'success' : 'warning'">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="执行人" prop="returnedByName" width="110" />
        <el-table-column label="执行时间" prop="returnedTime" width="165" />
        <el-table-column label="创建时间" prop="createTime" width="165" />
        <el-table-column label="操作" fixed="right" width="145" align="center">
          <template #default="scope">
            <el-button v-hasPermi="['asset:return:query']" link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              v-hasPermi="['asset:return:execute']"
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

    <el-dialog v-model="detailVisible" title="资产归还执行单详情" width="920px" append-to-body>
      <el-descriptions v-if="currentReturn" :column="3" border>
        <el-descriptions-item label="归还单号">{{ currentReturn.returnNo }}</el-descriptions-item>
        <el-descriptions-item label="来源申请">{{ currentReturn.applicationNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(currentReturn.status) }}</el-descriptions-item>
        <el-descriptions-item label="归还人">{{ currentReturn.returnerName }}</el-descriptions-item>
        <el-descriptions-item label="归还部门">{{ currentReturn.returnDeptName }}</el-descriptions-item>
        <el-descriptions-item label="执行人">{{ currentReturn.returnedByName || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="mb-2 mt-4 font-medium">待归还资产</div>
      <el-table border :data="currentReturn?.items || []">
        <el-table-column label="资产名称" prop="itemName" min-width="160" />
        <el-table-column label="规格型号" prop="specification" min-width="140" />
        <el-table-column label="数量" prop="quantity" width="80" align="center" />
        <el-table-column label="归还地点" prop="targetLocation" min-width="160" />
      </el-table>
      <el-alert
        class="mt-4"
        type="warning"
        :closable="false"
        title="执行后，资产将从“使用中”变为“库存”，清空使用部门和保管人，并自动生成不可编辑的资产履历。"
      />
      <template #footer>
        <el-button
          v-if="currentReturn?.status === 'pending'"
          v-hasPermi="['asset:return:execute']"
          type="primary"
          :loading="executeLoading"
          @click="handleExecute(currentReturn)"
          >确认归还</el-button
        >
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AssetReturnOrder" lang="ts">
import { executeAssetReturnOrder, getAssetReturnOrder, listAssetReturnOrder } from '@/api/asset/return';
import { AssetReturnOrderQuery, AssetReturnOrderVO } from '@/api/asset/return/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const returnList = ref<AssetReturnOrderVO[]>([]);
const currentReturn = ref<AssetReturnOrderVO>();
const loading = ref(false);
const executeLoading = ref(false);
const showSearch = ref(true);
const detailVisible = ref(false);
const total = ref(0);
const queryFormRef = ref<ElFormInstance>();
const queryParams = reactive<AssetReturnOrderQuery>({ pageNum: 1, pageSize: 10, returnNo: '', status: '' });

const statusLabel = (status: string) => ({ pending: '待执行', completed: '已完成' })[status] || status;
const getList = async () => {
  loading.value = true;
  try {
    const res = await listAssetReturnOrder(queryParams);
    returnList.value = res.rows;
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
const handleView = async (row: AssetReturnOrderVO) => {
  const res = await getAssetReturnOrder(row.returnId);
  currentReturn.value = res.data;
  detailVisible.value = true;
};
const handleExecute = async (row: AssetReturnOrderVO) => {
  await proxy?.$modal.confirm(`确认执行归还单“${row.returnNo}”吗？执行后将释放使用关系并写入资产履历。`);
  executeLoading.value = true;
  try {
    await executeAssetReturnOrder(row.returnId);
    proxy?.$modal.msgSuccess('归还执行成功，资产已回库并生成履历');
    detailVisible.value = false;
    await getList();
  } finally {
    executeLoading.value = false;
  }
};

onMounted(getList);
</script>
