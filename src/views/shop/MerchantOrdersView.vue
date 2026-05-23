<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ErrorAlert from '@/components/ErrorAlert.vue'
import LoadingState from '@/components/LoadingState.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getMerchantOrdersApi, shipOrderApi } from '@/api/modules/merchant'
import { resolveItems } from '@/utils/apiResponse'

const loading = ref(false)
const error = ref('')
const orders = ref([])
const activeStatus = ref('全部')
const shippingDialog = ref(false)
const currentOrderId = ref(null)
const logisticsInput = ref('')

const statusTabs = ['全部', '待发货', '已发货', '已完成']
const STATUS_LABEL = { pending_payment: '待支付', pending_shipment: '待发货', shipped: '已发货', completed: '已完成', cancelled: '已取消' }
const STATUS_TAG = { pending_payment: 'danger', pending_shipment: 'warning', shipped: 'primary', completed: 'success', cancelled: 'info' }

const filteredOrders = computed(() => {
  if (activeStatus.value === '全部') return orders.value
  const map = { 待发货: 'pending_shipment', 已发货: 'shipped', 已完成: 'completed' }
  return orders.value.filter((o) => o.status === map[activeStatus.value])
})

async function loadOrders() {
  loading.value = true
  error.value = ''
  try {
    const result = await getMerchantOrdersApi({ pageSize: 100 })
    orders.value = resolveItems(result)
  } catch (err) {
    error.value = err?.message || '加载订单列表失败'
  } finally {
    loading.value = false
  }
}

function openShipDialog(orderId) {
  currentOrderId.value = orderId
  logisticsInput.value = ''
  shippingDialog.value = true
}

async function handleShip() {
  if (!logisticsInput.value.trim()) {
    ElMessage.warning('请填写物流单号')
    return
  }
  try {
    await shipOrderApi(currentOrderId.value, logisticsInput.value.trim())
    ElMessage.success('已标记为已发货')
    shippingDialog.value = false
    loadOrders()
  } catch (err) {
    ElMessage.error(err?.message || '发货失败')
  }
}

onMounted(loadOrders)
</script>

<template>
  <div class="merchant-page">
    <div class="page-head">
      <div>
        <h1 class="page-title">订单管理</h1>
        <p class="page-sub">处理客户订单与发货</p>
      </div>
      <el-button @click="loadOrders">刷新</el-button>
    </div>

    <ErrorAlert v-if="error" :message="error" />

    <div class="filter-bar">
      <button
        v-for="tab in statusTabs" :key="tab"
        class="filter-btn" :class="{ active: activeStatus === tab }"
        @click="activeStatus = tab"
      >{{ tab }}</button>
    </div>

    <LoadingState v-if="loading" :rows="4" />
    <EmptyState v-else-if="!filteredOrders.length" description="暂无订单" />

    <div v-else class="order-list">
      <div v-for="order in filteredOrders" :key="order.id" class="order-card">
        <div class="order-head">
          <span class="order-no">订单号：#{{ order.orderNo || order.id }}</span>
          <el-tag :type="STATUS_TAG[order.status] || ''">
            {{ STATUS_LABEL[order.status] || order.status }}
          </el-tag>
        </div>
        <div class="order-body">
          <div>收货人：{{ order.receiver }} / {{ order.phone }}</div>
          <div class="order-address">地址：{{ order.address }}</div>
          <div v-if="order.logistics" class="order-logistics">物流单号：{{ order.logistics }}</div>
          <div class="order-amount">金额：<span class="amount-num">¥{{ order.amount }}</span></div>
        </div>
        <div class="order-foot">
          <span class="order-time">{{ order.createdAt || '-' }}</span>
          <el-button v-if="order.status === 'pending_shipment'" type="primary" size="small" @click="openShipDialog(order.id)">填写物流</el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="shippingDialog" title="发货信息" width="400px">
      <el-form label-position="top">
        <el-form-item label="物流单号">
          <el-input v-model="logisticsInput" placeholder="请输入快递单号或物流编号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shippingDialog = false">取消</el-button>
        <el-button type="primary" @click="handleShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.merchant-page {
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

.filter-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.filter-btn {
  padding: 5px 16px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--color-border);
  background: var(--color-paper-white);
  color: var(--color-text-soft);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.filter-btn:hover {
  border-color: var(--color-terracotta-soft);
  color: var(--color-terracotta);
}
.filter-btn.active {
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
  color: var(--color-text-soft);
  font-size: 13px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}
.order-address {
  color: var(--color-text-muted);
  font-size: 12px;
}
.order-logistics {
  color: var(--color-terracotta);
  font-size: 13px;
  font-weight: 500;
}
.order-amount {
  margin-top: 4px;
}
.amount-num {
  font-family: var(--font-display);
  font-weight: 800;
  color: var(--color-berry);
  font-size: 18px;
}
.order-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-time {
  color: var(--color-text-muted);
  font-size: 12px;
}
</style>
