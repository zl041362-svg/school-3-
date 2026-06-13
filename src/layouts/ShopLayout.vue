<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const isAdmin = computed(() => authStore.role === 'admin')
const isFarmer = computed(() => authStore.role === 'farmer')
const cartCount = computed(() => cartStore.itemCount)

function handleLogout() {
  authStore.logout()
  router.push('/auth/login')
}
</script>

<template>
  <div class="shop-shell">
    <!-- ═══ 顶部导航栏 ═══ -->
    <header class="shop-topbar">
      <div class="topbar-inner">
        <router-link to="/" class="brand-mark">
          <span class="brand-glyph">丰</span>
          <span class="brand-word">智慧三农</span>
        </router-link>

        <nav class="nav-links">
          <RouterLink to="/" class="nav-item">首页</RouterLink>
          <RouterLink to="/products" class="nav-item">市集</RouterLink>
          <RouterLink to="/news" class="nav-item">乡讯</RouterLink>
          <RouterLink v-if="isAuthenticated" to="/orders" class="nav-item">订单</RouterLink>
        </nav>

        <div class="nav-actions">
          <router-link to="/cart" class="cart-btn" :class="{ 'has-items': cartCount > 0 }">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
            </svg>
            <span v-if="cartCount" class="cart-dot">{{ cartCount > 99 ? '99+' : cartCount }}</span>
          </router-link>

          <template v-if="isAuthenticated">
            <router-link to="/profile" class="nav-icon-btn" title="个人中心">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </router-link>
            <button
              v-if="isFarmer"
              class="role-badge farmer"
              @click="$router.push('/merchant')"
            >商户</button>
            <button
              v-if="isAdmin"
              class="role-badge admin"
              @click="$router.push('/admin')"
            >管理</button>
            <button class="logout-btn" @click="handleLogout">退出</button>
          </template>
          <template v-else>
            <button class="btn-text" @click="$router.push('/auth/login')">登录</button>
            <button class="btn-primary-sm" @click="$router.push('/auth/register')">注册</button>
          </template>
        </div>
      </div>
    </header>

    <!-- ═══ 页面内容 ═══ -->
    <main class="shop-body">
      <RouterView />
    </main>

    <!-- ═══ 底部 ═══ -->
    <footer class="shop-foot">
      <div class="foot-inner">
        <div class="foot-brand">
          <span class="foot-glyph">丰</span>
          <span class="foot-name">智慧三农</span>
        </div>
        <p class="foot-desc">农产品产销直连 · 从田间到餐桌的温暖链接</p>
        <p class="foot-copy">© 2026 智慧三农平台</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.shop-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-cream);
}

/* ═══ Topbar ═══ */
.shop-topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #FFFFFF;
  border-bottom: 1px solid var(--color-border);
}

.topbar-inner {
  max-width: 1240px;
  margin: 0 auto;
  height: 56px;
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 0 28px;
}

/* Brand */
.brand-mark {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  text-decoration: none;
}
.brand-glyph {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  border-radius: 4px;
}
.brand-word {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}

/* Nav */
.nav-links {
  display: flex;
  gap: 4px;
  flex: 1;
}
.nav-item {
  padding: 6px 16px;
  border-radius: var(--radius-full);
  font-size: 14px;
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: color 0.15s;
}
.nav-item:hover { color: var(--color-primary); }
.nav-item.router-link-active {
  color: var(--color-primary);
  font-weight: 600;
  background: var(--color-primary-light);
}

/* Actions */
.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.cart-btn {
  position: relative;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  color: var(--color-text-secondary);
  transition: color 0.15s;
}
.cart-btn:hover,
.cart-btn.has-items {
  color: var(--color-primary);
}
.cart-dot {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: var(--radius-full);
  background: var(--color-primary);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  color: var(--color-text-secondary);
  transition: color 0.15s;
}
.nav-icon-btn:hover { color: var(--color-primary); }

.role-badge {
  padding: 4px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
  background: #FFFFFF;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s;
}
.role-badge:hover { color: var(--color-primary); border-color: var(--color-primary); }

.logout-btn {
  padding: 4px 12px;
  font-size: 12px;
  border: none;
  background: transparent;
  color: var(--color-text-hint);
  cursor: pointer;
}
.logout-btn:hover { color: var(--color-text-secondary); }

.btn-text {
  padding: 6px 14px;
  border: none;
  background: transparent;
  font-size: 14px;
  color: var(--color-text-secondary);
  cursor: pointer;
}
.btn-text:hover { color: var(--color-primary); }

.btn-primary-sm {
  padding: 6px 18px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-primary-sm:hover { background: var(--color-primary-hover); }

/* ═══ Body ═══ */
.shop-body {
  flex: 1;
  max-width: 1240px;
  margin: 0 auto;
  width: 100%;
  padding: 20px 24px;
}

/* ═══ Footer ═══ */
.shop-foot {
  background: #333333;
  color: rgba(255, 255, 255, 0.6);
  padding: 32px 28px 24px;
}
.foot-inner {
  max-width: 1240px;
  margin: 0 auto;
  text-align: center;
}
.foot-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 8px;
}
.foot-glyph {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  border-radius: 4px;
}
.foot-name {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}
.foot-desc {
  margin: 0 0 6px;
  font-size: 13px;
}
.foot-copy {
  margin: 0;
  font-size: 12px;
  opacity: 0.45;
}

@media (max-width: 860px) {
  .topbar-inner {
    gap: 12px;
    padding: 0 16px;
  }
  .nav-links { gap: 2px; }
  .nav-item { padding: 5px 10px; font-size: 13px; }
  .shop-body { padding: 16px; }
}
</style>
