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
  <el-container class="shop-layout">
    <el-header class="shop-header" height="60px">
      <div class="header-inner">
        <div class="brand" @click="$router.push('/')">
          <span class="brand-icon">🌾</span>
          <span class="brand-name">智慧三农</span>
        </div>
        <nav class="shop-nav">
          <RouterLink to="/">首页</RouterLink>
          <RouterLink to="/products">农产品商城</RouterLink>
          <RouterLink to="/news">三农资讯</RouterLink>
          <RouterLink v-if="isAuthenticated" to="/orders">我的订单</RouterLink>
        </nav>
        <div class="shop-actions">
          <el-badge v-if="isAuthenticated" :value="cartCount || ''" :hidden="!cartCount" type="danger">
            <el-button text @click="$router.push('/cart')">🛒 购物车</el-button>
          </el-badge>
          <el-button v-else text @click="$router.push('/cart')">🛒 购物车</el-button>
          <template v-if="isAuthenticated">
            <el-button text @click="$router.push('/profile')">个人中心</el-button>
            <el-button
              v-if="isFarmer"
              size="small"
              type="success"
              plain
              @click="$router.push('/merchant')"
              >商户后台</el-button
            >
            <el-button
              v-if="isAdmin"
              size="small"
              type="warning"
              plain
              @click="$router.push('/admin')"
              >管理后台</el-button
            >
            <el-button type="danger" plain size="small" @click="handleLogout">退出</el-button>
          </template>
          <template v-else>
            <el-button size="small" @click="$router.push('/auth/login')">登录</el-button>
            <el-button type="primary" size="small" @click="$router.push('/auth/register')"
              >注册</el-button
            >
          </template>
        </div>
      </div>
    </el-header>

    <el-main class="shop-main">
      <RouterView />
    </el-main>

    <el-footer class="shop-footer" height="60px">
      <div class="footer-inner">
        <span>© 2026 智慧三农平台 &nbsp;|&nbsp; 农产品产销直连，绿色生活从这里开始</span>
      </div>
    </el-footer>
  </el-container>
</template>

<style scoped>
.shop-layout {
  min-height: 100vh;
  background: #f7f9f5;
}
.shop-header {
  background: #fff;
  border-bottom: 1px solid #e8f5e9;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 0 24px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}
.brand-icon {
  font-size: 24px;
}
.brand-name {
  font-size: 18px;
  font-weight: 700;
  color: #2e7d32;
  white-space: nowrap;
}
.shop-nav {
  display: flex;
  gap: 24px;
  flex: 1;
}
.shop-nav a {
  color: #555;
  text-decoration: none;
  font-size: 14px;
  white-space: nowrap;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
  transition:
    color 0.2s,
    border-color 0.2s;
}
.shop-nav a:hover,
.shop-nav a.router-link-active {
  color: #2e7d32;
  border-bottom-color: #2e7d32;
}
.shop-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.shop-main {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 24px;
}
.shop-footer {
  background: #2e7d32;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.footer-inner {
  font-size: 13px;
  opacity: 0.85;
}
</style>
