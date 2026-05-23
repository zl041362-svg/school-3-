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
  margin-bottom: 48px;
}
.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 20px;
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
.section-more {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--color-text-soft);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.section-more:hover {
  border-color: var(--color-terracotta);
  color: var(--color-terracotta);
}
.arrow {
  transition: transform 0.25s var(--ease-smooth);
}
.section-more:hover .arrow { transform: translateX(3px); }

.news-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}
.news-tag {
  padding: 5px 16px;
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-soft);
  background: var(--color-cream-dark);
  border: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.news-tag:hover {
  color: var(--color-terracotta);
  border-color: var(--color-terracotta-soft);
  background: var(--color-paper-white);
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.news-card {
  display: flex;
  gap: 16px;
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
  width: 44px;
  height: 44px;
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
.news-title {
  margin: 0 0 6px;
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 700;
  color: var(--color-soil);
  line-height: 1.3;
}
.news-summary {
  margin: 0 0 10px;
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

@media (max-width: 900px) {
  .news-grid { grid-template-columns: 1fr; }
}
</style>
