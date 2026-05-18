<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductsApi } from '@/api/modules/products'
import { getNewsListApi } from '@/api/modules/news'
import { mockNews, mockProducts } from '@/mocks/shop'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const loading = ref(false)
const addingId = ref(null)
const featuredProducts = ref([])
const latestNews = ref([])

const categories = [
  { icon: '🌾', label: '粮油', value: '粮油' },
  { icon: '🍎', label: '水果', value: '水果' },
  { icon: '🥦', label: '蔬菜', value: '蔬菜' },
  { icon: '🍵', label: '茶饮', value: '茶饮' },
  { icon: '🥩', label: '肉禽蛋', value: '肉禽蛋' },
  { icon: '🐟', label: '水产', value: '水产' },
]

const newsCategories = [
  { label: '政策解读', value: 'policy' },
  { label: '种植技术', value: 'tech' },
  { label: '产业动态', value: 'industry' },
  { label: '市场行情', value: 'market' },
]

const productCards = computed(() => featuredProducts.value.slice(0, 6))
const newsCards = computed(() => latestNews.value.slice(0, 4))
const error = ref('')
const newsLoading = ref(false)

async function loadHomeData() {
  loading.value = true
  newsLoading.value = true
  try {
    const [productResult, newsResult] = await Promise.all([
      getProductsApi({ page: 1, pageSize: 6 }),
      getNewsListApi({ page: 1, pageSize: 4 }),
    ])
    featuredProducts.value = productResult.items || productResult.list || productResult.data || []
    latestNews.value = newsResult.items || newsResult.list || newsResult.data || []
  } catch {
    error.value = '数据加载失败，以下为演示数据'
    featuredProducts.value = mockProducts
    latestNews.value = mockNews
  } finally {
    loading.value = false
    newsLoading.value = false
  }
}

async function handleAddToCart(item) {
  if (!item || item.stock <= 0) {
    ElMessage.warning('当前商品缺货，暂时无法加入购物车')
    return
  }
  addingId.value = item.id
  try {
    await cartStore.addItem(item, 1)
    ElMessage.success(`${item.name} 已加入购物车`)
  } catch (error) {
    ElMessage.error(error?.message || cartStore.error || '加入购物车失败，请稍后重试。')
  } finally {
    addingId.value = null
  }
}

onMounted(loadHomeData)
</script>

<template>
  <div class="home-page">
    <el-alert
      v-if="error"
      type="warning"
      show-icon
      :closable="false"
      :title="error"
      style="margin-bottom: 16px"
    />

    <!-- Banner 区域 -->
    <div class="home-banner">
      <div class="banner-content">
        <div class="banner-text">
          <h1>智慧三农平台</h1>
          <p>产地直供 · 绿色安心 · 农产品上行新通道</p>
          <el-space>
            <el-button type="primary" size="large" @click="router.push('/products')"
              >立即选购</el-button
            >
            <el-button size="large" plain @click="router.push('/auth/register')"
              >入驻平台</el-button
            >
          </el-space>
        </div>
        <div class="banner-emoji">🌾🥦🍎</div>
      </div>
    </div>

    <!-- 商品分类入口 -->
    <section class="home-section">
      <div class="section-header">
        <h2>商品分类</h2>
        <el-link type="primary" @click="router.push('/products')">查看全部 →</el-link>
      </div>
      <div class="category-grid">
        <div
          v-for="cat in categories"
          :key="cat.value"
          class="category-card"
          @click="router.push(`/products?category=${cat.value}`)"
        >
          <span class="category-icon">{{ cat.icon }}</span>
          <span class="category-label">{{ cat.label }}</span>
        </div>
      </div>
    </section>

    <!-- 热销商品推荐 -->
    <section class="home-section">
      <div class="section-header">
        <h2>热销推荐</h2>
        <el-link type="primary" @click="router.push('/products')">查看更多 →</el-link>
      </div>
      <el-skeleton v-if="loading" :rows="3" animated />
      <div v-else class="product-grid">
        <div
          v-for="item in productCards"
          :key="item.id"
          class="product-card"
          @click="router.push(`/products/${item.id}`)"
        >
          <div class="product-img">🌿</div>
          <div class="product-info">
            <div class="product-name">{{ item.name }}</div>
            <div class="product-meta">{{ item.region }} · {{ item.category || '农产品' }}</div>
            <div class="product-summary">{{ item.summary }}</div>
            <div class="product-footer">
              <span class="product-price">￥{{ item.price }}</span>
              <el-tag v-if="item.stock <= 0" type="danger" size="small">已售罄</el-tag>
              <el-button
                v-else
                size="small"
                type="primary"
                plain
                :loading="addingId === item.id"
                @click.stop="handleAddToCart(item)"
                >加入购物车</el-button
              >
            </div>
          </div>
        </div>
        <el-empty v-if="!productCards.length" description="暂无推荐商品" />
      </div>
    </section>

    <!-- 三农资讯 -->
    <section class="home-section">
      <div class="section-header">
        <h2>三农资讯</h2>
        <el-link type="primary" @click="router.push('/news')">查看更多 →</el-link>
      </div>
      <div class="news-categories">
        <el-tag
          v-for="nc in newsCategories"
          :key="nc.value"
          class="news-cat-tag"
          @click="router.push('/news')"
          >{{ nc.label }}</el-tag
        >
      </div>
      <div class="news-list">
        <el-skeleton v-if="newsLoading" :rows="2" animated />
        <template v-else-if="newsCards.length">
          <div
            v-for="item in newsCards"
            :key="item.id"
            class="news-card"
            @click="router.push(`/news/${item.id}`)"
          >
            <div class="news-tag">资讯</div>
            <div class="news-body">
              <div class="news-title">{{ item.title }}</div>
              <div class="news-meta">{{ item.author }} · {{ item.publishedAt || '-' }}</div>
              <div class="news-summary">{{ item.summary }}</div>
            </div>
          </div>
        </template>
        <el-empty v-else description="暂无资讯" />
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  padding-bottom: 32px;
}

/* Banner */
.home-banner {
  background: linear-gradient(135deg, var(--zhhs-primary, #2e7d32) 0%, #43a047 100%);
  border-radius: 12px;
  padding: 56px 48px;
  margin-bottom: 32px;
  color: #fff;
}
.banner-content {
  max-width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.banner-text h1 {
  font-size: 36px;
  margin: 0 0 12px;
  font-weight: 700;
}
.banner-text p {
  font-size: 16px;
  margin: 0 0 24px;
  opacity: 0.9;
}
.banner-emoji {
  font-size: 72px;
  line-height: 1;
}

/* Section */
.home-section {
  margin-bottom: 32px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-header h2 {
  margin: 0;
  font-size: 20px;
  color: var(--zhhs-primary, #2e7d32);
}

/* Category */
.category-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}
.category-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px 8px;
  text-align: center;
  cursor: pointer;
  border: 1px solid #e8f5e9;
  transition:
    box-shadow 0.2s,
    transform 0.2s;
}
.category-card:hover {
  box-shadow: 0 4px 16px rgba(46, 125, 50, 0.12);
  transform: translateY(-2px);
}
.category-icon {
  display: block;
  font-size: 32px;
  margin-bottom: 8px;
}
.category-label {
  font-size: 13px;
  color: #555;
}

/* Product grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.product-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e8f5e9;
  overflow: hidden;
  cursor: pointer;
  transition:
    box-shadow 0.2s,
    transform 0.2s;
}
.product-card:hover {
  box-shadow: 0 4px 20px rgba(46, 125, 50, 0.12);
  transform: translateY(-2px);
}
.product-img {
  background: #e8f5e9;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
}
.product-info {
  padding: 14px;
}
.product-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}
.product-meta {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}
.product-summary {
  font-size: 12px;
  color: #666;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.product-price {
  font-size: 18px;
  font-weight: 700;
  color: #e53935;
}

/* News */
.news-categories {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.news-cat-tag {
  cursor: pointer;
}
.news-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}
.news-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e8f5e9;
  padding: 16px;
  cursor: pointer;
  display: flex;
  gap: 12px;
  transition: box-shadow 0.2s;
}
.news-card:hover {
  box-shadow: 0 4px 16px rgba(46, 125, 50, 0.1);
}
.news-tag {
  background: #e8f5e9;
  color: var(--zhhs-primary, #2e7d32);
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 12px;
  height: fit-content;
  white-space: nowrap;
}
.news-body {
  flex: 1;
  min-width: 0;
}
.news-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}
.news-meta {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}
.news-summary {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

@media (max-width: 900px) {
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .news-list {
    grid-template-columns: 1fr;
  }
  .banner-emoji {
    display: none;
  }
}
</style>
