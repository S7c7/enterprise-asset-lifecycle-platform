<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="search">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item label="分类编码" prop="categoryCode">
            <el-input v-model="queryParams.categoryCode" placeholder="请输入分类编码" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="分类名称" prop="categoryName">
            <el-input v-model="queryParams.categoryName" placeholder="请输入分类名称" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </transition>

    <el-card shadow="never">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['asset:category:add']" type="primary" plain icon="Plus" @click="handleAdd()">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="info" plain icon="Sort" @click="handleToggleExpandAll">展开/折叠</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
        </el-row>
      </template>
      <el-table ref="categoryTableRef" v-loading="loading" :data="categoryList" row-key="categoryId" border :default-expand-all="isExpandAll">
        <el-table-column label="分类名称" prop="categoryName" min-width="220" />
        <el-table-column label="分类编码" prop="categoryCode" min-width="180" />
        <el-table-column label="折旧年限" prop="depreciationYears" width="110" align="center">
          <template #default="scope">{{ scope.row.depreciationYears ? `${scope.row.depreciationYears} 年` : '-' }}</template>
        </el-table-column>
        <el-table-column label="排序" prop="orderNum" width="80" align="center" />
        <el-table-column label="状态" prop="status" width="90" align="center">
          <template #default="scope"
            ><el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag></template
          >
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" fixed="right" align="center" width="150">
          <template #default="scope">
            <el-button v-hasPermi="['asset:category:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" />
            <el-button v-hasPermi="['asset:category:add']" link type="primary" icon="Plus" @click="handleAdd(scope.row)" />
            <el-button v-hasPermi="['asset:category:remove']" link type="danger" icon="Delete" @click="handleDelete(scope.row)" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="620px" append-to-body>
      <el-form ref="categoryFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级分类" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryOptions"
            :props="{ value: 'categoryId', label: 'categoryName', children: 'children' } as any"
            value-key="categoryId"
            check-strictly
            clearable
            placeholder="不选择表示根分类"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"
            ><el-form-item label="分类编码" prop="categoryCode"
              ><el-input v-model="form.categoryCode" maxlength="32" placeholder="如 IT_DEVICE" /></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="分类名称" prop="categoryName"><el-input v-model="form.categoryName" maxlength="100" /></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="折旧年限" prop="depreciationYears"
              ><el-input-number v-model="form.depreciationYears" :min="0" :max="100" controls-position="right" /></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="显示顺序" prop="orderNum"
              ><el-input-number v-model="form.orderNum" :min="0" controls-position="right" /></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="状态" prop="status"
              ><el-radio-group v-model="form.status"
                ><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group
              ></el-form-item
            ></el-col
          >
          <el-col :span="24"
            ><el-form-item label="备注" prop="remark"
              ><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit /></el-form-item
          ></el-col>
        </el-row>
      </el-form>
      <template #footer
        ><el-button type="primary" :loading="buttonLoading" @click="submitForm">确定</el-button><el-button @click="cancel">取消</el-button></template
      >
    </el-dialog>
  </div>
</template>

<script setup name="AssetCategory" lang="ts">
import { addAssetCategory, delAssetCategory, getAssetCategory, listAssetCategory, updateAssetCategory } from '@/api/asset/category';
import { AssetCategoryForm, AssetCategoryQuery, AssetCategoryVO } from '@/api/asset/category/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const categoryList = ref<AssetCategoryVO[]>([]);
const categoryOptions = ref<AssetCategoryVO[]>([]);
const loading = ref(false);
const buttonLoading = ref(false);
const showSearch = ref(true);
const isExpandAll = ref(true);
const queryFormRef = ref<ElFormInstance>();
const categoryFormRef = ref<ElFormInstance>();
const categoryTableRef = ref<ElTableInstance>();
const dialog = reactive<DialogOption>({ visible: false, title: '' });

const initFormData: AssetCategoryForm = {
  categoryId: undefined,
  parentId: undefined,
  categoryCode: '',
  categoryName: '',
  depreciationYears: 0,
  orderNum: 0,
  status: '0',
  remark: ''
};
const data = reactive<PageData<AssetCategoryForm, AssetCategoryQuery>>({
  form: { ...initFormData },
  queryParams: { categoryCode: '', categoryName: '', status: '' },
  rules: {
    categoryCode: [
      { required: true, message: '分类编码不能为空', trigger: 'blur' },
      { pattern: /^[A-Za-z0-9_-]+$/, message: '只能使用字母、数字、下划线和中划线', trigger: 'blur' }
    ],
    categoryName: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  }
});
const { queryParams, form, rules } = toRefs(data);

const getList = async () => {
  loading.value = true;
  try {
    const res = await listAssetCategory(queryParams.value);
    categoryList.value = proxy?.handleTree<AssetCategoryVO>(res.data, 'categoryId', 'parentId') || [];
  } finally {
    loading.value = false;
  }
};

const getTreeSelect = async () => {
  const res = await listAssetCategory();
  categoryOptions.value = proxy?.handleTree<AssetCategoryVO>(res.data, 'categoryId', 'parentId') || [];
};
const reset = () => {
  form.value = { ...initFormData };
  categoryFormRef.value?.resetFields();
};
const cancel = () => {
  dialog.visible = false;
  reset();
};
const handleQuery = () => getList();
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};
const handleAdd = async (row?: AssetCategoryVO) => {
  reset();
  await getTreeSelect();
  form.value.parentId = row?.categoryId;
  dialog.title = '新增资产分类';
  dialog.visible = true;
};
const handleUpdate = async (row: AssetCategoryVO) => {
  reset();
  await getTreeSelect();
  const res = await getAssetCategory(row.categoryId);
  Object.assign(form.value, res.data);
  dialog.title = '修改资产分类';
  dialog.visible = true;
};
const submitForm = () =>
  categoryFormRef.value?.validate(async (valid) => {
    if (!valid) return;
    buttonLoading.value = true;
    try {
      form.value.categoryId ? await updateAssetCategory(form.value) : await addAssetCategory(form.value);
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    } finally {
      buttonLoading.value = false;
    }
  });
const handleDelete = async (row: AssetCategoryVO) => {
  await proxy?.$modal.confirm(`确认删除资产分类“${row.categoryName}”吗？`);
  await delAssetCategory(row.categoryId);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};
const toggleExpandAll = (items: AssetCategoryVO[], expanded: boolean) =>
  items.forEach((item) => {
    categoryTableRef.value?.toggleRowExpansion(item, expanded);
    if (item.children?.length) toggleExpandAll(item.children, expanded);
  });
const handleToggleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value;
  toggleExpandAll(categoryList.value, isExpandAll.value);
};
onMounted(getList);
</script>
