<template>
  <div class="p-2">
    <el-card v-show="showSearch" shadow="hover" class="mb-[10px]">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="申请单号" prop="applicationNo"
          ><el-input v-model="queryParams.applicationNo" clearable @keyup.enter="handleQuery"
        /></el-form-item>
        <el-form-item label="申请标题" prop="title"><el-input v-model="queryParams.title" clearable @keyup.enter="handleQuery" /></el-form-item>
        <el-form-item label="申请类型" prop="applicationType"
          ><el-select v-model="queryParams.applicationType" clearable style="width: 130px"
            ><el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select
        ></el-form-item>
        <el-form-item label="状态" prop="status"
          ><el-select v-model="queryParams.status" clearable style="width: 130px"
            ><el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select
        ></el-form-item>
        <el-form-item
          ><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button
          ><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item
        >
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5"
            ><el-button v-hasPermi="['asset:application:add']" type="primary" plain icon="Plus" @click="handleAdd">新建申请</el-button></el-col
          >
          <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
        </el-row>
      </template>
      <el-table v-loading="loading" border :data="applicationList">
        <el-table-column label="申请单号" prop="applicationNo" min-width="190" />
        <el-table-column label="标题" prop="title" min-width="190" show-overflow-tooltip />
        <el-table-column label="类型" prop="applicationType" width="90" align="center"
          ><template #default="scope">{{ typeLabel(scope.row.applicationType) }}</template></el-table-column
        >
        <el-table-column label="申请部门" prop="applyDeptName" min-width="120" />
        <el-table-column label="预估金额" prop="totalAmount" width="125" align="right"
          ><template #default="scope">{{ formatMoney(scope.row.totalAmount) }}</template></el-table-column
        >
        <el-table-column label="期望日期" prop="expectedDate" width="115" />
        <el-table-column label="状态" prop="status" width="105" align="center"
          ><template #default="scope"
            ><el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag></template
          ></el-table-column
        >
        <el-table-column label="创建时间" prop="createTime" width="165" />
        <el-table-column label="操作" fixed="right" width="210" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
            <template v-if="isEditable(scope.row.status)">
              <el-button v-hasPermi="['asset:application:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
              <el-button v-hasPermi="['asset:application:submit']" link type="success" icon="Promotion" @click="handleSubmit(scope.row)"
                >提交</el-button
              >
              <el-button v-hasPermi="['asset:application:remove']" link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="1050px" append-to-body destroy-on-close>
      <el-form ref="applicationFormRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="8"
            ><el-form-item label="申请类型" prop="applicationType"
              ><el-select v-model="form.applicationType" style="width: 100%" @change="handleTypeChange"
                ><el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item
          ></el-col>
          <el-col :span="10"
            ><el-form-item label="申请标题" prop="title"><el-input v-model="form.title" maxlength="200" /></el-form-item
          ></el-col>
          <el-col :span="6"
            ><el-form-item label="期望日期" prop="expectedDate"
              ><el-date-picker v-model="form.expectedDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item
          ></el-col>
          <el-col :span="24"
            ><el-form-item label="申请原因" prop="reason"
              ><el-input v-model="form.reason" type="textarea" :rows="2" maxlength="1000" show-word-limit /></el-form-item
          ></el-col>
        </el-row>

        <div class="mb-2 flex items-center justify-between">
          <span class="font-medium">申请明细</span><el-button type="primary" plain icon="Plus" @click="addItem">添加明细</el-button>
        </div>
        <el-table border :data="form.items">
          <el-table-column label="已有资产" min-width="160">
            <template #default="scope"
              ><el-select
                v-model="scope.row.assetId"
                clearable
                filterable
                :disabled="form.applicationType === 'purchase'"
                placeholder="请选择"
                @change="(id) => selectAsset(scope.row, id)"
                ><el-option
                  v-for="asset in assetOptions"
                  :key="asset.assetId"
                  :label="`${asset.assetCode} ${asset.assetName}`"
                  :value="asset.assetId" /></el-select
            ></template>
          </el-table-column>
          <el-table-column label="资产分类" min-width="140"
            ><template #default="scope"
              ><el-tree-select
                v-model="scope.row.categoryId"
                :data="categoryOptions"
                :props="categoryTreeProps"
                value-key="categoryId"
                check-strictly
                style="width: 100%" /></template
          ></el-table-column>
          <el-table-column label="名称" min-width="145"
            ><template #default="scope"><el-input v-model="scope.row.itemName" maxlength="200" /></template
          ></el-table-column>
          <el-table-column label="规格型号" min-width="120"
            ><template #default="scope"><el-input v-model="scope.row.specification" /></template
          ></el-table-column>
          <el-table-column label="数量" width="90"
            ><template #default="scope"><el-input-number v-model="scope.row.quantity" :min="1" :controls="false" style="width: 100%" /></template
          ></el-table-column>
          <el-table-column label="单位" width="80"
            ><template #default="scope"><el-input v-model="scope.row.unit" /></template
          ></el-table-column>
          <el-table-column label="预估单价" width="120"
            ><template #default="scope"
              ><el-input-number v-model="scope.row.estimatedUnitPrice" :min="0" :precision="2" :controls="false" style="width: 100%" /></template
          ></el-table-column>
          <el-table-column label="操作" width="70" align="center"
            ><template #default="scope"><el-button link type="danger" icon="Delete" @click="removeItem(scope.$index)" /></template
          ></el-table-column>
        </el-table>
        <div class="mt-3 text-right">
          预估总金额：<strong>{{ formatMoney(formTotal) }}</strong>
        </div>
      </el-form>
      <template #footer
        ><el-button type="primary" :loading="buttonLoading" @click="saveForm">保存草稿</el-button
        ><el-button @click="dialog.visible = false">取消</el-button></template
      >
    </el-dialog>
  </div>
</template>

<script setup name="AssetApplication" lang="ts">
import {
  addAssetApplication,
  delAssetApplication,
  getAssetApplication,
  listAssetApplication,
  submitAssetApplication,
  updateAssetApplication
} from '@/api/asset/application';
import { AssetApplicationForm, AssetApplicationItemForm, AssetApplicationQuery, AssetApplicationVO } from '@/api/asset/application/types';
import { listAssetCategoryOptions } from '@/api/asset/category';
import { AssetCategoryVO } from '@/api/asset/category/types';
import { listAssetInfo } from '@/api/asset/info';
import { AssetInfoVO } from '@/api/asset/info/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const typeOptions = [
  { value: 'purchase', label: '采购' },
  { value: 'use', label: '领用' },
  { value: 'transfer', label: '调拨' },
  { value: 'return', label: '归还' },
  { value: 'scrap', label: '报废' }
];
const statusOptions = [
  { value: 'draft', label: '草稿' },
  { value: 'waiting', label: '审批中' },
  { value: 'finish', label: '已完成' },
  { value: 'back', label: '已退回' },
  { value: 'cancel', label: '已撤销' },
  { value: 'termination', label: '已终止' },
  { value: 'invalid', label: '已作废' }
];
const categoryTreeProps = { value: 'categoryId', label: 'categoryName', children: 'children' } as any;
const emptyItem = (): AssetApplicationItemForm => ({
  assetId: undefined,
  categoryId: undefined,
  itemName: '',
  specification: '',
  quantity: 1,
  unit: '台',
  estimatedUnitPrice: 0
});
const initForm = (): AssetApplicationForm => ({
  applicationId: undefined,
  applicationType: 'purchase',
  title: '',
  expectedDate: undefined,
  reason: '',
  remark: '',
  items: [emptyItem()]
});
const applicationList = ref<AssetApplicationVO[]>([]);
const categoryOptions = ref<AssetCategoryVO[]>([]);
const assetOptions = ref<AssetInfoVO[]>([]);
const loading = ref(false);
const buttonLoading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const applicationFormRef = ref<ElFormInstance>();
const queryFormRef = ref<ElFormInstance>();
const dialog = reactive<DialogOption>({ visible: false, title: '' });
const data = reactive<PageData<AssetApplicationForm, AssetApplicationQuery>>({
  form: initForm(),
  queryParams: { pageNum: 1, pageSize: 10, applicationNo: '', title: '', applicationType: '', status: '' },
  rules: {
    applicationType: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
    title: [{ required: true, message: '请输入申请标题', trigger: 'blur' }],
    reason: [{ required: true, message: '请输入申请原因', trigger: 'blur' }]
  }
});
const { form, queryParams, rules } = toRefs(data);
const formTotal = computed(() => form.value.items.reduce((sum, item) => sum + Number(item.estimatedUnitPrice || 0) * Number(item.quantity || 0), 0));
const typeLabel = (value: string) => typeOptions.find((item) => item.value === value)?.label || value;
const statusLabel = (value: string) => statusOptions.find((item) => item.value === value)?.label || value;
const statusType = (value: string) =>
  (({ draft: 'info', waiting: 'warning', finish: 'success', back: 'danger', cancel: 'info', termination: 'danger', invalid: 'danger' })[value] ||
    'info') as any;
const isEditable = (status: string) => ['draft', 'back', 'cancel'].includes(status);
const formatMoney = (value?: number) => Number(value || 0).toLocaleString('zh-CN', { style: 'currency', currency: 'CNY' });
const loadOptions = async () => {
  const [categoryRes, assetRes] = await Promise.all([listAssetCategoryOptions(), listAssetInfo({ pageNum: 1, pageSize: 200, assetStatus: '' })]);
  categoryOptions.value = proxy?.handleTree<AssetCategoryVO>(categoryRes.data, 'categoryId', 'parentId') || [];
  assetOptions.value = assetRes.rows;
};
const getList = async () => {
  loading.value = true;
  try {
    const res = await listAssetApplication(queryParams.value);
    applicationList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};
const handleAdd = async () => {
  form.value = initForm();
  await loadOptions();
  dialog.title = '新建资产申请';
  dialog.visible = true;
};
const handleUpdate = async (row: AssetApplicationVO) => {
  await loadOptions();
  const res = await getAssetApplication(row.applicationId);
  form.value = { ...res.data, items: res.data.items || [emptyItem()] };
  dialog.title = '编辑资产申请';
  dialog.visible = true;
};
const handleView = (row: AssetApplicationVO) =>
  proxy?.$router.push({ path: '/asset/application/detail/index', query: { id: row.applicationId, type: 'view' } });
const addItem = () => form.value.items.push(emptyItem());
const removeItem = (index: number) => {
  if (form.value.items.length === 1) return proxy?.$modal.msgWarning('至少保留一条明细');
  form.value.items.splice(index, 1);
};
const handleTypeChange = () => {
  form.value.items.forEach((item) => {
    item.assetId = undefined;
    if (form.value.applicationType !== 'purchase') {
      item.categoryId = undefined;
      item.itemName = '';
      item.specification = '';
    }
  });
};
const selectAsset = (item: AssetApplicationItemForm, assetId?: string | number) => {
  const asset = assetOptions.value.find((value) => value.assetId === assetId);
  if (!asset) return;
  item.categoryId = asset.categoryId;
  item.itemName = asset.assetName;
  item.specification = asset.specification;
  item.estimatedUnitPrice = Number(asset.originalValue || 0);
  item.quantity = 1;
};
const validateItems = () => {
  if (form.value.items.some((item) => !item.categoryId || !item.itemName || !item.quantity)) {
    proxy?.$modal.msgError('请完整填写申请明细的分类、名称和数量');
    return false;
  }
  if (form.value.applicationType !== 'purchase' && form.value.items.some((item) => !item.assetId)) {
    proxy?.$modal.msgError('非采购申请必须选择已有资产');
    return false;
  }
  return true;
};
const saveForm = () =>
  applicationFormRef.value?.validate(async (valid) => {
    if (!valid || !validateItems()) return;
    buttonLoading.value = true;
    try {
      form.value.applicationId ? await updateAssetApplication(form.value) : await addAssetApplication(form.value);
      proxy?.$modal.msgSuccess('草稿保存成功');
      dialog.visible = false;
      await getList();
    } finally {
      buttonLoading.value = false;
    }
  });
const handleSubmit = async (row: AssetApplicationVO) => {
  await proxy?.$modal.confirm(`确认提交“${row.title}”进入审批流程吗？`);
  await submitAssetApplication(row.applicationId);
  proxy?.$modal.msgSuccess('已提交审批');
  await getList();
};
const handleDelete = async (row: AssetApplicationVO) => {
  await proxy?.$modal.confirm(`确认删除申请“${row.title}”吗？`);
  await delAssetApplication(row.applicationId);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};
onMounted(getList);
</script>
