<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getNewsListApi } from '@/api/modules/news'
import { mockNews } from '@/mocks/shop'
import { resolveItems } from '@/utils/apiResponse'
import EmptyState from '@/components/EmptyState.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import LoadingState from '@/components/LoadingState.vue'

const loading = ref(false)
const error = ref('')
const newsRows = ref([])
const total = ref(0)
const activeCategory = ref('全部')

const categories = ['全部', '政策解读', '种植技术', '产业动态', '市场行情']

const query = reactive({ category: '', keyword: '', page: 1, pageSize: 10 })

async function loadNews() {
  loading.value = true
  error.value = ''
  try {
    const result = await getNewsListApi(query)
    newsRows.value = resolveItems(result)
    total.value = result.total || newsRows.value.length
  } catch (err) {
    newsRows.value = mockNews
    total.value = mockNews.length
    error.value = err.message || '资讯列表加载失败，当前显示演示数据'
  } finally {
    loading.value = false
  }
}

function handleCategoryChange(cat) {
  activeCategory.value = cat
  query.category = cat === '全部' ? '' : cat
  query.page = 1
  loadNews()
}

onMounted(loadNews)
</script>

<template>
  <div class="news-list-page">
    <div class="page-head">
      <h1 class="page-title">三农资讯</h1>
      <p class="page-sub">政策 · 技术 · 行情 · 乡间故事</p>
    </div>

    <div class="category-bar">
      <button
        v-for="cat in categories"
        :key="cat"
        class="cat-btn"
        :class="{ active: activeCategory === cat }"
        @click="handleCategoryChange(cat)"
      >{{ cat }}</button>
    </div>

    <ErrorAlert v-if="error" :message="error" />
    <LoadingState v-if="loading" :rows="4" />

    <template v-else>
      <div v-if="newsRows.length" class="news-grid">
        <article
          v-for="(item, idx) in newsRows"
          :key="item.id"
          class="news-card"
          :style="{ animationDelay: idx * 0.08 + 's' }"
          @click="$router.push(`/news/${item.id}`)"
        >
          <div class="news-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
              <line x1="8" y1="7" x2="16" y2="7"/>
              <line x1="8" y1="11" x2="14" y2="11"/>
            </svg>
          </div>
          <div class="news-body">
            <div class="news-head">
              <span class="news-category">{{ item.category || '资讯' }}</span>
              <h3 class="news-title">{{ item.title }}</h3>
            </div>
            <p class="news-summary">{{ item.summary }}</p>
            <div class="news-meta">
              <span>{{ item.author }}</span>
              <span class="meta-divider">·</span>
              <span>{{ item.publishedAt || '-' }}</span>
            </div>
          </div>
        </article>
      </div>
      <EmptyState v-else description="暂无资讯内容" />
    </template>

    <div v-if="total >= query.pageSize" class="pagination-wrap">
      <el-pagination
        :current-page="query.page"
        :page-size="query.pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="(p) => { query.page = p; loadNews() }"
      />
    </div>
  </div>
</template>

<style scoped>
.news-list-page {
  padding-bottom: 32px;
}
.page-head {
  margin-bottom: 20px;
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

.category-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}
.cat-btn {
  padding: 7px 20px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--color-border);
  background: var(--color-paper-white);
  color: var(--color-text-soft);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.cat-btn:hover {
  border-color: var(--color-terracotta-soft);
  color: var(--color-terracotta);
}
.cat-btn.active {
  background: var(--color-terracotta);
  border-color: var(--color-terracotta);
  color: #fff;
  font-weight: 600;
}

.news-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.news-card {
  display: flex;
  gap: 18px;
  padding: 20px;
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.35s var(--ease-smooth);
  animation: fadeUp 0.5s var(--ease-out) both;
}
.news-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-terracotta-soft);
}
.news-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--color-cream-dark), var(--color-paper));
  color: var(--color-terracotta);
}
.news-body {
  flex: 1;
  min-width: 0;
}
.news-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.news-category {
  padding: 2px 10px;
  border-radius: var(--radius-full);
  background: var(--color-terracotta-soft);
  color: var(--color-terracotta);
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}
.news-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 700;
  color: var(--color-soil);
}
.news-summary {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--color-text-soft);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.news-meta {
  font-size: 12px;
  color: var(--color-text-muted);
}
.meta-divider {
  margin: 0 6px;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.pagination-wrap {
  text-align: center;
  margin-top: 32px;
}
</style>
