<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.farmerVerifications)
const pagination = computed(() => moderationStore.pagination('farmerVerifications'))
const detailVisible = ref(false)
const detailRow = ref(null)

function handlePageChange(page) {
  moderationStore.hydrateSection('farmerVerifications', { page })
}

function showDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

async function handleReview(row, approved) {
  let reason = ''

  if (!approved) {
    const result = await ElMessageBox.prompt('请输入驳回原因', '审核驳回', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPlaceholder: '原因将同步给农户',
    }).catch(() => null)

    if (!result) {
      return
    }

    reason = result.value
  } else {
    try {
      await ElMessageBox.confirm('确认通过该农户认证？', '审核通过', { type: 'warning', confirmButtonText: '通过', cancelButtonText: '取消' })
    } catch {
      return
    }
  }

  await moderationStore.reviewFarmerVerification(row.id, approved, reason)
  ElMessage.success(approved ? '已通过认证审核' : '已驳回认证审核')
}

onMounted(() => {
  moderationStore.hydrateSection('farmerVerifications')
})
</script>

<template>
  <PageContainer title="农户认证审核">
    <template #actions>
      <el-button @click="moderationStore.hydrateSection('farmerVerifications')">刷新</el-button>
    </template>

    <el-alert
      v-if="moderationStore.error"
      type="warning"
      show-icon
      :closable="false"
      :title="moderationStore.error"
      style="margin-bottom: 16px"
    />

    <el-table v-loading="moderationStore.loadingMap.farmerVerifications" :data="rows" border>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="farmer" label="农户姓名" />
      <el-table-column prop="contact" label="联系方式" width="140" />
      <el-table-column prop="submittedAt" label="提交时间" width="180" />
      <el-table-column label="状态" width="120">
        <template #default="scope">
          <el-tag
            :type="
              scope.row.status === 'approved'
                ? 'success'
                : scope.row.status === 'rejected'
                  ? 'danger'
                  : 'warning'
            "
            >{{
              scope.row.status === 'approved'
                ? '已通过'
                : scope.row.status === 'rejected'
                  ? '已驳回'
                  : '待审核'
            }}</el-tag
          >
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-space>
            <el-button text type="info" @click="showDetail(scope.row)">详情</el-button>
            <el-button text type="primary" @click="handleReview(scope.row, true)">通过</el-button>
            <el-button text type="danger" @click="handleReview(scope.row, false)">驳回</el-button>
          </el-space>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无认证申请" />
      </template>
    </el-table>

    <div v-if="pagination.total > pagination.pageSize" style="text-align: center; margin-top: 16px">
      <el-pagination
        :current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="detailVisible" title="认证详情" width="480px">
      <el-descriptions v-if="detailRow" border :column="1">
        <el-descriptions-item label="农户姓名">{{ detailRow.farmer }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ detailRow.realName }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detailRow.idNumber }}</el-descriptions-item>
        <el-descriptions-item label="统一社会信用代码">{{ detailRow.businessNo }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ detailRow.contact }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detailRow.submittedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="detailRow.reason" label="审核意见">{{ detailRow.reason }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </PageContainer>
</template>
