<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductDetailApi, getProductEvaluationsApi, canReviewProductApi, createProductEvaluationApi, deleteProductEvaluationApi } from '@/api/modules/products'
import { mockProducts } from '@/mocks/shop'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()
const loading = ref(false)
const error = ref('')
const quantity = ref(1)
const adding = ref(false)
const buying = ref(false)
const product = ref(null)
const activeTab = ref('overview')

const evaluations = ref([])
const evalTotal = ref(0)
const evalPage = ref(1)
const avgRating = ref(0)
const evalCount = ref(0)
const canReview = ref(false)
const myEvalId = ref(null)
const evalSaving = ref(false)
const evalForm = ref({ rating: 0, content: '' })

const maxQuantity = computed(() => Math.max(product.value?.stock || 1, 1))

async function loadProduct() {
  loading.value = true
  error.value = ''
  try {
    const result = await getProductDetailApi(route.params.id)
    product.value = result.item || result.data || result
  } catch (err) {
    product.value = mockProducts.find((item) => String(item.id) === String(route.params.id)) || null
    error.value = err.message || '商品详情加载失败，当前显示演示数据'
  } finally {
    loading.value = false
  }
  if (product.value) saveRecentView(product.value)
}

function saveRecentView(p) {
  try {
    const KEY = 'ZHHS_RECENT_VIEWS'
    let list = JSON.parse(localStorage.getItem(KEY) || '[]')
    list = list.filter((v) => v.id !== p.id)
    list.unshift({ id: p.id, name: p.name, time: new Date().toLocaleString('zh-CN', { hour12: false }) })
    if (list.length > 10) list = list.slice(0, 10)
    localStorage.setItem(KEY, JSON.stringify(list))
  } catch {
    // localStorage unavailable - ignore
  }
}

async function handleAddToCart() {
  if (!product.value || product.value.stock <= 0) return false
  adding.value = true
  try {
    await cartStore.addItem(product.value, quantity.value)
    ElMessage.success('已加入购物车')
    return true
  } catch (err) {
    ElMessage.error(err?.message || '加入购物车失败')
    return false
  } finally {
    adding.value = false
  }
}

async function handleBuyNow() {
  buying.value = true
  const ok = await handleAddToCart()
  if (!ok) { buying.value = false; return }
  router.push('/checkout')
}

async function loadEvaluations() {
  try {
    const r = await getProductEvaluationsApi(route.params.id, { page: evalPage.value, pageSize: 5 })
    evaluations.value = r.items || []
    evalTotal.value = r.total || 0
    avgRating.value = r.avgRating || 0
    evalCount.value = r.count || 0
  } catch {
    // evaluations are optional - ignore load failures
  }
  if (authStore.isAuthenticated) {
    try {
      const r = await canReviewProductApi(route.params.id)
      canReview.value = r.canReview
    } catch {
      // canReview is optional - default to false
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
    await createProductEvaluationApi(route.params.id, { rating: evalForm.value.rating, content: evalForm.value.content })
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

onMounted(() => { loadProduct(); loadEvaluations() })
watch(() => route.params.id, () => { loadProduct(); loadEvaluations() })
</script>

<template>
  <div class="product-detail-page">
    <!-- 面包屑屑导航 -->
    <el-breadcrumb separator="/" style="margin-bottom: 20px">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/products' }">商品列表</el-breadcrumb-item>
      <el-breadcrumb-item>{{ product?.name || '商品详情' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-alert
      v-if="error"
      type="warning"
      show-icon
      :closable="false"
      :title="error"
      style="margin-bottom: 16px"
    />
    <el-skeleton v-if="loading" :rows="8" animated />
    <el-empty v-else-if="!product" description="商品不存在或已下架">
      <template #extra>
        <el-button @click="router.push('/products')">返回商品列表</el-button>
      </template>
    </el-empty>

    <div v-if="product" class="detail-body">
      <div class="detail-left">
        <div class="product-img-box">🌿</div>
      </div>
      <div class="detail-right">
        <h1 class="detail-name">{{ product.name }}</h1>
        <div class="detail-meta">
          <el-tag type="success">产地：{{ product.region }}</el-tag>
          <el-tag>分类：{{ product.category || '农产品' }}</el-tag>
          <el-tag v-if="product.farmer" type="info">农户：{{ product.farmer }}</el-tag>
        </div>
        <div class="detail-price">
          ￥<span>{{ Number(product.price).toFixed(2) }}</span>
        </div>
        <div class="detail-purchase">
          <div class="detail-stock">
            库存：
            <el-tag :type="product.stock > 0 ? 'success' : 'danger'">
              {{ product.stock > 0 ? `${product.stock}件` : '已售罄' }}
            </el-tag>
          </div>
          <div class="detail-qty">
            <span>数量：</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="maxQuantity"
              :disabled="product.stock <= 0"
            />
          </div>
          <div class="detail-actions">
            <el-button
              type="primary"
              size="large"
              :disabled="product.stock <= 0"
              :loading="buying"
              @click="handleBuyNow"
              >立即购买</el-button
            >
            <el-button
              size="large"
              :disabled="product.stock <= 0"
              :loading="adding"
              @click="handleAddToCart"
              >加入购物车</el-button
            >
          </div>
        </div>
      </div>
    </div>

    <el-tabs v-if="product" v-model="activeTab" class="product-tabs">
      <el-tab-pane label="商品概述" name="overview">
        <div class="tab-content">
          <p class="tab-summary">{{ product.summary }}</p>
          <div class="tab-tags">
            <el-tag type="success">产地：{{ product.region }}</el-tag>
            <el-tag>分类：{{ product.category || '农产品' }}</el-tag>
            <el-tag v-if="product.farmer" type="info">农户：{{ product.farmer }}</el-tag>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="商品详情" name="detail">
        <div class="tab-content">
          <p class="tab-desc">{{ product.description || '暂无详细介绍' }}</p>
        </div>
      </el-tab-pane>
      <el-tab-pane label="规格参数" name="spec">
        <div class="tab-content">
          <el-descriptions border :column="2">
            <el-descriptions-item label="产地">{{ product.region || '-' }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ product.category || '-' }}</el-descriptions-item>
            <el-descriptions-item label="规格">{{ product.spec || '-' }}</el-descriptions-item>
            <el-descriptions-item label="库存">{{ product.stock || 0 }} 件</el-descriptions-item>
            <el-descriptions-item label="农户">{{ product.farmer || '-' }}</el-descriptions-item>
            <el-descriptions-item label="资质">{{ product.qualification || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-tab-pane>
      <el-tab-pane :label="'商品评价 (' + evalCount + ')'" name="eval">
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
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.product-detail-page {
  padding-bottom: 32px;
}
.detail-body {
  display: flex;
  gap: 40px;
}
.detail-left {
  flex-shrink: 0;
}
.product-img-box {
  width: 380px;
  height: 320px;
  background: #e8f5e9;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 96px;
}
.detail-right {
  flex: 1;
  min-width: 0;
}
.detail-name {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 12px;
}
.detail-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.detail-price {
  font-size: 14px;
  color: #e53935;
  margin-bottom: 12px;
}
.detail-price span {
  font-size: 32px;
  font-weight: 700;
}
.detail-summary {
  color: #555;
  margin-bottom: 8px;
}
.detail-desc {
  color: #777;
  font-size: 14px;
}
.detail-purchase {
  background: #f9fafb;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 16px;
  margin-top: 20px;
}
.detail-stock {
  margin-bottom: 8px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.detail-qty {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: #555;
  font-size: 14px;
}
.detail-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}
@media (max-width: 700px) {
  .detail-body {
    flex-direction: column;
  }
  .product-img-box {
    width: 100%;
    height: 200px;
  }
}

.eval-section {
  max-width: 860px;
}
.eval-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
.eval-header h3 {
  margin: 0;
  font-size: 18px;
}

.product-tabs {
  margin-top: 24px;
}
.tab-content {
  padding: 16px 0;
  line-height: 1.8;
}
.tab-summary {
  color: #555;
  font-size: 15px;
  margin-bottom: 16px;
}
.tab-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.tab-desc {
  color: #333;
  font-size: 15px;
  white-space: pre-wrap;
}
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
