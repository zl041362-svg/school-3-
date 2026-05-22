<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { getMyEvaluationsApi, deleteProductEvaluationApi } from '@/api/modules/products'

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
  <PageContainer title="我的评价">
    <el-alert v-if="error" type="warning" show-icon :closable="false" :title="error" style="margin-bottom: 16px" />
    <el-skeleton v-if="loading" :rows="4" animated />
    <el-empty v-else-if="!evaluations.length" description="暂无评价">
      <template #extra><el-button type="primary" @click="$router.push('/products')">去选购</el-button></template>
    </el-empty>
    <div v-else class="review-list">
      <div v-for="e in evaluations" :key="e.id" class="review-card">
        <div class="review-header">
          <span class="review-product">{{ e.userName || '未知商品' }}</span>
          <el-rate v-model="e.rating" disabled size="small" />
        </div>
        <p v-if="e.content" class="review-content">{{ e.content }}</p>
        <div class="review-footer">
          <span class="review-time">{{ e.createdAt || '' }}</span>
          <el-button text type="danger" size="small" @click="deleteEval(e.id)">删除</el-button>
        </div>
      </div>
    </div>
    <div v-if="total > 10" style="text-align: center; margin-top: 16px">
      <el-pagination :current-page="page" :page-size="10" :total="total" layout="prev, pager, next" @current-change="(p) => { page = p; load() }" />
    </div>
  </PageContainer>
</template>

<style scoped>
.review-list { display: flex; flex-direction: column; gap: 12px; }
.review-card { padding: 14px; border: 1px solid #eee; border-radius: 8px; }
.review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.review-product { font-weight: 600; font-size: 14px; }
.review-content { color: #555; font-size: 14px; line-height: 1.6; margin-bottom: 6px; }
.review-footer { display: flex; justify-content: space-between; align-items: center; }
.review-time { color: #999; font-size: 12px; }
</style>
