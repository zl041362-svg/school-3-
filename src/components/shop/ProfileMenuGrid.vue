<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const menuItems = computed(() => {
  const items = [
    { icon: '📦', label: '我的订单', path: '/orders' },
    { icon: '🛒', label: '购物车', path: '/cart' },
    { icon: '⭐', label: '我的评价', path: '/profile/reviews' },
    { icon: '❤️', label: '我的收藏', path: '/profile/favorites' },
  ]
  if (authStore.role === 'farmer') {
    items.push({ icon: '🌾', label: '商户后台', path: '/merchant' })
    items.push({ icon: '✅', label: '身份认证', path: '/merchant/verify' })
  }
  return items
})
</script>

<template>
  <div class="menu-grid">
    <div v-for="m in menuItems" :key="m.path" class="menu-card" @click="$router.push(m.path)">
      <div class="menu-icon">{{ m.icon }}</div>
      <span class="menu-label">{{ m.label }}</span>
    </div>
  </div>
</template>

<style scoped>
.menu-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-bottom: 24px;
}
.menu-card {
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: 22px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.35s var(--ease-smooth);
}
.menu-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-terracotta-soft);
}
.menu-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin: 0 auto 8px;
  background: var(--color-cream-dark);
}
.menu-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-soft);
}

@media (max-width: 600px) {
  .menu-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
