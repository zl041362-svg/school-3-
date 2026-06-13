<script setup>
import { useRouter } from 'vue-router'
import LoadingState from '@/components/LoadingState.vue'
import EmptyState from '@/components/EmptyState.vue'

defineProps({
  news: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
})

const router = useRouter()

const tags = [
  { label: '政策解读', value: 'policy' },
  { label: '种植技术', value: 'tech' },
  { label: '产业动态', value: 'industry' },
  { label: '市场行情', value: 'market' },
]
</script>

<template>
  <section class="section">
    <div class="section-head">
      <div>
        <h2 class="section-title">三农资讯</h2>
        <p class="section-desc">政策 · 技术 · 行情 · 乡间故事</p>
      </div>
      <button class="section-more" @click="router.push('/news')">
        查看全部 <span class="arrow">→</span>
      </button>
    </div>

    <div class="news-tags">
      <button
        v-for="t in tags"
        :key="t.value"
        class="news-tag"
        @click="router.push('/news')"
      >{{ t.label }}</button>
    </div>

    <LoadingState v-if="loading" :rows="2" />
    <div v-else class="news-grid">
      <article
        v-for="(item, idx) in news"
        :key="item.id"
        class="news-card"
        :style="{ animationDelay: idx * 0.1 + 's' }"
        @click="router.push(`/news/${item.id}`)"
      >
        <div class="news-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
            <line x1="8" y1="7" x2="16" y2="7"/>
            <line x1="8" y1="11" x2="14" y2="11"/>
          </svg>
        </div>
        <div class="news-body">
          <h3 class="news-title">{{ item.title }}</h3>
          <p class="news-summary">{{ item.summary }}</p>
          <div class="news-meta">
            <span>{{ item.author }}</span>
            <span class="meta-divider">·</span>
            <span>{{ item.publishedAt || '-' }}</span>
          </div>
        </div>
      </article>
      <EmptyState v-if="!news.length" description="暂无资讯" />
    </div>
  </section>
</template>

<style scoped>
.section {
  margin-bottom: 24px;
}
.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 10px;
}
.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}
.section-desc {
  margin: 1px 0 0;
  font-size: 12px;
  color: var(--color-text-hint);
}
.section-more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--color-text-hint);
  cursor: pointer;
  border: none;
  background: none;
}
.section-more:hover { color: var(--color-primary); }

.news-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.news-tag {
  padding: 4px 14px;
  font-size: 13px;
  color: var(--color-text-secondary);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: border-color 0.15s;
}
.news-tag:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.news-card {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: border-color 0.15s;
}
.news-card:hover {
  border-color: var(--color-primary);
}
.news-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F5F5F5;
  color: var(--color-text-hint);
}
.news-body {
  flex: 1;
  min-width: 0;
}
.news-title {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text);
  line-height: 1.3;
}
.news-summary {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--color-text-hint);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.news-meta {
  font-size: 12px;
  color: var(--color-text-hint);
}
.meta-divider {
  margin: 0 6px;
}

@media (max-width: 900px) {
  .news-grid { grid-template-columns: 1fr; }
}
</style>
