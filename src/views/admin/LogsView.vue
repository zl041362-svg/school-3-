<script setup>
import { computed, onMounted, ref } from 'vue'
import PageContainer from '@/components/PageContainer.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.logs)
const pagination = computed(() => moderationStore.pagination('logs'))
const searchKeyword = ref('')
const filterAction = ref('')
const dateRange = ref([])

const actionOptions = [
  'review_farmer_verification', 'review_product', 'review_news',
  'update_user_status', 'update_role_members', 'update_product',
  'update_product_status', 'update_news', 'update_news_status',
  'create_permission', 'update_permission', 'delete_permission',
  'create_role', 'delete_role', 'update_role',
  'batch_review_products', 'batch_review_news', 'batch_review_farmers',
]

const filteredRows = computed(() => {
  let list = rows.value
  if (searchKeyword.value) {
    list = list.filter((l) =>
      (l.operator || '').includes(searchKeyword.value) ||
      (l.action || '').includes(searchKeyword.value) ||
      (l.detail || '').includes(searchKeyword.value)
    )
  }
  if (filterAction.value) {
    list = list.filter((l) => l.action === filterAction.value)
  }
  if (dateRange.value && dateRange.value.length === 2) {
    const [start, end] = dateRange.value
    const startStr = typeof start === 'string' ? start : start.toISOString().slice(0, 10)
    const endStr = typeof end === 'string' ? end : end.toISOString().slice(0, 10)
    list = list.filter((l) => {
      const d = (l.createdAt || '').slice(0, 10)
      return d >= startStr && d <= endStr
    })
  }
  return list
})

function handlePageChange(page) {
  moderationStore.hydrateSection('logs', { page })
}

function refreshWithFilters() {
  moderationStore.hydrateSection('logs')
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

    <ErrorAlert v-if="moderationStore.error" :message="moderationStore.error" />

    <div class="filter-bar">
      <el-input v-model="searchKeyword" placeholder="搜索操作人/操作/详情" style="max-width: 260px" clearable />
      <el-select v-model="filterAction" placeholder="操作类型" style="width: 200px" clearable>
        <el-option v-for="a in actionOptions" :key="a" :label="a" :value="a" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 260px" />
      <el-button @click="refreshWithFilters">筛选</el-button>
    </div>

    <el-table v-loading="moderationStore.loadingMap?.logs" :data="filteredRows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="operator" label="操作人" width="130" />
      <el-table-column prop="action" label="操作类型" width="200" />
      <el-table-column prop="detail" label="操作详情" />
      <el-table-column prop="createdAt" label="操作时间" width="180" />
      <template #empty>
        <el-empty description="暂无操作日志" />
      </template>
    </el-table>

    <div v-if="pagination.total > pagination.pageSize" style="text-align: center; margin-top: 20px">
      <el-pagination
        :current-page="pagination.page" :page-size="pagination.pageSize"
        :total="pagination.total" layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </PageContainer>
</template>

<style scoped>
.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
}
</style>
