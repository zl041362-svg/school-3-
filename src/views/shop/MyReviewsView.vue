<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyEvaluationsApi, deleteProductEvaluationApi } from '@/api/modules/products'
import EmptyState from '@/components/EmptyState.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import LoadingState from '@/components/LoadingState.vue'

const loading = ref(false)
const error = ref('')
const evaluations = ref([])
const total = ref(0)
const page = ref(1)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const r = await getMyEvaluationsApi({ page: page.value, pageSize: 10 })
    evaluations.value = r.items || []
    total.value = r.total || 0
  } catch (err) {
    error.value = err?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function deleteEval(id) {
  try { await ElMessageBox.confirm('确认删除该评价？', '提示', { type: 'warning' }) } catch { return }
  try {
    await deleteProductEvaluationApi(id)
    ElMessage.success('已删除')
    load()
  } catch (err) {
    ElMessage.error(err?.message || '删除失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="reviews-page">
    <div class="page-head">
      <h1 class="page-title">我的评价</h1>
      <p class="page-sub">已发布的商品评价</p>
    </div>

    <ErrorAlert v-if="error" :message="error" />
    <LoadingState v-if="loading" :rows="4" />
    <EmptyState v-else-if="!evaluations.length" description="暂无评价">
      <template #extra>
        <el-button type="primary" @click="$router.push('/products')">去选购</el-button>
      </template>
    </EmptyState>

    <div v-else class="review-list">
      <div v-for="e in evaluations" :key="e.id" class="review-card">
        <div class="review-head">
          <span class="review-product">{{ e.userName || '未知商品' }}</span>
          <el-rate v-model="e.rating" disabled size="small" />
        </div>
        <p v-if="e.content" class="review-content">{{ e.content }}</p>
        <div class="review-foot">
          <span class="review-time">{{ e.createdAt || '' }}</span>
          <button class="delete-btn" @click="deleteEval(e.id)">删除</button>
        </div>
      </div>
    </div>

    <div v-if="total > 10" class="pagination-wrap">
      <el-pagination
        :current-page="page" :page-size="10" :total="total"
        layout="prev, pager, next"
        @current-change="(p) => { page = p; load() }"
      />
    </div>
  </div>
</template>

<style scoped>
.reviews-page {
  padding-bottom: 32px;
}
.page-head {
  margin-bottom: 24px;
}
.page-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
  color: var(--color-soil);
}
.page-sub {
  margin: 4px 0 0;
  font-size: 14px;
  color: var(--color-text-muted);
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.review-card {
  padding: 18px 20px;
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  transition: all 0.3s var(--ease-smooth);
}
.review-card:hover {
  box-shadow: var(--shadow-sm);
}
.review-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.review-product {
  font-weight: 600;
  font-size: 15px;
  color: var(--color-soil);
}
.review-content {
  margin: 0 0 10px;
  color: var(--color-text-soft);
  font-size: 14px;
  line-height: 1.6;
}
.review-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.review-time {
  color: var(--color-text-muted);
  font-size: 12px;
}
.delete-btn {
  padding: 4px 12px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--color-text-muted);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.delete-btn:hover {
  border-color: var(--color-berry);
  color: var(--color-berry);
}

.pagination-wrap {
  text-align: center;
  margin-top: 24px;
}
</style>
