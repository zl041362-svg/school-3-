<script setup>
import { onMounted, ref } from 'vue'
import PageContainer from '@/components/PageContainer.vue'
import { getMerchantDashboardApi } from '@/api/modules/merchant'

const loading = ref(false)
const error = ref('')
const stats = ref({ productCount: 0, newsCount: 0, pendingOrderCount: 0 })

async function loadDashboard() {
  loading.value = true
  error.value = ''
  try {
    const result = await getMerchantDashboardApi()
    stats.value = result
  } catch (err) {
    error.value = err?.message || '加载仪表盘数据失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>

<template>
  <PageContainer title="商户后台">
    <el-alert
      v-if="error"
      type="warning"
      show-icon
      :closable="false"
      :title="error"
      style="margin-bottom: 16px"
    />

    <el-row :gutter="16" style="margin-bottom: 24px">
      <el-col :md="8" :sm="24" style="margin-bottom: 12px">
        <el-card class="stat-card">
          <div v-loading="loading" class="stat-num">{{ stats.productCount }}</div>
          <div class="stat-label">我的商品</div>
        </el-card>
      </el-col>
      <el-col :md="8" :sm="24" style="margin-bottom: 12px">
        <el-card class="stat-card">
          <div v-loading="loading" class="stat-num">{{ stats.pendingOrderCount }}</div>
          <div class="stat-label">待处理订单</div>
        </el-card>
      </el-col>
      <el-col :md="8" :sm="24" style="margin-bottom: 12px">
        <el-card class="stat-card">
          <div v-loading="loading" class="stat-num">{{ stats.newsCount }}</div>
          <div class="stat-label">我的资讯</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>快捷入口</template>
      <el-space wrap size="large">
        <el-button type="primary" @click="$router.push('/merchant/products')"
          >🌾 商品管理</el-button
        >
        <el-button type="success" @click="$router.push('/merchant/news')">📰 资讯管理</el-button>
        <el-button @click="$router.push('/orders')">📦 订单查看</el-button>
        <el-button @click="$router.push('/merchant/verify')">✅ 身份认证</el-button>
      </el-space>
    </el-card>
  </PageContainer>
</template>

<style scoped>
.stat-card {
  text-align: center;
}
.stat-num {
  font-size: 32px;
  font-weight: 700;
  color: #2e7d32;
}
.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 6px;
}
</style>
