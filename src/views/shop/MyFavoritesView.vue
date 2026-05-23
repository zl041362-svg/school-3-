<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getFavoritesApi } from '@/api/modules/news'
import { toggleFavoriteApi } from '@/api/modules/news'
import EmptyState from '@/components/EmptyState.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import LoadingState from '@/components/LoadingState.vue'

const loading = ref(false)
const error = ref('')
const favorites = ref([])
const total = ref(0)
const page = ref(1)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const r = await getFavoritesApi({ page: page.value, pageSize: 10 })
    favorites.value = r.items || []
    total.value = r.total || 0
  } catch (err) {
    error.value = err?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function unfavorite(newsId) {
  try {
    await toggleFavoriteApi(newsId)
    ElMessage.success('已取消收藏')
    load()
  } catch (err) {
    ElMessage.error(err?.message || '操作失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="fav-page">
    <div class="page-head">
      <h1 class="page-title">我的收藏</h1>
      <p class="page-sub">收藏的资讯内容</p>
    </div>

    <ErrorAlert v-if="error" :message="error" />
    <LoadingState v-if="loading" :rows="4" />
    <EmptyState v-else-if="!favorites.length" description="暂无收藏">
      <template #extra>
        <el-button type="primary" @click="$router.push('/news')">去浏览资讯</el-button>
      </template>
    </EmptyState>

    <div v-else class="fav-list">
      <div v-for="f in favorites" :key="f.newsId" class="fav-card">
        <div class="fav-body">
          <div class="fav-title" @click="$router.push(`/news/${f.newsId}`)">{{ f.title }}</div>
          <div class="fav-meta">
            <span class="fav-cat">{{ f.category || '资讯' }}</span>
            <span class="meta-divider">·</span>
            <span>{{ f.createdAt || '' }}</span>
          </div>
        </div>
        <button class="unfav-btn" @click="unfavorite(f.newsId)">取消收藏</button>
      </div>
    </div>

    <div v-if="total > 10" class="pagination-wrap">
      <el-pagination
        :current-page="page" :page-size="10" :total="total"
        layout="prev, pager, next"
        @current-change="(p) => { page = p; load() }"
      />
    </div>
  </div>
</template>

<style scoped>
.fav-page {
  padding-bottom: 32px;
}
.page-head {
  margin-bottom: 24px;
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

.fav-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.fav-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  transition: all 0.3s var(--ease-smooth);
}
.fav-card:hover {
  box-shadow: var(--shadow-sm);
  border-color: var(--color-terracotta-soft);
}
.fav-body {
  flex: 1;
  min-width: 0;
}
.fav-title {
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  color: var(--color-soil);
  margin-bottom: 4px;
  transition: color 0.2s;
}
.fav-title:hover {
  color: var(--color-terracotta);
}
.fav-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-muted);
  font-size: 12px;
}
.fav-cat {
  padding: 1px 8px;
  border-radius: var(--radius-full);
  background: var(--color-terracotta-soft);
  color: var(--color-terracotta);
  font-size: 11px;
  font-weight: 600;
}
.meta-divider {
  color: var(--color-border);
}

.unfav-btn {
  padding: 5px 14px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--color-text-muted);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
  flex-shrink: 0;
}
.unfav-btn:hover {
  border-color: var(--color-berry);
  color: var(--color-berry);
}

.pagination-wrap {
  text-align: center;
  margin-top: 24px;
}
</style>
