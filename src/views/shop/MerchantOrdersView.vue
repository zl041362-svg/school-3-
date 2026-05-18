<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { getMerchantOrdersApi, shipOrderApi } from '@/api/modules/merchant'

const loading = ref(false)
const error = ref('')
const orders = ref([])
const activeStatus = ref('全部')
const shippingDialog = ref(false)
const currentOrderId = ref(null)
const logisticsInput = ref('')

const statusTabs = ['全部', '待发货', '已发货', '已完成']
const STATUS_LABEL = { pending_shipment: '待发货', shipped: '已发货', completed: '已完成', cancelled: '已取消' }
const STATUS_TAG = { pending_shipment: 'warning', shipped: 'primary', completed: 'success', cancelled: 'info' }

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
    orders.value = result.items || result.list || result.data || []
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
  <PageContainer title="订单管理">
    <template #actions>
      <el-button @click="loadOrders">刷新</el-button>
    </template>

    <el-alert
      v-if="error"
      type="warning" show-icon :closable="false"
      :title="error" style="margin-bottom: 16px"
    />

    <el-tabs v-model="activeStatus" style="margin-bottom: 16px">
      <el-tab-pane v-for="tab in statusTabs" :key="tab" :label="tab" :name="tab" />
    </el-tabs>

    <el-skeleton v-if="loading" :rows="4" animated />
    <el-empty v-else-if="!filteredOrders.length" description="暂无订单" />

    <div v-else class="order-list">
      <div v-for="order in filteredOrders" :key="order.id" class="order-card">
        <div class="order-header">
          <span class="order-no">订单号：#{{ order.orderNo || order.id }}</span>
          <el-tag :type="STATUS_TAG[order.status] || ''">
            {{ STATUS_LABEL[order.status] || order.status }}
          </el-tag>
        </div>
        <div class="order-body">
          <div>收货人：{{ order.receiver }} / {{ order.phone }}</div>
          <div class="order-address">地址：{{ order.address }}</div>
          <div v-if="order.logistics" class="order-logistics">物流单号：{{ order.logistics }}</div>
          <div class="order-amount">
            金额：<span>￥{{ order.amount }}</span>
          </div>
        </div>
        <div class="order-footer">
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
  flex-direction: column;
  gap: 4px;
  margin-bottom: 10px;
}
.order-address {
  color: #888;
  font-size: 12px;
}
.order-logistics {
  color: var(--zhhs-primary, #2e7d32);
  font-size: 13px;
  font-weight: 500;
}
.order-amount span {
  font-weight: 700;
  color: #e53935;
  font-size: 16px;
}
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-time {
  color: #999;
  font-size: 12px;
}
</style>
