<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useOrderStore } from '@/stores/orders'
import { confirmReceiptApi } from '@/api/modules/orders'
import EmptyState from '@/components/EmptyState.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import LoadingState from '@/components/LoadingState.vue'

const route = useRoute()
const orderStore = useOrderStore()
const order = ref(null)
const confirming = ref(false)
const loading = ref(false)

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

async function loadOrder() {
  loading.value = true
  try {
    order.value = await orderStore.getDetail(route.params.id)
  } catch {
    order.value = null
    ElMessage.error('订单详情加载失败')
  } finally {
    loading.value = false
  }
}

async function handleConfirmReceipt() {
  confirming.value = true
  try {
    await confirmReceiptApi(order.value.id)
    await loadOrder()
    ElMessage.success('确认收货成功，感谢您的购买！')
  } catch (err) {
    ElMessage.error(err?.message || '确认收货失败，请稍后重试')
  } finally {
    confirming.value = false
  }
}

watch(() => route.params.id, loadOrder)
onMounted(loadOrder)
</script>

<template>
  <div class="order-detail-page">
    <el-breadcrumb separator="/" style="margin-bottom: 20px">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/orders' }">我的订单</el-breadcrumb-item>
      <el-breadcrumb-item>订单详情</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="page-head">
      <h1 class="page-title">{{ order?.id ? `订单详情 #${order.id}` : '订单详情' }}</h1>
      <el-button @click="loadOrder">刷新</el-button>
    </div>

    <ErrorAlert v-if="orderStore.error" :message="orderStore.error" />
    <LoadingState v-if="loading" :rows="6" />
    <EmptyState v-else-if="!order" description="订单不存在">
      <template #extra>
        <el-button type="primary" @click="$router.push('/orders')">返回订单列表</el-button>
      </template>
    </EmptyState>

    <template v-else-if="order">
      <!-- 状态栏 -->
      <div class="status-card">
        <div class="status-info">
          <span class="status-label">订单状态</span>
          <el-tag :type="STATUS_TAG[order.status] || ''" size="large">
            {{ STATUS_LABEL[order.status] || order.status }}
          </el-tag>
        </div>
        <el-button
          v-if="order.status === 'shipped'"
          type="primary"
          :loading="confirming"
          @click="handleConfirmReceipt"
        >确认收货</el-button>
      </div>

      <!-- 收货信息 -->
      <div class="info-card">
        <h3 class="card-title">收货信息</h3>
        <el-descriptions border :column="2">
          <el-descriptions-item label="收货人">{{ order.receiver }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.phone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ order.address }}</el-descriptions-item>
          <el-descriptions-item label="物流信息" :span="2">{{ order.logistics || '暂无物流信息' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 商品明细 -->
      <div class="info-card">
        <h3 class="card-title">商品明细</h3>
        <el-table :data="order.items || []" border>
          <el-table-column prop="name" label="商品名称" />
          <el-table-column label="单价" width="120">
            <template #default="scope">¥{{ scope.row.price }}</template>
          </el-table-column>
          <el-table-column prop="qty" label="数量" width="100" />
          <el-table-column label="小计" width="140">
            <template #default="scope">¥{{ (scope.row.qty * scope.row.price).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
        <div class="order-total">
          订单合计：<span class="total-amount">¥{{ order.amount }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.order-detail-page {
  padding-bottom: 32px;
}
.page-head {
  display: flex;
  align-items: center;
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

.status-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  margin-bottom: 16px;
}
.status-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.status-label {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-soil);
}

.info-card {
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-bottom: 16px;
}
.card-title {
  margin: 0 0 16px;
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--color-soil);
}

.order-total {
  text-align: right;
  margin-top: 16px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-soft);
}
.total-amount {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 800;
  color: var(--color-berry);
}
</style>
