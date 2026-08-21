<template>
  <div class="p-2">
    <el-card v-show="showSearch" shadow="hover" class="mb-[10px]">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="资产编码" prop="assetCode">
          <el-input v-model="queryParams.assetCode" clearable placeholder="请输入资产编码" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="资产名称" prop="assetName">
          <el-input v-model="queryParams.assetName" clearable placeholder="请输入资产名称" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="变更类型" prop="businessType">
          <el-select v-model="queryParams.businessType" clearable placeholder="全部类型" style="width: 140px">
            <el-option label="领用" value="issue" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">资产全生命周期履历</span>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
        </div>
      </template>
      <el-table v-loading="loading" border :data="historyList">
        <el-table-column label="资产编码" prop="assetCode" min-width="145" />
        <el-table-column label="资产名称" prop="assetName" min-width="150" show-overflow-tooltip />
        <el-table-column label="业务类型" prop="businessType" width="95" align="center">
          <template #default="scope"
            ><el-tag>{{ businessLabel(scope.row.businessType) }}</el-tag></template
          >
        </el-table-column>
        <el-table-column label="状态变化" min-width="150" align="center">
          <template #default="scope">{{ statusLabel(scope.row.beforeStatus) }} → {{ statusLabel(scope.row.afterStatus) }}</template>
        </el-table-column>
        <el-table-column label="部门变化" min-width="190" show-overflow-tooltip>
          <template #default="scope">{{ changeText(scope.row.beforeDeptName, scope.row.afterDeptName) }}</template>
        </el-table-column>
        <el-table-column label="保管人变化" min-width="170" show-overflow-tooltip>
          <template #default="scope">{{ changeText(scope.row.beforeKeeperName, scope.row.afterKeeperName) }}</template>
        </el-table-column>
        <el-table-column label="地点变化" min-width="190" show-overflow-tooltip>
          <template #default="scope">{{ changeText(scope.row.beforeLocation, scope.row.afterLocation) }}</template>
        </el-table-column>
        <el-table-column label="操作人" prop="operatorName" width="110" />
        <el-table-column label="操作时间" prop="operationTime" width="165" />
        <el-table-column label="说明" prop="description" min-width="230" show-overflow-tooltip />
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="AssetHistory" lang="ts">
import { listAssetHistory } from '@/api/asset/history';
import { AssetHistoryQuery, AssetHistoryVO } from '@/api/asset/history/types';

const route = useRoute();
const historyList = ref<AssetHistoryVO[]>([]);
const loading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const queryFormRef = ref<ElFormInstance>();
const queryParams = reactive<AssetHistoryQuery>({
  pageNum: 1,
  pageSize: 10,
  assetId: route.query.assetId as string | undefined,
  assetCode: '',
  assetName: '',
  businessType: ''
});
const statuses: Record<string, string> = { '0': '库存', '1': '使用中', '2': '维修中', '3': '调拨中', '4': '已报废' };

const statusLabel = (value?: string) => (value == null ? '-' : statuses[value] || value);
const businessLabel = (value: string) => ({ issue: '领用' })[value] || value;
const changeText = (before?: string, after?: string) => `${before || '-'} → ${after || '-'}`;
const getList = async () => {
  loading.value = true;
  try {
    const res = await listAssetHistory(queryParams);
    historyList.value = res.rows;
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
  queryParams.assetId = undefined;
  handleQuery();
};

onMounted(getList);
</script>
