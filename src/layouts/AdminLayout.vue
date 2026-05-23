<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const menus = [
  { label: '控制台', path: '/admin', icon: '📊' },
  { label: '用户管理', path: '/admin/users', icon: '👥' },
  { label: '农户审核', path: '/admin/farmer-verifications', icon: '🌾' },
  { label: '商品管理', path: '/admin/products', icon: '📦' },
  { label: '商品审核', path: '/admin/product-reviews', icon: '✅' },
  { label: '资讯管理', path: '/admin/news', icon: '📰' },
  { label: '资讯审核', path: '/admin/news-reviews', icon: '📝' },
  { label: '角色管理', path: '/admin/roles', icon: '🎭' },
  { label: '权限配置', path: '/admin/permissions', icon: '🔑' },
  { label: '操作日志', path: '/admin/logs', icon: '📋' },
]

const activePath = computed(() => route.path)

function handleSelect(path) {
  router.push(path)
}

function handleLogout() {
  authStore.logout()
  router.push('/auth/login')
}
</script>

<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside width="230px" class="admin-aside">
      <div class="aside-brand">
        <span class="brand-glyph">丰</span>
        <span class="brand-text">智慧三农 · 管理</span>
      </div>
      <nav class="aside-nav">
        <button
          v-for="menu in menus"
          :key="menu.path"
          class="nav-item"
          :class="{ active: activePath === menu.path }"
          @click="handleSelect(menu.path)"
        >
          <span class="nav-icon">{{ menu.icon }}</span>
          <span class="nav-label">{{ menu.label }}</span>
        </button>
      </nav>
      <div class="aside-foot">
        <button class="shop-link" @click="$router.push('/')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
          前往商城
        </button>
      </div>
    </el-aside>

    <!-- 主区域 -->
    <el-container>
      <el-header class="admin-header">
        <el-breadcrumb separator=">">
          <el-breadcrumb-item>管理后台</el-breadcrumb-item>
          <el-breadcrumb-item>{{ route.meta.title || '控制台' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="header-actions">
          <button class="action-btn" @click="handleLogout">退出登录</button>
        </div>
      </el-header>

      <el-main class="admin-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout {
  height: 100vh;
}

/* ═══ Aside ═══ */
.admin-aside {
  background: linear-gradient(180deg, #2E2218 0%, #3C2A1C 40%, #2E2218 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.aside-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 22px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.brand-glyph {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-terracotta), var(--color-amber));
  color: #fff;
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 900;
  border-radius: 8px;
  flex-shrink: 0;
}
.brand-text {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 0.03em;
}

/* Navigation */
.aside-nav {
  flex: 1;
  padding: 12px 12px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 16px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: rgba(255, 255, 255, 0.55);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
  text-align: left;
  width: 100%;
}
.nav-item:hover {
  color: rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.06);
}
.nav-item.active {
  color: #fff;
  background: rgba(193, 114, 69, 0.3);
  font-weight: 600;
}
.nav-icon {
  width: 20px;
  text-align: center;
  flex-shrink: 0;
}

.aside-foot {
  padding: 14px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}
.shop-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 16px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: var(--radius-full);
  background: transparent;
  color: rgba(255, 255, 255, 0.55);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.shop-link:hover {
  border-color: rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.85);
}

/* ═══ Header ═══ */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  background: rgba(253, 249, 242, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--color-border-light);
  height: 56px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.action-btn {
  padding: 6px 16px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--color-text-muted);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.action-btn:hover {
  border-color: var(--color-berry);
  color: var(--color-berry);
}

/* ═══ Main ═══ */
.admin-main {
  background: var(--color-cream);
  padding: 28px;
  overflow-y: auto;
}

@media (max-width: 860px) {
  .admin-aside {
    width: 70px !important;
  }
  .brand-text {
    display: none;
  }
  .nav-label {
    display: none;
  }
  .nav-item {
    justify-content: center;
    padding: 12px;
  }
  .nav-icon {
    margin: 0;
  }
  .aside-brand {
    justify-content: center;
    padding: 16px 8px;
  }
  .admin-main {
    padding: 16px;
  }
  .admin-header {
    padding: 0 16px;
  }
}
</style>
