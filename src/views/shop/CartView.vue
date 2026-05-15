<script setup>
import { computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import PageContainer from '@/components/PageContainer.vue'
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
  <PageContainer title="购物车">
    <template #actions>
      <el-tag type="success">{{ cartStore.itemCount }} 件商品</el-tag>
    </template>

    <el-alert
      v-if="cartStore.error"
      type="warning"
      show-icon
      :closable="false"
      :title="cartStore.error"
      style="margin-bottom: 16px"
    />
    <el-empty
      v-if="!cartRows.length && !cartStore.loading"
      description="购物车为空，快去选购喜欢的农产品吧"
    >
      <template #extra>
        <el-button type="primary" @click="router.push('/products')">去选购</el-button>
      </template>
    </el-empty>

    <el-table v-else v-loading="cartStore.loading" :data="cartRows" border>
      <el-table-column prop="name" label="商品名称" />
      <el-table-column label="单价" width="120">
        <template #default="scope">￥{{ scope.row.price }}</template>
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
      <el-table-column label="小计" width="120">
        <template #default="scope">￥{{ (scope.row.qty * scope.row.price).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button text type="danger" @click="handleRemove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="cartRows.length" style="margin-top: 20px; text-align: right">
      <div style="font-size: 18px; font-weight: 600; margin-bottom: 12px">
        合计：<span style="color: #e53935">￥{{ cartStore.totalAmount }}</span>
      </div>
      <el-button type="primary" size="large" @click="router.push('/checkout')">结算下单</el-button>
    </div>
  </PageContainer>
</template>
