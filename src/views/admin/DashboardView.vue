<script setup>
import { computed, onMounted } from 'vue'
import PageContainer from '@/components/PageContainer.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'

const moderationStore = useAdminModerationStore()
const cards = computed(() => moderationStore.dashboardCards)
const recentLogs = computed(() => moderationStore.logs.slice(0, 5))

function refreshAll() {
  moderationStore.hydrateSection('productReviews')
  moderationStore.hydrateSection('newsReviews')
  moderationStore.hydrateSection('farmerVerifications')
  moderationStore.hydrateSection('users')
  moderationStore.hydrateSection('logs')
}

onMounted(refreshAll)
</script>

<template>
  <PageContainer title="控制台">
    <template #actions>
      <el-button @click="refreshAll">刷新</el-button>
      <el-button type="primary" @click="$router.push('/admin/product-reviews')">待审商品</el-button>
    </template>

    <el-alert
      v-if="moderationStore.error"
      type="warning"
      show-icon
      :closable="false"
      :title="moderationStore.error"
      style="margin-bottom: 16px"
    />

    <el-row :gutter="16" style="margin-bottom: 20px">
      <el-col v-for="card in cards" :key="card.key" :md="8" :sm="24" style="margin-bottom: 12px">
        <el-card
          class="stat-card"
          shadow="hover"
          style="cursor: pointer"
          @click="$router.push(card.path || '/admin')"
        >
          <div style="font-size: 28px; font-weight: 700; color: #2e7d32">{{ card.value }}</div>
          <div style="font-size: 14px; color: #666; margin-top: 6px">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>最近操作日志</template>
      <el-table v-loading="moderationStore.loadingMap?.logs" :data="recentLogs" border>
        <el-table-column prop="operator" label="操作人" width="140" />
        <el-table-column prop="action" label="操作类型" width="160" />
        <el-table-column prop="detail" label="详情" />
        <el-table-column prop="createdAt" label="时间" width="180" />
        <template #empty>
          <el-empty description="暂无操作日志" />
        </template>
      </el-table>
    </el-card>
  </PageContainer>
</template>
