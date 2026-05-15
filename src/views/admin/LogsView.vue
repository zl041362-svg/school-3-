<script setup>
import { computed, onMounted, ref } from 'vue'
import PageContainer from '@/components/PageContainer.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.logs)
const pagination = computed(() => moderationStore.pagination('logs'))
const searchKeyword = ref('')

const filteredRows = computed(() => {
  if (!searchKeyword.value) return rows.value
  return rows.value.filter((l) =>
    (l.operator || '').includes(searchKeyword.value) ||
    (l.action || '').includes(searchKeyword.value) ||
    (l.detail || '').includes(searchKeyword.value)
  )
})

function handlePageChange(page) {
  moderationStore.hydrateSection('logs', { page })
}

onMounted(() => {
  moderationStore.hydrateSection('logs')
})
</script>

<template>
  <PageContainer title="操作日志">
    <template #actions>
      <el-button @click="moderationStore.hydrateSection('logs')">刷新</el-button>
    </template>

    <el-alert
      v-if="moderationStore.error"
      type="warning"
      show-icon
      :closable="false"
      :title="moderationStore.error"
      style="margin-bottom: 16px"
    />

    <div style="display: flex; gap: 12px; margin-bottom: 16px">
      <el-input v-model="searchKeyword" placeholder="搜索操作人/操作/详情" style="max-width: 300px" clearable />
    </div>

    <el-table v-loading="moderationStore.loadingMap?.logs" :data="filteredRows" border>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="operator" label="操作人" width="140" />
      <el-table-column prop="action" label="操作类型" width="160" />
      <el-table-column prop="detail" label="操作详情" />
      <el-table-column prop="createdAt" label="操作时间" width="200" />
      <template #empty>
        <el-empty description="暂无操作日志" />
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
  </PageContainer>
</template>
