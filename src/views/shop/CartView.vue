<script setup>
import { computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import EmptyState from '@/components/EmptyState.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const cartRows = computed(() => cartStore.items)

async function handleQtyChange(row, value) {
  try {
    await cartStore.updateQty(row.id, Number(value))
  } catch (error) {
    ElMessage.error(error?.message || cartStore.error || '更新数量失败')
  }
}

async function handleRemove(row) {
  try {
    await cartStore.removeItem(row.id)
    ElMessage.success('商品已移除')
  } catch (error) {
    ElMessage.error(error?.message || cartStore.error || '删除商品失败')
  }
}

onMounted(() => {
  cartStore.hydrate()
})
</script>

<template>
  <div class="cart-page">
    <div class="page-head">
      <div>
        <h1 class="page-title">购物车</h1>
        <p class="page-sub">{{ cartStore.itemCount }} 件商品</p>
      </div>
    </div>

    <ErrorAlert v-if="cartStore.error" :message="cartStore.error" />

    <EmptyState
      v-if="!cartRows.length && !cartStore.loading"
      description="购物车为空，快去选购喜欢的农产品吧"
    >
      <template #extra>
        <el-button type="primary" @click="router.push('/products')">去选购</el-button>
      </template>
    </EmptyState>

    <template v-else>
      <div class="cart-table-wrap">
        <el-table v-loading="cartStore.loading" :data="cartRows" border>
          <el-table-column prop="name" label="商品名称" />
          <el-table-column label="单价" width="120">
            <template #default="scope">¥{{ scope.row.price }}</template>
          </el-table-column>
          <el-table-column label="数量" width="180">
            <template #default="scope">
              <el-input-number
                :model-value="scope.row.qty"
                :min="1"
                :max="scope.row.stock || 999"
                @change="(value) => handleQtyChange(scope.row, value)"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="140">
            <template #default="scope">¥{{ (scope.row.qty * scope.row.price).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button text type="danger" @click="handleRemove(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="cartRows.length" class="cart-summary">
        <span class="summary-label">合计</span>
        <span class="summary-amount">¥{{ cartStore.totalAmount }}</span>
        <button class="checkout-btn" @click="router.push('/checkout')">结算下单</button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.cart-page {
  padding-bottom: 32px;
}
.page-head {
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

.cart-table-wrap {
  background: var(--color-paper-white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  overflow: hidden;
}

.cart-summary {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 20px;
  padding: 16px 20px;
  background: var(--color-paper-white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
}
.summary-label {
  font-size: 16px;
  color: var(--color-text-soft);
}
.summary-amount {
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: 900;
  color: var(--color-berry);
}
.checkout-btn {
  padding: 12px 32px;
  border: none;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-terracotta), var(--color-amber));
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s var(--ease-smooth);
  box-shadow: 0 4px 16px rgba(193, 114, 69, 0.3);
}
.checkout-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 24px rgba(193, 114, 69, 0.4);
}
</style>
