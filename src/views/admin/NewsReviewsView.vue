<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'
import { batchReviewNewsApi } from '@/api/modules/admin'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.newsReviews)
const pagination = computed(() => moderationStore.pagination('newsReviews'))
const batchSelect = ref([])

function handlePageChangeReviews(page) {
  moderationStore.hydrateSection('newsReviews', { page })
}

async function handleReview(row, approved) {
  let reason = ''
  if (!approved) {
    const result = await ElMessageBox.prompt('请输入驳回原因', '资讯驳回', {
      confirmButtonText: '提交', cancelButtonText: '取消', inputPlaceholder: '原因将同步给作者',
    }).catch(() => null)
    if (!result) return
    reason = result.value
  } else {
    try { await ElMessageBox.confirm('确认通过该资讯审核？', '审核通过', { type: 'warning', confirmButtonText: '通过', cancelButtonText: '取消' }) } catch { return }
  }
  await moderationStore.reviewNews(row.id, approved, reason)
  ElMessage.success(approved ? '资讯审核已通过' : '资讯审核已驳回')
  batchSelect.value = []
}

async function batchReview(approved) {
  if (!batchSelect.value.length) { ElMessage.warning('请先选择审核项'); return }
  let reason = ''
  if (!approved) {
    const result = await ElMessageBox.prompt('请输入批量驳回原因', '批量驳回', { confirmButtonText: '提交', cancelButtonText: '取消' }).catch(() => null)
    if (!result) return
    reason = result.value
  } else {
    try { await ElMessageBox.confirm(`确认批量通过所选 ${batchSelect.value.length} 条审核？`, '批量审核', { type: 'warning' }) } catch { return }
  }
  try {
    await batchReviewNewsApi({ ids: batchSelect.value.map((r) => r.id), approved, reason })
    ElMessage.success(approved ? '批量审核通过' : '批量驳回完成')
    batchSelect.value = []
    moderationStore.hydrateSection('newsReviews')
    moderationStore.hydrateSection('news')
  } catch (err) { ElMessage.error(err?.message || '批量审核失败') }
}

onMounted(() => {
  moderationStore.hydrateSection('newsReviews')
  moderationStore.hydrateSection('news')
})
</script>

<template>
  <PageContainer title="资讯审核">
    <template #actions>
      <el-button type="success" @click="batchReview(true)" :disabled="!batchSelect.length">批量通过</el-button>
      <el-button type="danger" @click="batchReview(false)" :disabled="!batchSelect.length">批量驳回</el-button>
      <el-button @click="moderationStore.hydrateSection('newsReviews')">刷新</el-button>
    </template>

    <el-alert v-if="moderationStore.error" type="warning" show-icon :closable="false" :title="moderationStore.error" style="margin-bottom: 16px" />

    <el-table v-loading="moderationStore.loadingMap.newsReviews" :data="rows" border @selection-change="(val) => batchSelect = val">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="author" label="作者" width="140" />
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
      <template #empty><el-empty description="暂无待审资讯" /></template>
    </el-table>

    <div v-if="pagination.total > pagination.pageSize" style="text-align: center; margin-top: 16px">
      <el-pagination :current-page="pagination.page" :page-size="pagination.pageSize" :total="pagination.total" layout="prev, pager, next" @current-change="handlePageChangeReviews" />
    </div>
  </PageContainer>
</template>
