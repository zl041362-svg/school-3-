<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNewsDetailApi, toggleFavoriteApi, getFavoriteStatusApi } from '@/api/modules/news'
import { mockNews } from '@/mocks/shop'
import { useAuthStore } from '@/stores/auth'

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
      // ignore - favorite status is optional
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

    <el-alert
      v-if="error"
      type="warning"
      show-icon
      :closable="false"
      :title="error"
      style="margin-bottom: 16px"
    />
    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="!article" description="资讯不存在或已下架">
      <template #extra>
        <el-button @click="router.push('/news')">返回资讯列表</el-button>
      </template>
    </el-empty>

    <el-card v-else class="article-card">
      <h1 class="article-title">{{ article.title }}</h1>
      <div class="article-meta">
        <el-tag type="success" size="small">{{ article.category || '三农资讯' }}</el-tag>
        <span>作者：{{ article.author }}</span>
        <span>发布时间：{{ article.publishedAt || '-' }}</span>
      </div>
      <el-divider />
      <div class="article-summary">{{ article.summary }}</div>
      <div class="article-content">{{ article.content }}</div>
      <el-divider />
      <div class="article-actions">
        <el-button :type="isFavorited ? 'warning' : ''" :loading="favoring" @click="handleFavorite">
          {{ isFavorited ? '⭐ 已收藏' : '☆ 收藏' }}
        </el-button>
        <el-button @click="handleShare">🔗 分享</el-button>
        <el-button text @click="router.push('/news')">← 返回列表</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.news-detail-page {
  padding-bottom: 32px;
}
.article-card {
  max-width: 860px;
}
.article-title {
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 16px;
  line-height: 1.4;
}
.article-meta {
  display: flex;
  gap: 16px;
  align-items: center;
  color: #999;
  font-size: 13px;
  flex-wrap: wrap;
}
.article-summary {
  color: #555;
  font-size: 15px;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-left: 3px solid var(--zhhs-primary, #2e7d32);
  border-radius: 4px;
}
.article-content {
  color: #333;
  font-size: 15px;
  line-height: 1.8;
  white-space: pre-wrap;
}
.article-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
