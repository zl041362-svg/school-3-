<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import PageContainer from '@/components/PageContainer.vue'
import { useAddressStore } from '@/stores/address'
import { useCartStore } from '@/stores/cart'
import { useOrderStore } from '@/stores/orders'

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
  <PageContainer title="确认订单">
    <template #actions>
      <el-tag type="success" size="large">合计：￥{{ cartStore.totalAmount }}</el-tag>
    </template>

    <el-alert
      v-if="cartStore.error || orderStore.error || addressStore.error"
      type="warning"
      show-icon
      :closable="false"
      :title="cartStore.error || orderStore.error || addressStore.error"
      style="margin-bottom: 16px"
    />
    <el-empty
      v-if="!cartStore.items.length && !cartStore.loading"
      description="购物车为空，请先添加商品"
    >
      <template #extra>
        <el-button type="primary" @click="router.push('/products')">去选购</el-button>
      </template>
    </el-empty>

    <template v-else>
      <!-- 收货信息 -->
      <el-card style="margin-bottom: 16px">
        <template #header>收货信息</template>
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
      </el-card>

      <!-- 商品清单 -->
      <el-card style="margin-bottom: 16px">
        <template #header>商品清单</template>
        <el-table :data="cartStore.items" border>
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
        <div style="text-align: right; margin-top: 12px; font-size: 16px; font-weight: 600">
          订单合计：<span style="color: #e53935">￥{{ cartStore.totalAmount }}</span>
        </div>
      </el-card>

      <el-button
        type="primary"
        size="large"
        :loading="submitting"
        :disabled="!canSubmit"
        @click="handleSubmit"
        >提交订单</el-button
      >
    </template>
  </PageContainer>
</template>
