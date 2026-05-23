<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNewsDetailApi, toggleFavoriteApi, getFavoriteStatusApi } from '@/api/modules/news'
import { mockNews } from '@/mocks/shop'
import { useAuthStore } from '@/stores/auth'
import EmptyState from '@/components/EmptyState.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import LoadingState from '@/components/LoadingState.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const error = ref('')
const article = ref(null)
const isFavorited = ref(false)
const favoring = ref(false)

function requireLogin() {
  ElMessageBox.confirm('请先登录后再操作', '提示', {
    confirmButtonText: '去登录',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    router.push(`/auth/login?redirect=${encodeURIComponent(route.fullPath)}`)
  }).catch(() => {})
}

async function loadArticle() {
  loading.value = true
  error.value = ''
  try {
    const result = await getNewsDetailApi(route.params.id)
    article.value = result.item || result.data || result
  } catch (err) {
    article.value = mockNews.find((item) => String(item.id) === String(route.params.id)) || null
    error.value = err.message || '资讯详情加载失败，当前显示演示数据'
  } finally {
    loading.value = false
  }
  if (authStore.isAuthenticated) {
    try {
      const r = await getFavoriteStatusApi(route.params.id)
      isFavorited.value = r.favorited
    } catch {
      // ignore
    }
  }
}

async function handleFavorite() {
  if (!authStore.isAuthenticated) {
    requireLogin()
    return
  }
  favoring.value = true
  try {
    const r = await toggleFavoriteApi(route.params.id)
    isFavorited.value = r.favorited
    ElMessage.success(isFavorited.value ? '已收藏该资讯' : '已取消收藏')
  } catch (err) {
    ElMessage.error(err?.message || '操作失败')
  } finally {
    favoring.value = false
  }
}

function handleShare() {
  if (!authStore.isAuthenticated) {
    requireLogin()
    return
  }
  navigator.clipboard.writeText(window.location.href).then(() => {
    ElMessage.success('分享链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.info('分享链接：' + window.location.href)
  })
}

watch(() => route.params.id, loadArticle)
onMounted(loadArticle)
</script>

<template>
  <div class="news-detail-page">
    <el-breadcrumb separator="/" style="margin-bottom: 20px">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/news' }">三农资讯</el-breadcrumb-item>
      <el-breadcrumb-item>{{ article?.title || '资讯详情' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <ErrorAlert v-if="error" :message="error" />
    <LoadingState v-if="loading" :rows="6" />
    <EmptyState v-else-if="!article" description="资讯不存在或已下架">
      <template #extra>
        <el-button @click="router.push('/news')">返回资讯列表</el-button>
      </template>
    </EmptyState>

    <div v-else class="article-card">
      <h1 class="article-title">{{ article.title }}</h1>
      <div class="article-meta">
        <span class="meta-category">{{ article.category || '三农资讯' }}</span>
        <span class="meta-divider">·</span>
        <span>作者：{{ article.author }}</span>
        <span class="meta-divider">·</span>
        <span>{{ article.publishedAt || '-' }}</span>
      </div>
      <el-divider />
      <div class="article-summary">{{ article.summary }}</div>
      <div class="article-content">{{ article.content }}</div>
      <el-divider />
      <div class="article-actions">
        <button
          class="action-btn"
          :class="{ favorited: isFavorited }"
          :disabled="favoring"
          @click="handleFavorite"
        >
          <span v-if="favoring" class="btn-spinner"></span>
          <span v-else>{{ isFavorited ? '★ 已收藏' : '☆ 收藏' }}</span>
        </button>
        <button class="action-btn share" @click="handleShare">🔗 分享</button>
        <button class="back-link" @click="router.push('/news')">← 返回列表</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.news-detail-page {
  padding-bottom: 32px;
  max-width: 860px;
}

.article-card {
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: 32px;
}
.article-title {
  margin: 0 0 16px;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
  color: var(--color-soil);
  line-height: 1.4;
}
.article-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  color: var(--color-text-muted);
  font-size: 13px;
  flex-wrap: wrap;
}
.meta-category {
  padding: 2px 12px;
  border-radius: var(--radius-full);
  background: var(--color-terracotta-soft);
  color: var(--color-terracotta);
  font-size: 12px;
  font-weight: 600;
}
.meta-divider {
  color: var(--color-border);
}

.article-summary {
  color: var(--color-text-soft);
  font-size: 15px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: var(--color-cream);
  border-left: 4px solid var(--color-amber);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  line-height: 1.7;
}
.article-content {
  color: var(--color-text);
  font-size: 15px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.article-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
.action-btn {
  padding: 8px 20px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--color-border);
  background: var(--color-paper-white);
  color: var(--color-text-soft);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.action-btn:hover {
  border-color: var(--color-terracotta);
  color: var(--color-terracotta);
}
.action-btn.favorited {
  border-color: var(--color-amber);
  background: var(--color-amber-glow);
  color: var(--color-soil);
}
.btn-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(0,0,0,0.2);
  border-top-color: var(--color-soil);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.back-link {
  padding: 8px 16px;
  border: none;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s;
  margin-left: auto;
}
.back-link:hover {
  color: var(--color-terracotta);
}
</style>
