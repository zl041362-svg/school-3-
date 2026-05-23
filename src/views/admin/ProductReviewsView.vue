<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'
import { batchReviewProductsApi } from '@/api/modules/admin'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.productReviews)
const pagination = computed(() => moderationStore.pagination('productReviews'))
const batchSelect = ref([])

function handlePageChangeReviews(page) {
  moderationStore.hydrateSection('productReviews', { page })
}

async function handleReview(row, approved) {
  let reason = ''
  if (!approved) {
    const result = await ElMessageBox.prompt('请输入驳回原因', '商品驳回', {
      confirmButtonText: '提交', cancelButtonText: '取消', inputPlaceholder: '原因将同步给农户',
    }).catch(() => null)
    if (!result) return; reason = result.value
  } else {
    try { await ElMessageBox.confirm('确认通过该商品审核？', '审核通过', { type: 'warning', confirmButtonText: '通过', cancelButtonText: '取消' }) } catch { return }
  }
  await moderationStore.reviewProduct(row.id, approved, reason)
  ElMessage.success(approved ? '商品审核已通过' : '商品审核已驳回')
  batchSelect.value = []
}

async function batchReview(approved) {
  if (!batchSelect.value.length) { ElMessage.warning('请先选择审核项'); return }
  let reason = ''
  if (!approved) {
    const result = await ElMessageBox.prompt('请输入批量驳回原因', '批量驳回', { confirmButtonText: '提交', cancelButtonText: '取消' }).catch(() => null)
    if (!result) return; reason = result.value
  } else {
    try { await ElMessageBox.confirm(`确认批量通过所选 ${batchSelect.value.length} 条审核？`, '批量审核', { type: 'warning' }) } catch { return }
  }
  try {
    await batchReviewProductsApi({ ids: batchSelect.value.map((r) => r.id), approved, reason })
    ElMessage.success(approved ? '批量审核通过' : '批量驳回完成')
    batchSelect.value = []
    moderationStore.hydrateSection('productReviews')
    moderationStore.hydrateSection('products')
  } catch (err) { ElMessage.error(err?.message || '批量审核失败') }
}

onMounted(() => {
  moderationStore.hydrateSection('productReviews')
  moderationStore.hydrateSection('products')
})
</script>

<template>
  <PageContainer title="商品审核">
    <template #actions>
      <el-button type="success" @click="batchReview(true)" :disabled="!batchSelect.length">批量通过</el-button>
      <el-button type="danger" @click="batchReview(false)" :disabled="!batchSelect.length">批量驳回</el-button>
      <el-button @click="moderationStore.hydrateSection('productReviews')">刷新</el-button>
    </template>

    <ErrorAlert v-if="moderationStore.error" :message="moderationStore.error" />

    <el-table v-loading="moderationStore.loadingMap.productReviews" :data="rows" border @selection-change="(val) => batchSelect = val">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="product" label="商品名称" />
      <el-table-column prop="farmer" label="发布农户" width="140" />
      <el-table-column label="价格" width="100">
        <template #default="scope">¥{{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column prop="submittedAt" label="提交时间" width="170" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'approved' ? 'success' : scope.row.status === 'rejected' ? 'danger' : 'warning'">
            {{ scope.row.status === 'approved' ? '已通过' : scope.row.status === 'rejected' ? '已驳回' : '待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button text type="primary" @click="handleReview(scope.row, true)">通过</el-button>
          <el-button text type="danger" @click="handleReview(scope.row, false)">驳回</el-button>
        </template>
      </el-table-column>
      <template #empty><el-empty description="暂无待审商品" /></template>
    </el-table>

    <div v-if="pagination.total > pagination.pageSize" style="text-align: center; margin-top: 20px">
      <el-pagination :current-page="pagination.page" :page-size="pagination.pageSize" :total="pagination.total" layout="prev, pager, next" @current-change="handlePageChangeReviews" />
    </div>
  </PageContainer>
</template>
