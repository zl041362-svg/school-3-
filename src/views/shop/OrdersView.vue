<script setup>
import { computed, onMounted, ref } from 'vue'
import EmptyState from '@/components/EmptyState.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import LoadingState from '@/components/LoadingState.vue'
import { useOrderStore } from '@/stores/orders'

const orderStore = useOrderStore()
const activeTab = ref('全部')
const statusTabs = ['全部', '待发货', '待收货', '已完成']

const STATUS_MAP = {
  待支付: 'pending_payment',
  待发货: 'pending_shipment',
  待收货: 'shipped',
  已完成: 'completed',
  已取消: 'cancelled',
}

const STATUS_LABEL = {
  pending_payment: '待支付',
  pending_shipment: '待发货',
  shipped: '待收货',
  completed: '已完成',
  cancelled: '已取消',
}

const STATUS_TAG = {
  pending_payment: 'warning',
  pending_shipment: 'primary',
  shipped: '',
  completed: 'success',
  cancelled: 'info',
}

const orders = computed(() => {
  if (activeTab.value === '全部') return orderStore.orders
  const statusCode = STATUS_MAP[activeTab.value]
  return orderStore.orders.filter((o) => o.status === statusCode)
})

onMounted(() => {
  orderStore.hydrate()
})
</script>

<template>
  <div class="orders-page">
    <div class="page-head">
      <div>
        <h1 class="page-title">我的订单</h1>
        <p class="page-sub">跟踪订单状态，管理您的采购</p>
      </div>
      <el-button @click="orderStore.hydrate">刷新</el-button>
    </div>

    <div class="tab-bar">
      <button
        v-for="tab in statusTabs"
        :key="tab"
        class="tab-btn"
        :class="{ active: activeTab === tab }"
        @click="activeTab = tab"
      >{{ tab }}</button>
    </div>

    <ErrorAlert v-if="orderStore.error" :message="orderStore.error" />
    <LoadingState v-if="orderStore.loading" :rows="4" />
    <EmptyState v-else-if="!orders.length" description="暂无订单" />

    <div v-else class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-head">
          <span class="order-no">订单号：{{ order.id }}</span>
          <el-tag :type="STATUS_TAG[order.status] || ''">
            {{ STATUS_LABEL[order.status] || order.status }}
          </el-tag>
        </div>
        <div class="order-body">
          <span>下单时间：{{ order.createdAt || '-' }}</span>
          <span class="order-amount">¥{{ order.amount }}</span>
        </div>
        <div class="order-foot">
          <button class="detail-btn" @click="$router.push(`/orders/${order.id}`)">查看详情</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.orders-page {
  padding-bottom: 32px;
}
.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
}
.page-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
  color: var(--color-soil);
}
.page-sub {
  margin: 4px 0 0;
  font-size: 14px;
  color: var(--color-text-muted);
}

.tab-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}
.tab-btn {
  padding: 7px 20px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--color-border);
  background: var(--color-paper-white);
  color: var(--color-text-soft);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.tab-btn:hover {
  border-color: var(--color-terracotta-soft);
  color: var(--color-terracotta);
}
.tab-btn.active {
  background: var(--color-terracotta);
  border-color: var(--color-terracotta);
  color: #fff;
  font-weight: 600;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.order-card {
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: 20px;
  transition: all 0.3s var(--ease-smooth);
}
.order-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-terracotta-soft);
}
.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.order-no {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-soil);
}
.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  color: var(--color-text-soft);
  font-size: 13px;
}
.order-amount {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 800;
  color: var(--color-berry);
}
.order-foot {
  text-align: right;
}
.detail-btn {
  padding: 7px 20px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--color-terracotta);
  background: transparent;
  color: var(--color-terracotta);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.detail-btn:hover {
  background: var(--color-terracotta);
  color: #fff;
}
</style>
