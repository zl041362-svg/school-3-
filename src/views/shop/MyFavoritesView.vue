<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { getFavoritesApi } from '@/api/modules/news'
import { toggleFavoriteApi } from '@/api/modules/news'

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
  <PageContainer title="我的收藏">
    <el-alert v-if="error" type="warning" show-icon :closable="false" :title="error" style="margin-bottom: 16px" />
    <el-skeleton v-if="loading" :rows="4" animated />
    <el-empty v-else-if="!favorites.length" description="暂无收藏">
      <template #extra><el-button type="primary" @click="$router.push('/news')">去浏览资讯</el-button></template>
    </el-empty>
    <div v-else class="fav-list">
      <div v-for="f in favorites" :key="f.newsId" class="fav-card">
        <div class="fav-body">
          <div class="fav-title" @click="$router.push(`/news/${f.newsId}`)">{{ f.title }}</div>
          <div class="fav-meta">
            <el-tag size="small" type="success">{{ f.category || '资讯' }}</el-tag>
            <span>{{ f.createdAt || '' }}</span>
          </div>
        </div>
        <el-button text type="danger" size="small" @click="unfavorite(f.newsId)">取消收藏</el-button>
      </div>
    </div>
    <div v-if="total > 10" style="text-align: center; margin-top: 16px">
      <el-pagination :current-page="page" :page-size="10" :total="total" layout="prev, pager, next" @current-change="(p) => { page = p; load() }" />
    </div>
  </PageContainer>
</template>

<style scoped>
.fav-list { display: flex; flex-direction: column; gap: 10px; }
.fav-card { display: flex; align-items: center; justify-content: space-between; padding: 14px; border: 1px solid #eee; border-radius: 8px; }
.fav-body { flex: 1; min-width: 0; }
.fav-title { font-weight: 600; font-size: 14px; cursor: pointer; color: var(--zhhs-primary, #2e7d32); margin-bottom: 4px; }
.fav-title:hover { text-decoration: underline; }
.fav-meta { display: flex; align-items: center; gap: 8px; color: #999; font-size: 12px; }
</style>
