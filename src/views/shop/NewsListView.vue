<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getNewsListApi } from '@/api/modules/news'
import { mockNews } from '@/mocks/shop'

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
    newsRows.value = result.items || result.list || result.data || []
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
    <div class="news-header">
      <h2>三农资讯</h2>
      <div class="category-bar">
        <el-tag
          v-for="cat in categories"
          :key="cat"
          :type="activeCategory === cat ? 'primary' : 'info'"
          :effect="activeCategory === cat ? 'dark' : 'plain'"
          class="cat-tag"
          @click="handleCategoryChange(cat)"
          >{{ cat }}</el-tag
        >
      </div>
    </div>

    <el-alert
      v-if="error"
      type="warning"
      show-icon
      :closable="false"
      :title="error"
      style="margin-bottom: 16px"
    />
    <el-skeleton v-if="loading" :rows="4" animated />

    <div v-else class="news-grid">
      <div
        v-for="item in newsRows"
        :key="item.id"
        class="news-card"
        @click="$router.push(`/news/${item.id}`)"
      >
        <div class="news-img">📰</div>
        <div class="news-body">
          <el-tag size="small" type="success">{{ item.category || '资讯' }}</el-tag>
          <div class="news-title">{{ item.title }}</div>
          <div class="news-meta">{{ item.author }} · {{ item.publishedAt || '-' }}</div>
          <div class="news-summary">{{ item.summary }}</div>
        </div>
      </div>
      <el-empty v-if="!newsRows.length" description="暂无资讯内容" />
    </div>

    <div v-if="total >= query.pageSize" style="text-align: center; margin-top: 24px">
      <el-pagination
        :current-page="query.page"
        :page-size="query.pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="
          (p) => {
            query.page = p
            loadNews()
          }
        "
      />
    </div>
  </div>
</template>

<style scoped>
.news-list-page {
  padding-bottom: 32px;
}
.news-header {
  margin-bottom: 20px;
}
.news-header h2 {
  font-size: 22px;
  color: var(--zhhs-primary, #2e7d32);
  margin: 0 0 12px;
}
.category-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.cat-tag {
  cursor: pointer;
}
.news-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.news-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e8f5e9;
  padding: 16px;
  cursor: pointer;
  display: flex;
  gap: 16px;
  transition: box-shadow 0.2s;
}
.news-card:hover {
  box-shadow: 0 4px 16px rgba(46, 125, 50, 0.1);
}
.news-img {
  width: 80px;
  height: 80px;
  background: #e8f5e9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  flex-shrink: 0;
}
.news-body {
  flex: 1;
  min-width: 0;
}
.news-title {
  font-size: 16px;
  font-weight: 600;
  margin: 6px 0 4px;
}
.news-meta {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}
.news-summary {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
