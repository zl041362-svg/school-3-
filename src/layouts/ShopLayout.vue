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
/* ═══ Shell ═══ */
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
  background: rgba(253, 249, 242, 0.88);
  backdrop-filter: blur(18px) saturate(180%);
  -webkit-backdrop-filter: blur(18px) saturate(180%);
  border-bottom: 1px solid var(--color-border-light);
}

.topbar-inner {
  max-width: 1240px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 36px;
  padding: 0 28px;
}

/* Brand */
.brand-mark {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  text-decoration: none;
}
.brand-glyph {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-terracotta), var(--color-amber));
  color: #fff;
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 900;
  border-radius: 10px;
}
.brand-word {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--color-soil);
  letter-spacing: 0.04em;
}

/* Nav */
.nav-links {
  display: flex;
  gap: 8px;
  flex: 1;
}
.nav-item {
  padding: 8px 18px;
  border-radius: var(--radius-full);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-soft);
  text-decoration: none;
  transition: all 0.25s var(--ease-smooth);
}
.nav-item:hover {
  color: var(--color-terracotta);
  background: var(--color-terracotta-soft);
}
.nav-item.router-link-active {
  color: var(--color-terracotta);
  background: var(--color-terracotta-soft);
  font-weight: 600;
}

/* Actions */
.nav-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.cart-btn {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  color: var(--color-text-soft);
  transition: all 0.25s var(--ease-smooth);
}
.cart-btn:hover,
.cart-btn.has-items {
  color: var(--color-terracotta);
  background: var(--color-terracotta-soft);
}
.cart-dot {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: var(--radius-full);
  background: var(--color-berry);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-icon-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  color: var(--color-text-soft);
  transition: all 0.25s var(--ease-smooth);
}
.nav-icon-btn:hover {
  color: var(--color-terracotta);
  background: var(--color-terracotta-soft);
}

.role-badge {
  padding: 5px 14px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.role-badge.farmer {
  background: var(--color-sage-soft);
  color: var(--color-leaf-deep);
}
.role-badge.farmer:hover { filter: brightness(0.95); }
.role-badge.admin {
  background: var(--color-amber-glow);
  color: var(--color-soil);
}
.role-badge.admin:hover { filter: brightness(0.95); }

.logout-btn {
  padding: 5px 14px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 500;
  border: 1.5px solid var(--color-border);
  background: transparent;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.logout-btn:hover {
  border-color: var(--color-berry);
  color: var(--color-berry);
}

.btn-text {
  padding: 6px 16px;
  border: none;
  background: transparent;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-soft);
  cursor: pointer;
  transition: color 0.2s;
}
.btn-text:hover { color: var(--color-terracotta); }

.btn-primary-sm {
  padding: 8px 20px;
  border: none;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-terracotta), var(--color-amber));
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s var(--ease-smooth);
  box-shadow: 0 2px 8px rgba(193, 114, 69, 0.25);
}
.btn-primary-sm:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(193, 114, 69, 0.35);
}

/* ═══ Body ═══ */
.shop-body {
  flex: 1;
  max-width: 1240px;
  margin: 0 auto;
  width: 100%;
  padding: 32px 28px;
}

/* ═══ Footer ═══ */
.shop-foot {
  background: var(--color-soil);
  color: rgba(255, 255, 255, 0.7);
  padding: 40px 28px 32px;
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
  gap: 10px;
  margin-bottom: 12px;
}
.foot-glyph {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 900;
  border-radius: 8px;
}
.foot-name {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}
.foot-desc {
  margin: 0 0 8px;
  font-size: 13px;
  line-height: 1.8;
}
.foot-copy {
  margin: 0;
  font-size: 12px;
  opacity: 0.5;
}

/* ═══ Responsive ═══ */
@media (max-width: 860px) {
  .topbar-inner {
    gap: 16px;
    padding: 0 16px;
  }
  .nav-links { gap: 4px; }
  .nav-item { padding: 6px 12px; font-size: 13px; }
  .shop-body { padding: 20px 16px; }
}
</style>
