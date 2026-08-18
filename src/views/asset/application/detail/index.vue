<template>
  <div class="p-2">
    <el-card shadow="never" class="mb-2">
      <approval-button :id="form.applicationId" :status="form.status" :page-type="routeParams.type" :button-loading="buttonLoading" @approval-verify-open="approvalVerifyOpen" @handle-approval-record="handleApprovalRecord" />
    </el-card>
    <el-card v-loading="loading" shadow="never">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="申请单号">{{ form.applicationNo }}</el-descriptions-item>
        <el-descriptions-item label="申请类型">{{ typeLabel(form.applicationType) }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusType(form.status)">{{ statusLabel(form.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="申请标题" :span="2">{{ form.title }}</el-descriptions-item>
        <el-descriptions-item label="期望日期">{{ form.expectedDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ form.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="申请部门">{{ form.applyDeptName }}</el-descriptions-item>
        <el-descriptions-item label="预估金额">{{ formatMoney(form.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="申请原因" :span="3">{{ form.reason }}</el-descriptions-item>
      </el-descriptions>
      <h4 class="mt-5 mb-2">申请明细</h4>
      <el-table border :data="form.items">
        <el-table-column type="index" label="#" width="55" />
        <el-table-column label="资产名称" prop="itemName" min-width="150" />
        <el-table-column label="分类" prop="categoryName" min-width="120" />
        <el-table-column label="规格型号" prop="specification" min-width="130" />
        <el-table-column label="数量" prop="quantity" width="80" align="right" />
        <el-table-column label="单位" prop="unit" width="70" />
        <el-table-column label="预估单价" prop="estimatedUnitPrice" width="120" align="right"><template #default="scope">{{ formatMoney(scope.row.estimatedUnitPrice) }}</template></el-table-column>
        <el-table-column label="目标部门" prop="targetDeptName" min-width="120" />
        <el-table-column label="目标地点" prop="targetLocation" min-width="130" />
      </el-table>
    </el-card>
    <submit-verify ref="submitVerifyRef" :task-variables="{}" @submit-callback="submitCallback" />
    <approval-record ref="approvalRecordRef" />
  </div>
</template>

<script setup name="AssetApplicationDetail" lang="ts">
import { getAssetApplication } from '@/api/asset/application';
import { AssetApplicationVO } from '@/api/asset/application/types';
import SubmitVerify from '@/components/Process/submitVerify.vue';
import ApprovalRecord from '@/components/Process/approvalRecord.vue';
import ApprovalButton from '@/components/Process/approvalButton.vue';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const loading = ref(true);
const buttonLoading = ref(false);
const routeParams = ref<Record<string, any>>({});
const submitVerifyRef = ref<InstanceType<typeof SubmitVerify>>();
const approvalRecordRef = ref<InstanceType<typeof ApprovalRecord>>();
const form = ref<AssetApplicationVO>({} as AssetApplicationVO);
const types: Record<string, string> = { purchase: '采购', use: '领用', transfer: '调拨', return: '归还', scrap: '报废' };
const statuses: Record<string, string> = { draft: '草稿', waiting: '审批中', finish: '已完成', back: '已退回', cancel: '已撤销', termination: '已终止', invalid: '已作废' };
const typeLabel = (value: string) => types[value] || value;
const statusLabel = (value: string) => statuses[value] || value;
const statusType = (value: string) => ({ waiting: 'warning', finish: 'success', back: 'danger', termination: 'danger', invalid: 'danger' }[value] || 'info') as any;
const formatMoney = (value?: number) => Number(value || 0).toLocaleString('zh-CN', { style: 'currency', currency: 'CNY' });
const approvalVerifyOpen = () => submitVerifyRef.value?.openDialog(routeParams.value.taskId);
const handleApprovalRecord = () => approvalRecordRef.value?.init(form.value.applicationId);
const submitCallback = async () => { await proxy.$tab.closePage(proxy.$route); proxy.$router.go(-1); };
onMounted(async () => { routeParams.value = proxy.$route.query; try { const res = await getAssetApplication(routeParams.value.id); form.value = res.data; } finally { loading.value = false; } });
</script>
