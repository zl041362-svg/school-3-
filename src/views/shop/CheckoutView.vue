<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAddressStore } from '@/stores/address'
import { useCartStore } from '@/stores/cart'
import { useOrderStore } from '@/stores/orders'
import EmptyState from '@/components/EmptyState.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'

const router = useRouter()
const addressStore = useAddressStore()
const cartStore = useCartStore()
const orderStore = useOrderStore()
const submitting = ref(false)
const selectedAddressId = ref(null)
const formRef = ref(null)

const form = reactive({ receiver: '', phone: '', address: '' })
const addresses = computed(() => addressStore.addresses)
const canSubmit = computed(() =>
  Boolean(form.receiver && form.phone && form.address && cartStore.items.length > 0),
)

const rules = {
  receiver: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' },
  ],
  address: [{ required: true, message: '请填写详细地址', trigger: 'blur' }],
}

function applyAddress(address) {
  if (!address) return
  selectedAddressId.value = address.id
  form.receiver = address.receiver
  form.phone = address.phone
  form.address = address.address
}

async function handleSubmit() {
  await cartStore.hydrate()

  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!cartStore.items.length) {
    ElMessage.warning('购物车为空，请先添加商品')
    return
  }

  submitting.value = true
  try {
    const order = await orderStore.createOrder({ receiver: form.receiver, phone: form.phone, address: form.address })
    cartStore.clear()
    ElMessage.success('订单提交成功！')
    router.push(`/orders/${order.id}`)
  } catch (error) {
    ElMessage.error(error?.message || orderStore.error || '订单提交失败，请稍后重试。')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  cartStore.hydrate()
  addressStore.hydrate().then(() => {
    if (addressStore.defaultAddress) applyAddress(addressStore.defaultAddress)
  })
})
</script>

<template>
  <div class="checkout-page">
    <div class="page-head">
      <h1 class="page-title">确认订单</h1>
      <p class="page-sub">合计：¥{{ cartStore.totalAmount }}</p>
    </div>

    <ErrorAlert v-if="cartStore.error || orderStore.error || addressStore.error"
      :message="cartStore.error || orderStore.error || addressStore.error" />

    <EmptyState
      v-if="!cartStore.items.length && !cartStore.loading"
      description="购物车为空，请先添加商品"
    >
      <template #extra>
        <el-button type="primary" @click="router.push('/products')">去选购</el-button>
      </template>
    </EmptyState>

    <template v-else>
      <!-- 收货信息 -->
      <div class="checkout-card">
        <h3 class="card-title">收货信息</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="选择已保存地址">
            <el-select
              :model-value="selectedAddressId"
              placeholder="请选择收货地址（可选）"
              style="width: 100%"
              clearable
              @change="(value) => applyAddress(addresses.find((item) => item.id === value))"
            >
              <el-option
                v-for="item in addresses"
                :key="item.id"
                :label="`${item.receiver} / ${item.phone} / ${item.address}`"
                :value="item.id"
              />
            </el-select>
            <el-link type="primary" style="margin-top: 6px" @click="$router.push('/profile')"
              >前往个人中心管理地址</el-link
            >
          </el-form-item>
          <el-row :gutter="16">
            <el-col :md="8" :sm="24">
              <el-form-item label="收货人姓名" prop="receiver">
                <el-input v-model="form.receiver" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>
            <el-col :md="8" :sm="24">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
              </el-form-item>
            </el-col>
            <el-col :md="8" :sm="24">
              <el-form-item label="详细地址" prop="address">
                <el-input v-model="form.address" placeholder="省市区+详细地址" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 商品清单 -->
      <div class="checkout-card">
        <h3 class="card-title">商品清单</h3>
        <el-table :data="cartStore.items" border>
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
          订单合计：<span>¥{{ cartStore.totalAmount }}</span>
        </div>
      </div>

      <button
        class="submit-btn"
        :disabled="!canSubmit || submitting"
        @click="handleSubmit"
      >
        <span v-if="submitting" class="btn-spinner"></span>
        <span v-else>提交订单</span>
      </button>
    </template>
  </div>
</template>

<style scoped>
.checkout-page {
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

.checkout-card {
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
.order-total span {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 800;
  color: var(--color-berry);
}

.submit-btn {
  padding: 14px 40px;
  border: none;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-berry), #D4534A);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s var(--ease-smooth);
  box-shadow: 0 4px 20px rgba(184, 69, 58, 0.3);
}
.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 28px rgba(184, 69, 58, 0.4);
}
.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
