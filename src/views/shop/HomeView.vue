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

const categories = [
  { emoji: '🌾', label: '粮油', value: '粮油' },
  { emoji: '🍎', label: '水果', value: '水果' },
  { emoji: '🥦', label: '蔬菜', value: '蔬菜' },
  { emoji: '🍵', label: '茶饮', value: '茶饮' },
  { emoji: '🥩', label: '肉禽蛋', value: '肉禽蛋' },
  { emoji: '🐟', label: '水产', value: '水产' },
]

const productCards = computed(() => featuredProducts.value.slice(0, 6))
const newsCards = computed(() => latestNews.value.slice(0, 4))

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

onMounted(loadHomeData)
</script>

<template>
  <div class="home-page">
    <ErrorAlert v-if="error" :message="error" />

    <HomeBanner />

    <!-- 商品分类 -->
    <section class="section">
      <div class="section-head">
        <div>
          <h2 class="section-title">商品分类</h2>
          <p class="section-desc">六大地标品类，应季而食</p>
        </div>
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
  padding-bottom: 32px;
}

.section {
  margin-bottom: 48px;
}
.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24px;
}
.section-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
  color: var(--color-soil);
}
.section-desc {
  margin: 4px 0 0;
  font-size: 14px;
  color: var(--color-text-muted);
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}
.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 12px;
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.35s var(--ease-smooth);
}
.category-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-terracotta-soft);
}
.category-emoji {
  font-size: 36px;
  transition: transform 0.35s var(--ease-spring);
}
.category-card:hover .category-emoji {
  transform: scale(1.15);
}
.category-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-soil);
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
