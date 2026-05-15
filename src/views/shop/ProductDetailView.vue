<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetailApi } from '@/api/modules/products'
import { mockProducts } from '@/mocks/shop'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const loading = ref(false)
const error = ref('')
const quantity = ref(1)
const adding = ref(false)
const buying = ref(false)
const product = ref(null)

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
}

async function handleAddToCart() {
  if (!product.value || product.value.stock <= 0) return
  adding.value = true
  try {
    await cartStore.addItem(product.value, quantity.value)
    ElMessage.success('已加入购物车')
  } catch (err) {
    ElMessage.error(err?.message || '加入购物车失败')
  } finally {
    adding.value = false
  }
}

async function handleBuyNow() {
  await handleAddToCart()
  if (adding.value) return
  router.push('/checkout')
}

watch(() => route.params.id, loadProduct)
onMounted(loadProduct)
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
        </div>
        <div class="detail-price">
          ￥<span>{{ product.price }}</span>
        </div>
        <p class="detail-summary">{{ product.summary }}</p>
        <p class="detail-desc">{{ product.description }}</p>

        <el-divider />

        <div class="detail-stock">
          库存：
          <el-tag :type="product.stock > 0 ? 'success' : 'danger'">
            {{ product.stock > 0 ? `${product.stock}件可以购买` : '已售罄' }}
          </el-tag>
        </div>
        <div class="detail-qty" style="margin-top: 16px">
          <span style="margin-right: 12px; color: #555">购买数量：</span>
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
.detail-stock {
  margin-bottom: 8px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
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
</style>
