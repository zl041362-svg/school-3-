<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetailApi } from '@/api/modules/products'
import { mockProducts } from '@/mocks/shop'
import { useCartStore } from '@/stores/cart'
import { readJsonStorage, writeJsonStorage } from '@/utils/storage'
import ErrorAlert from '@/components/ErrorAlert.vue'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'
import ProductEvaluations from '@/components/shop/ProductEvaluations.vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const loading = ref(false)
const error = ref('')
const quantity = ref(1)
const adding = ref(false)
const buying = ref(false)
const product = ref(null)
const activeTab = ref('overview')
const evalRef = ref(null)

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
    let list = readJsonStorage(KEY, [])
    list = list.filter((v) => v.id !== p.id)
    list.unshift({ id: p.id, name: p.name, time: new Date().toLocaleString('zh-CN', { hour12: false }) })
    if (list.length > 10) list = list.slice(0, 10)
    writeJsonStorage(KEY, list)
  } catch {
    // localStorage unavailable
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

function handleTabChange(tab) {
  if (tab === 'eval') {
    evalRef.value?.loadEvaluations()
  }
}

onMounted(loadProduct)
watch(() => route.params.id, loadProduct)
</script>

<template>
  <div class="product-detail-page">
    <el-breadcrumb separator="/" style="margin-bottom: 20px">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/products' }">商品列表</el-breadcrumb-item>
      <el-breadcrumb-item>{{ product?.name || '商品详情' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <ErrorAlert :message="error" />
    <LoadingState v-if="loading" :rows="8" />
    <EmptyState v-else-if="!product" description="商品不存在或已下架">
      <template #extra>
        <el-button @click="router.push('/products')">返回商品列表</el-button>
      </template>
    </EmptyState>

    <div v-if="product" class="detail-body">
      <div class="detail-left">
        <div class="product-img-box">
          <span class="product-emoji">{{ product.category === '水果' ? '🍎' : product.category === '蔬菜' ? '🥦' : product.category === '粮油' ? '🌾' : product.category === '茶饮' ? '🍵' : product.category === '肉禽蛋' ? '🥩' : product.category === '水产' ? '🐟' : '🌿' }}</span>
        </div>
      </div>
      <div class="detail-right">
        <h1 class="detail-name">{{ product.name }}</h1>
        <div class="detail-meta">
          <span class="meta-tag">产地：{{ product.region }}</span>
          <span class="meta-tag">分类：{{ product.category || '农产品' }}</span>
          <span v-if="product.farmer" class="meta-tag">农户：{{ product.farmer }}</span>
        </div>
        <div class="detail-price">
          <span class="price-unit">¥</span>
          <span class="price-num">{{ Number(product.price).toFixed(2) }}</span>
        </div>
        <div class="detail-purchase">
          <div class="purchase-row">
            <span class="purchase-label">库存</span>
            <el-tag :type="product.stock > 0 ? 'success' : 'danger'">
              {{ product.stock > 0 ? `${product.stock} 件` : '已售罄' }}
            </el-tag>
          </div>
          <div class="purchase-row">
            <span class="purchase-label">数量</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="maxQuantity"
              :disabled="product.stock <= 0"
            />
          </div>
          <div class="purchase-actions">
            <button
              class="btn-buy"
              :disabled="product.stock <= 0 || buying"
              @click="handleBuyNow"
            >
              <span v-if="buying" class="btn-spinner"></span>
              <span v-else>立即购买</span>
            </button>
            <button
              class="btn-cart"
              :disabled="product.stock <= 0 || adding"
              @click="handleAddToCart"
            >
              <span v-if="adding" class="btn-spinner"></span>
              <span v-else>加入购物车</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <el-tabs v-if="product" v-model="activeTab" class="product-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="商品概述" name="overview">
        <div class="tab-content">
          <p class="tab-summary">{{ product.summary }}</p>
          <div class="tab-tags">
            <span class="meta-tag">产地：{{ product.region }}</span>
            <span class="meta-tag">分类：{{ product.category || '农产品' }}</span>
            <span v-if="product.farmer" class="meta-tag">农户：{{ product.farmer }}</span>
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
      <el-tab-pane :label="'商品评价'" name="eval">
        <ProductEvaluations ref="evalRef" :product-id="route.params.id" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.product-detail-page {
  padding-bottom: 24px;
  background: var(--color-surface);
  padding: 24px;
}

.detail-body {
  display: flex;
  gap: 40px;
  margin-bottom: 0;
}
.detail-left {
  flex-shrink: 0;
}
.product-img-box {
  width: 360px;
  height: 300px;
  background: #F7F7F7;
  display: flex;
  align-items: center;
  justify-content: center;
}
.product-emoji {
  font-size: 96px;
}

.detail-right {
  flex: 1;
  min-width: 0;
}
.detail-name {
  margin: 0 0 10px;
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text);
}
.detail-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.meta-tag {
  padding: 2px 10px;
  background: #F5F5F5;
  color: var(--color-text-hint);
  font-size: 12px;
}

.detail-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 20px;
  background: #FFF5F5;
  padding: 12px 16px;
}
.price-unit {
  font-size: 16px;
  font-weight: 600;
  color: #E4393C;
}
.price-num {
  font-size: 36px;
  font-weight: 700;
  color: #E4393C;
}

.detail-purchase {
  padding: 0;
}
.purchase-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.purchase-label {
  font-size: 13px;
  color: var(--color-text-hint);
  min-width: 36px;
}
.purchase-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}
.btn-buy {
  flex: 1;
  padding: 11px 0;
  border: none;
  background: #E4393C;
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-buy:hover { background: #C93538; }
.btn-buy:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-cart {
  flex: 1;
  padding: 11px 0;
  border: 1px solid #E4393C;
  background: transparent;
  color: #E4393C;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.btn-cart:hover {
  background: #FFF5F5;
}
.btn-cart:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.product-tabs {
  margin-top: 24px;
}
.tab-content {
  padding: 16px 0;
  line-height: 1.8;
}
.tab-summary {
  color: var(--color-text-secondary);
  font-size: 14px;
  margin-bottom: 12px;
}
.tab-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.tab-desc {
  color: var(--color-text);
  font-size: 14px;
  white-space: pre-wrap;
}

@media (max-width: 760px) {
  .detail-body {
    flex-direction: column;
    gap: 20px;
  }
  .product-img-box {
    width: 100%;
    height: 220px;
  }
  .detail-name { font-size: 18px; }
  .price-num { font-size: 28px; }
}
</style>
