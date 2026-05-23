<script setup>
defineProps({
  views: { type: Array, default: () => [] },
})
</script>

<template>
  <div v-if="views.length" class="recent-section">
    <h3 class="recent-title">最近浏览</h3>
    <div class="recent-scroll">
      <div
        v-for="v in views"
        :key="v.id"
        class="recent-item"
        @click="$router.push(`/products/${v.id}`)"
      >
        <div class="recent-icon">🌿</div>
        <div class="recent-name">{{ v.name }}</div>
        <div class="recent-time">{{ v.time?.slice(5) || '' }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.recent-section {
  margin-bottom: 24px;
  padding: 20px;
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
}
.recent-title {
  margin: 0 0 14px;
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 700;
  color: var(--color-soil);
}
.recent-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}
.recent-scroll::-webkit-scrollbar { height: 4px; }
.recent-scroll::-webkit-scrollbar-thumb { background: var(--color-border); border-radius: 2px; }

.recent-item {
  flex-shrink: 0;
  width: 100px;
  text-align: center;
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius-sm);
  transition: background 0.25s;
}
.recent-item:hover {
  background: var(--color-cream-dark);
}
.recent-icon {
  width: 48px;
  height: 48px;
  background: var(--color-cream-dark);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin: 0 auto 6px;
}
.recent-name {
  font-size: 12px;
  color: var(--color-soil);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.recent-time {
  font-size: 11px;
  color: var(--color-text-muted);
  margin-top: 2px;
}
</style>
