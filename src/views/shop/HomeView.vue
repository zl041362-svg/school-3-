<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getProductsApi } from '@/api/modules/products'
import { getNewsListApi } from '@/api/modules/news'
import { mockNews, mockProducts } from '@/mocks/shop'
import HomeBanner from '@/components/shop/HomeBanner.vue'
import HomeProductGrid from '@/components/shop/HomeProductGrid.vue'
import HomeNewsSection from '@/components/shop/HomeNewsSection.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'

const router = useRouter()
const loading = ref(false)
const newsLoading = ref(false)
const error = ref('')
const featuredProducts = ref([])
const latestNews = ref([])
const searchKeyword = ref('')

const categories = [
  { emoji: '🌾', label: '粮油', value: '粮油' },
  { emoji: '🍎', label: '水果', value: '水果' },
  { emoji: '🥦', label: '蔬菜', value: '蔬菜' },
  { emoji: '🍵', label: '茶饮', value: '茶饮' },
  { emoji: '🥩', label: '肉禽蛋', value: '肉禽蛋' },
  { emoji: '🐟', label: '水产', value: '水产' },
]

const productCards = computed(() => featuredProducts.value.slice(0, 8))
const newsCards = computed(() => latestNews.value.slice(0, 4))

function handleSearch() {
  const kw = searchKeyword.value.trim()
  if (kw) {
    router.push(`/products?keyword=${encodeURIComponent(kw)}`)
  }
}

async function loadHomeData() {
  loading.value = true
  newsLoading.value = true
  try {
    const [productResult, newsResult] = await Promise.all([
      getProductsApi({ page: 1, pageSize: 8 }),
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

onMounted(loadHomeData)
</script>

<template>
  <div class="home-page">
    <ErrorAlert v-if="error" :message="error" />

    <HomeBanner />

    <!-- 搜索区 -->
    <div class="search-section">
      <div class="search-input-wrap">
        <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          placeholder="搜索农产品、产地、农户..."
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>
    </div>

    <!-- 商品分类 -->
    <section class="section">
      <div class="section-head">
        <h2 class="section-title">商品分类</h2>
      </div>
      <div class="category-grid">
        <div
          v-for="cat in categories"
          :key="cat.value"
          class="category-card"
          @click="router.push(`/products?category=${cat.value}`)"
        >
          <span class="category-emoji">{{ cat.emoji }}</span>
          <span class="category-label">{{ cat.label }}</span>
        </div>
      </div>
    </section>

    <HomeProductGrid :products="productCards" :loading="loading" />
    <HomeNewsSection :news="newsCards" :loading="newsLoading" />
  </div>
</template>

<style scoped>
.home-page {
  padding-bottom: 24px;
}

/* ── 搜索 ── */
.search-section {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}
.search-input-wrap {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 640px;
  height: 42px;
  border: 2px solid var(--color-primary);
  background: var(--color-surface);
}
.search-icon {
  margin: 0 10px;
  flex-shrink: 0;
  color: var(--color-text-hint);
}
.search-input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  font-size: 13px;
  color: var(--color-text);
  background: transparent;
}
.search-input::placeholder {
  color: var(--color-text-disabled);
}
.search-btn {
  height: 100%;
  padding: 0 20px;
  border: none;
  background: var(--color-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}
.search-btn:hover {
  background: var(--color-primary-hover);
}

/* ── 分类 ── */
.section {
  margin-bottom: 24px;
}
.section-head {
  margin-bottom: 10px;
}
.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
}
.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 8px;
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: border-color 0.15s;
}
.category-card:hover {
  border-color: var(--color-primary);
}
.category-emoji {
  font-size: 22px;
}
.category-label {
  font-size: 12px;
  color: var(--color-text-secondary);
}

@media (max-width: 960px) {
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 560px) {
  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
