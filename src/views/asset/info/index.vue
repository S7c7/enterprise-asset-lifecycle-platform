<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="资产编码" prop="assetCode"><el-input v-model="queryParams.assetCode" clearable placeholder="请输入资产编码" @keyup.enter="handleQuery" /></el-form-item>
            <el-form-item label="资产名称" prop="assetName"><el-input v-model="queryParams.assetName" clearable placeholder="请输入资产名称" @keyup.enter="handleQuery" /></el-form-item>
            <el-form-item label="资产分类" prop="categoryId">
              <el-tree-select v-model="queryParams.categoryId" :data="categoryOptions" :props="categoryTreeProps" value-key="categoryId" check-strictly clearable placeholder="全部分类" style="width: 180px" />
            </el-form-item>
            <el-form-item label="资产状态" prop="assetStatus">
              <el-select v-model="queryParams.assetStatus" clearable placeholder="全部状态" style="width: 140px"><el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5"><el-button v-hasPermi="['asset:info:add']" type="primary" plain icon="Plus" @click="handleAdd">新增资产</el-button></el-col>
          <el-col :span="1.5"><el-button v-hasPermi="['asset:info:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button></el-col>
          <el-col :span="1.5"><el-button v-hasPermi="['asset:info:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button></el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
        </el-row>
      </template>
      <el-table v-loading="loading" border :data="assetList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="资产编码" prop="assetCode" min-width="140" show-overflow-tooltip />
        <el-table-column label="资产名称" prop="assetName" min-width="160" show-overflow-tooltip />
        <el-table-column label="分类" prop="categoryName" min-width="120" />
        <el-table-column label="规格型号" prop="specification" min-width="140" show-overflow-tooltip />
        <el-table-column label="资产原值" prop="originalValue" width="120" align="right"><template #default="scope">{{ formatMoney(scope.row.originalValue) }}</template></el-table-column>
        <el-table-column label="状态" prop="assetStatus" width="95" align="center"><template #default="scope"><el-tag :type="statusType(scope.row.assetStatus)">{{ statusLabel(scope.row.assetStatus) }}</el-tag></template></el-table-column>
        <el-table-column label="使用部门" prop="deptName" min-width="120" />
        <el-table-column label="保管人" prop="keeperName" width="110" />
        <el-table-column label="存放地点" prop="location" min-width="150" show-overflow-tooltip />
        <el-table-column label="购置日期" prop="purchaseDate" width="115" />
        <el-table-column label="操作" fixed="right" align="center" width="110">
          <template #default="scope">
            <el-button v-hasPermi="['asset:info:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" />
            <el-button v-hasPermi="['asset:info:remove']" link type="danger" icon="Delete" @click="handleDelete(scope.row)" />
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="820px" append-to-body>
      <el-form ref="assetFormRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="资产编码" prop="assetCode"><el-input v-model="form.assetCode" maxlength="64" placeholder="如 ZC-2026-0001" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="资产名称" prop="assetName"><el-input v-model="form.assetName" maxlength="200" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="资产分类" prop="categoryId"><el-tree-select v-model="form.categoryId" :data="categoryOptions" :props="categoryTreeProps" value-key="categoryId" check-strictly placeholder="请选择分类" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="资产状态" prop="assetStatus"><el-select v-model="form.assetStatus" style="width: 100%"><el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="品牌" prop="brand"><el-input v-model="form.brand" maxlength="100" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="规格型号" prop="specification"><el-input v-model="form.specification" maxlength="200" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="计量单位" prop="unit"><el-input v-model="form.unit" maxlength="20" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="资产原值" prop="originalValue"><el-input-number v-model="form.originalValue" :min="0" :precision="2" :controls="false" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="购置日期" prop="purchaseDate"><el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="使用部门" prop="deptId"><el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'label', children: 'children' } as any" value-key="id" check-strictly clearable placeholder="请选择部门" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="保管人" prop="keeperId"><el-input v-model="form.keeperName" readonly clearable placeholder="点击选择保管人" @clear="clearKeeper"><template #append><el-button icon="User" @click="openKeeperSelect" /></template></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="存放地点" prop="location"><el-input v-model="form.location" maxlength="200" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="保修到期" prop="warrantyExpiryDate"><el-date-picker v-model="form.warrantyExpiryDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button type="primary" :loading="buttonLoading" @click="submitForm">确定</el-button><el-button @click="cancel">取消</el-button></template>
    </el-dialog>

    <UserSelect ref="keeperSelectRef" :multiple="false" :data="form.keeperId" @confirm-call-back="handleKeeperSelected" />
  </div>
</template>

<script setup name="AssetInfo" lang="ts">
import { addAssetInfo, delAssetInfo, getAssetInfo, listAssetInfo, updateAssetInfo } from '@/api/asset/info';
import { AssetInfoForm, AssetInfoQuery, AssetInfoVO } from '@/api/asset/info/types';
import { listAssetCategoryOptions } from '@/api/asset/category';
import { AssetCategoryVO } from '@/api/asset/category/types';
import { deptTreeSelect } from '@/api/system/user';
import { DeptTreeVO } from '@/api/system/dept/types';
import { UserVO } from '@/api/system/user/types';
import UserSelect from '@/components/UserSelect';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const statusOptions = [
  { value: '0', label: '库存' }, { value: '1', label: '使用中' }, { value: '2', label: '维修中' }, { value: '3', label: '调拨中' }, { value: '4', label: '已报废' }
];
const categoryTreeProps = { value: 'categoryId', label: 'categoryName', children: 'children' } as any;
const assetList = ref<AssetInfoVO[]>([]);
const categoryOptions = ref<AssetCategoryVO[]>([]);
const deptOptions = ref<DeptTreeVO[]>([]);
const ids = ref<Array<string | number>>([]);
const loading = ref(false);
const buttonLoading = ref(false);
const showSearch = ref(true);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const queryFormRef = ref<ElFormInstance>();
const assetFormRef = ref<ElFormInstance>();
const keeperSelectRef = ref<InstanceType<typeof UserSelect>>();
const dialog = reactive<DialogOption>({ visible: false, title: '' });

const initFormData: AssetInfoForm = { assetId: undefined, assetCode: '', assetName: '', categoryId: undefined, specification: '', brand: '', unit: '台', purchaseDate: undefined, originalValue: 0, assetStatus: '0', deptId: undefined, keeperId: undefined, keeperName: '', location: '', warrantyExpiryDate: undefined, remark: '' };
const data = reactive<PageData<AssetInfoForm, AssetInfoQuery>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10, assetCode: '', assetName: '', categoryId: undefined, assetStatus: '', deptId: undefined },
  rules: {
    assetCode: [{ required: true, message: '资产编码不能为空', trigger: 'blur' }, { pattern: /^[A-Za-z0-9_-]+$/, message: '只能使用字母、数字、下划线和中划线', trigger: 'blur' }],
    assetName: [{ required: true, message: '资产名称不能为空', trigger: 'blur' }],
    categoryId: [{ required: true, message: '请选择资产分类', trigger: 'change' }],
    assetStatus: [{ required: true, message: '请选择资产状态', trigger: 'change' }]
  }
});
const { queryParams, form, rules } = toRefs(data);

const loadOptions = async () => {
  const [categoryRes, deptRes] = await Promise.all([listAssetCategoryOptions(), deptTreeSelect()]);
  categoryOptions.value = proxy?.handleTree<AssetCategoryVO>(categoryRes.data, 'categoryId', 'parentId') || [];
  deptOptions.value = deptRes.data;
};
const getList = async () => { loading.value = true; try { const res = await listAssetInfo(queryParams.value); assetList.value = res.rows; total.value = res.total; } finally { loading.value = false; } };
const statusLabel = (value: string) => statusOptions.find((item) => item.value === value)?.label || value;
const statusType = (value: string) => ({ '0': 'info', '1': 'success', '2': 'warning', '3': 'primary', '4': 'danger' }[value] || 'info') as any;
const formatMoney = (value?: number) => Number(value || 0).toLocaleString('zh-CN', { style: 'currency', currency: 'CNY' });
const reset = () => { form.value = { ...initFormData }; assetFormRef.value?.resetFields(); };
const cancel = () => { dialog.visible = false; reset(); };
const handleQuery = () => { queryParams.value.pageNum = 1; getList(); };
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery(); };
const handleSelectionChange = (selection: AssetInfoVO[]) => { ids.value = selection.map((item) => item.assetId); single.value = selection.length !== 1; multiple.value = selection.length === 0; };
const handleAdd = async () => { reset(); await loadOptions(); dialog.title = '新增资产'; dialog.visible = true; };
const handleUpdate = async (row?: AssetInfoVO) => { reset(); await loadOptions(); const assetId = row?.assetId || ids.value[0]; const res = await getAssetInfo(assetId); Object.assign(form.value, res.data); dialog.title = '修改资产'; dialog.visible = true; };
const submitForm = () => assetFormRef.value?.validate(async (valid) => { if (!valid) return; buttonLoading.value = true; try { form.value.assetId ? await updateAssetInfo(form.value) : await addAssetInfo(form.value); proxy?.$modal.msgSuccess('操作成功'); dialog.visible = false; await getList(); } finally { buttonLoading.value = false; } });
const handleDelete = async (row?: AssetInfoVO) => { const assetIds = row?.assetId || ids.value; await proxy?.$modal.confirm(`确认删除资产编号为“${assetIds}”的数据吗？只有库存资产允许删除。`); await delAssetInfo(assetIds); proxy?.$modal.msgSuccess('删除成功'); await getList(); };
const openKeeperSelect = () => keeperSelectRef.value?.open();
const handleKeeperSelected = (users: UserVO[]) => { const user = users[0]; form.value.keeperId = user?.userId; form.value.keeperName = user?.nickName || ''; };
const clearKeeper = () => { form.value.keeperId = undefined; form.value.keeperName = ''; };
onMounted(async () => { await loadOptions(); await getList(); });
</script>
