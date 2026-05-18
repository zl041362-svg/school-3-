<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { useOrderStore } from '@/stores/orders'
import { confirmReceiptApi } from '@/api/modules/orders'

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
  <PageContainer :title="order?.id ? `订单详情 #${order.id}` : `订单详情`">
    <template #actions>
      <el-button @click="loadOrder">刷新</el-button>
    </template>

    <el-breadcrumb separator="/" style="margin-bottom: 16px">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/orders' }">我的订单</el-breadcrumb-item>
      <el-breadcrumb-item>订单详情</el-breadcrumb-item>
    </el-breadcrumb>

    <el-alert
      v-if="orderStore.error"
      type="warning"
      show-icon
      :closable="false"
      :title="orderStore.error"
      style="margin-bottom: 16px"
    />
    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="!order" description="订单不存在">
      <template #extra>
        <el-button type="primary" @click="$router.push('/orders')">返回订单列表</el-button>
      </template>
    </el-empty>

    <template v-else-if="order">
      <!-- 状态栏 -->
      <el-card style="margin-bottom: 16px">
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div>
            <span style="font-size: 16px; font-weight: 600">订单状态：</span>
            <el-tag :type="STATUS_TAG[order.status] || ''" size="large">
              {{ STATUS_LABEL[order.status] || order.status }}
            </el-tag>
          </div>
          <el-button v-if="order.status === 'shipped'" type="primary" :loading="confirming" @click="handleConfirmReceipt"
            >确认收货</el-button
          >
        </div>
      </el-card>

      <!-- 收货信息 -->
      <el-card style="margin-bottom: 16px">
        <template #header>收货信息</template>
        <el-descriptions border :column="2">
          <el-descriptions-item label="收货人">{{ order.receiver }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.phone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{
            order.address
          }}</el-descriptions-item>
          <el-descriptions-item label="物流信息" :span="2">{{
            order.logistics || '暂无物流信息'
          }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 订单商品 -->
      <el-card>
        <template #header>商品明细</template>
        <el-table :data="order.items || []" border>
          <el-table-column prop="name" label="商品名称" />
          <el-table-column label="单价" width="120">
            <template #default="scope">￥{{ scope.row.price }}</template>
          </el-table-column>
          <el-table-column prop="qty" label="数量" width="100" />
          <el-table-column label="小计" width="120">
            <template #default="scope"
              >￥{{ (scope.row.qty * scope.row.price).toFixed(2) }}</template
            >
          </el-table-column>
        </el-table>
        <div style="text-align: right; margin-top: 16px; font-size: 16px; font-weight: 600">
          订单合计：<span style="color: #e53935">￥{{ order.amount }}</span>
        </div>
      </el-card>
    </template>
  </PageContainer>
</template>
