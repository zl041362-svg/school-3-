<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductEvaluationsApi, canReviewProductApi, createProductEvaluationApi, deleteProductEvaluationApi } from '@/api/modules/products'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  productId: { type: [String, Number], required: true },
})

const authStore = useAuthStore()
const evaluations = ref([])
const evalTotal = ref(0)
const evalPage = ref(1)
const avgRating = ref(0)
const evalCount = ref(0)
const canReview = ref(false)
const myEvalId = ref(null)
const evalSaving = ref(false)
const evalForm = ref({ rating: 0, content: '' })

async function loadEvaluations() {
  try {
    const r = await getProductEvaluationsApi(props.productId, { page: evalPage.value, pageSize: 5 })
    evaluations.value = r.items || []
    evalTotal.value = r.total || 0
    avgRating.value = r.avgRating || 0
    evalCount.value = r.count || 0
  } catch {
    // evaluations are optional
  }
  if (authStore.isAuthenticated) {
    try {
      const r = await canReviewProductApi(props.productId)
      canReview.value = r.canReview
    } catch {
      // canReview is optional
    }
    if (evaluations.value.length > 0) {
      const mine = evaluations.value.find((e) => e.userId === authStore.user?.id)
      if (mine) myEvalId.value = mine.id
    }
  }
}

async function submitEval() {
  if (!evalForm.value.rating) { ElMessage.warning('请选择评分'); return }
  evalSaving.value = true
  try {
    await createProductEvaluationApi(props.productId, { rating: evalForm.value.rating, content: evalForm.value.content })
    ElMessage.success('评价提交成功')
    evalForm.value = { rating: 0, content: '' }
    evalPage.value = 1
    await loadEvaluations()
  } catch (err) {
    ElMessage.error(err?.message || '评价提交失败')
  } finally {
    evalSaving.value = false
  }
}

async function deleteEval(id) {
  try {
    await ElMessageBox.confirm('确认删除该评价？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteProductEvaluationApi(id)
    ElMessage.success('评价已删除')
    myEvalId.value = null
    await loadEvaluations()
  } catch (err) {
    ElMessage.error(err?.message || '删除失败')
  }
}

function handleEvalPageChange(page) {
  evalPage.value = page
  loadEvaluations()
}

defineExpose({ loadEvaluations })
</script>

<template>
  <div class="eval-summary">
    <el-rate v-model="avgRating" disabled show-score text-color="#ff9900" />
    <span class="eval-count">({{ evalCount }} 条)</span>
  </div>

  <el-card v-if="authStore.isAuthenticated && canReview && !myEvalId" class="eval-form-card">
    <template #header>写评价</template>
    <div class="eval-form">
      <div class="eval-form-rate">
        <span>评分：</span>
        <el-rate v-model="evalForm.rating" />
      </div>
      <el-input
        v-model="evalForm.content"
        type="textarea"
        :rows="3"
        placeholder="分享你的使用体验..."
        maxlength="500"
        show-word-limit
      />
      <el-button type="primary" :loading="evalSaving" style="margin-top: 12px" @click="submitEval">提交评价</el-button>
    </div>
  </el-card>

  <el-alert v-else-if="authStore.isAuthenticated && myEvalId" type="info" show-icon :closable="false" style="margin-bottom: 12px">
    <template #title>
      你已经评价过该商品
      <el-button text type="danger" size="small" style="margin-left: 8px" @click="deleteEval(myEvalId)">删除重评</el-button>
    </template>
  </el-alert>

  <el-empty v-if="!evaluations.length && evalCount === 0" description="暂无评价，快来抢沙发吧" />

  <div v-else class="eval-list">
    <div v-for="e in evaluations" :key="e.id" class="eval-item">
      <div class="eval-item-header">
        <span class="eval-user">{{ e.userName || '匿名用户' }}</span>
        <el-rate v-model="e.rating" disabled size="small" />
        <span class="eval-time">{{ e.createdAt || '' }}</span>
      </div>
      <p v-if="e.content" class="eval-content">{{ e.content }}</p>
      <el-button v-if="authStore.user?.id === e.userId" text type="danger" size="small" @click="deleteEval(e.id)">删除</el-button>
    </div>
  </div>

  <div v-if="evalTotal > 5" style="text-align: center; margin-top: 16px">
    <el-pagination
      :current-page="evalPage"
      :page-size="5"
      :total="evalTotal"
      layout="prev, pager, next"
      @current-change="handleEvalPageChange"
    />
  </div>
</template>

<style scoped>
.eval-summary {
  display: flex;
  align-items: center;
  gap: 8px;
}
.eval-count {
  color: #999;
  font-size: 13px;
}
.eval-form-card {
  margin-bottom: 16px;
}
.eval-form-rate {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.eval-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.eval-item {
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
}
.eval-item-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.eval-user {
  font-weight: 600;
  font-size: 14px;
}
.eval-time {
  color: #999;
  font-size: 12px;
  margin-left: auto;
}
.eval-content {
  margin: 8px 0 4px;
  color: #555;
  font-size: 14px;
  line-height: 1.6;
}
</style>
