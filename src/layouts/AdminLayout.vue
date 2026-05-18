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
    <el-aside width="220px" class="admin-aside">
      <div class="admin-brand">🌾 智慧三农·管理</div>
      <el-menu :default-active="activePath" class="admin-menu" @select="handleSelect">
        <el-menu-item v-for="menu in menus" :key="menu.path" :index="menu.path">
          <span class="menu-icon">{{ menu.icon }}</span>
          <span>{{ menu.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <el-breadcrumb separator=">">
          <el-breadcrumb-item>管理后台</el-breadcrumb-item>
          <el-breadcrumb-item>{{ route.meta.title || '控制台' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <el-space>
          <el-button size="small" @click="$router.push('/')">前往商城</el-button>
          <el-button type="danger" plain size="small" @click="handleLogout">退出登录</el-button>
        </el-space>
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
.admin-aside {
  background: #1a2e1a;
  overflow: hidden;
}
.admin-brand {
  padding: 20px 16px;
  font-size: 16px;
  font-weight: 700;
  color: #a5d6a7;
  border-bottom: 1px solid #2e4d2e;
}
.admin-menu {
  background: transparent;
  border-right: none;
}
.admin-menu .el-menu-item {
  color: #c8e6c9;
}
.admin-menu .el-menu-item:hover,
.admin-menu .el-menu-item.is-active {
  background: var(--zhhs-primary, #2e7d32) !important;
  color: #fff;
}
.menu-icon {
  margin-right: 6px;
}
.admin-header {
  background: #fff;
  border-bottom: 1px solid #e8f5e9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.admin-main {
  background: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .admin-aside {
    width: 64px !important;
  }
  .admin-aside .menu-icon {
    margin-right: 0;
  }
  .admin-aside .el-menu-item span:last-child {
    display: none;
  }
  .admin-brand {
    font-size: 20px;
    padding: 16px 8px;
    text-align: center;
  }
  .admin-header {
    padding: 0 12px;
  }
  .admin-main {
    padding: 16px;
  }
}
</style>
