<script setup>
import { computed, onMounted } from 'vue'
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
  <div class="dashboard">
    <div class="page-head">
      <div>
        <h1 class="page-title">控制台</h1>
        <p class="page-sub">管理后台概览</p>
      </div>
      <div class="head-actions">
        <el-button @click="refreshAll">刷新</el-button>
        <el-button type="primary" @click="$router.push('/admin/product-reviews')">待审商品</el-button>
      </div>
    </div>

    <el-alert
      v-if="moderationStore.error"
      type="warning" show-icon :closable="false"
      :title="moderationStore.error" style="margin-bottom: 20px"
    />

    <div class="stats-row">
      <div
        v-for="card in cards" :key="card.key"
        class="stat-card"
        @click="$router.push(card.path || '/admin')"
      >
        <div class="stat-value">{{ card.value }}</div>
        <div class="stat-label">{{ card.label }}</div>
      </div>
    </div>

    <div class="logs-card">
      <h3 class="card-title">最近操作日志</h3>
      <el-table v-loading="moderationStore.loadingMap?.logs" :data="recentLogs" border>
        <el-table-column prop="operator" label="操作人" width="140" />
        <el-table-column prop="action" label="操作类型" width="180" />
        <el-table-column prop="detail" label="详情" />
        <el-table-column prop="createdAt" label="时间" width="180" />
        <template #empty>
          <el-empty description="暂无操作日志" />
        </template>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  padding-bottom: 24px;
}
.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
}
.page-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 800;
  color: var(--color-soil);
}
.page-sub {
  margin: 4px 0 0;
  font-size: 14px;
  color: var(--color-text-muted);
}
.head-actions {
  display: flex;
  gap: 8px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}
.stat-card {
  text-align: center;
  padding: 28px 20px;
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.35s var(--ease-smooth);
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-terracotta-soft);
}
.stat-value {
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 900;
  color: var(--color-terracotta);
}
.stat-label {
  font-size: 14px;
  color: var(--color-text-muted);
  margin-top: 6px;
}

.logs-card {
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: 24px;
}
.card-title {
  margin: 0 0 16px;
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--color-soil);
}

@media (max-width: 860px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 560px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
}
</style>
