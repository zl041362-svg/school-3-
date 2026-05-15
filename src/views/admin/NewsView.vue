<script setup>
import { computed, onMounted, ref } from 'vue'
import PageContainer from '@/components/PageContainer.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.news)
const pagination = computed(() => moderationStore.pagination('news'))
const searchKeyword = ref('')

const filteredRows = computed(() => {
  if (!searchKeyword.value) return rows.value
  return rows.value.filter((n) => (n.title || '').includes(searchKeyword.value) || (n.author || '').includes(searchKeyword.value))
})

function handlePageChange(page) {
  moderationStore.hydrateSection('news', { page })
}

onMounted(() => {
  moderationStore.hydrateSection('news')
})
</script>

<template>
  <PageContainer title="资讯管理">
    <template #actions>
      <el-button @click="moderationStore.hydrateSection('news')">刷新</el-button>
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
      <el-input v-model="searchKeyword" placeholder="搜索标题或作者" style="max-width: 260px" clearable />
    </div>

    <el-table v-loading="moderationStore.loadingMap?.news" :data="filteredRows" border>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="author" label="作者" width="160" />
      <el-table-column prop="publishedAt" label="发布时间" width="180" />
      <el-table-column label="状态" width="120">
        <template #default="scope">
          <el-tag
            :type="
              scope.row.status === 'published'
                ? 'success'
                : scope.row.status === 'rejected'
                  ? 'danger'
                  : 'warning'
            "
            >{{
              {
                draft: '草稿',
                pending: '待审核',
                published: '已发布',
                rejected: '已驳回',
                offline: '已下架',
              }[scope.row.status] || scope.row.status
            }}</el-tag
          >
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无资讯数据" />
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
