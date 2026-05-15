<script setup>
import { computed, onMounted, ref } from 'vue'
import PageContainer from '@/components/PageContainer.vue'
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
  <PageContainer title="我的订单">
    <template #actions>
      <el-button @click="orderStore.hydrate">刷新</el-button>
    </template>

    <el-tabs v-model="activeTab" style="margin-bottom: 16px">
      <el-tab-pane v-for="tab in statusTabs" :key="tab" :label="tab" :name="tab" />
    </el-tabs>

    <el-alert
      v-if="orderStore.error"
      type="warning"
      show-icon
      :closable="false"
      :title="orderStore.error"
      style="margin-bottom: 16px"
    />
    <el-skeleton v-if="orderStore.loading" :rows="4" animated />
    <el-empty v-else-if="!orders.length" description="暂无订单" />

    <div v-else class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <span class="order-no">订单号：{{ order.id }}</span>
          <el-tag :type="STATUS_TAG[order.status] || ''">
            {{ STATUS_LABEL[order.status] || order.status }}
          </el-tag>
        </div>
        <div class="order-body">
          <div>下单时间：{{ order.createdAt || '-' }}</div>
          <div class="order-amount">
            订单金额：<span>￥{{ order.amount }}</span>
          </div>
        </div>
        <div class="order-footer">
          <el-button text type="primary" @click="$router.push(`/orders/${order.id}`)"
            >查看详情</el-button
          >
        </div>
      </div>
    </div>
  </PageContainer>
</template>

<style scoped>
.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.order-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e8f5e9;
  padding: 16px;
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.order-no {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}
.order-body {
  color: #666;
  font-size: 13px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.order-amount span {
  font-weight: 700;
  color: #e53935;
  font-size: 16px;
}
.order-footer {
  text-align: right;
}
</style>
