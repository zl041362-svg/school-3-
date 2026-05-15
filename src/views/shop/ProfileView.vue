<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { useAuthStore } from '@/stores/auth'
import { useAddressStore } from '@/stores/address'

const authStore = useAuthStore()
const addressStore = useAddressStore()
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({
  receiver: '',
  phone: '',
  address: '',
  isDefault: false,
})

const addresses = computed(() => addressStore.addresses)
const isFarmer = computed(() => authStore.role === 'farmer')

const rules = {
  receiver: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' },
  ],
  address: [{ required: true, message: '请填写详细地址', trigger: 'blur' }],
}

function resetForm() {
  editingId.value = null
  form.receiver = ''
  form.phone = ''
  form.address = ''
  form.isDefault = addresses.value.length === 0
}

function handleCreate() {
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  editingId.value = row.id
  form.receiver = row.receiver
  form.phone = row.phone
  form.address = row.address
  form.isDefault = row.isDefault
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    saving.value = true
    await addressStore.saveAddress({
      id: editingId.value,
      receiver: form.receiver,
      phone: form.phone,
      address: form.address,
      isDefault: form.isDefault,
    })
    dialogVisible.value = false
    ElMessage.success('地址已保存')
  } catch (error) {
    ElMessage.error(error?.message || addressStore.error || '保存地址失败')
  } finally {
    saving.value = false
  }
}

async function handleRemove(row) {
  try {
    await addressStore.removeAddress(row.id)
    ElMessage.success('地址已删除')
  } catch (error) {
    ElMessage.error(error?.message || addressStore.error || '删除地址失败')
  }
}

async function handleSetDefault(row) {
  try {
    await addressStore.setDefault(row.id)
    ElMessage.success('默认地址已更新')
  } catch (error) {
    ElMessage.error(error?.message || addressStore.error || '设置默认地址失败')
  }
}

onMounted(() => {
  addressStore.hydrate()
})
</script>

<template>
  <PageContainer title="个人中心">
    <template #actions>
      <el-button type="primary" @click="handleCreate">新增收货地址</el-button>
    </template>

    <!-- 基础信息 -->
    <el-card style="margin-bottom: 16px">
      <template #header>账号信息</template>
      <el-descriptions border :column="3">
        <el-descriptions-item label="用户名">{{
          authStore.user?.name || '用户'
        }}</el-descriptions-item>
        <el-descriptions-item label="身份">
          <el-tag :type="isFarmer ? 'success' : 'primary'">
            {{ isFarmer ? '农户/经营者' : '消费者' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收货地址数">{{ addressStore.count }} 个</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 快捷入口 -->
    <el-card style="margin-bottom: 16px">
      <template #header>快捷功能</template>
      <el-space wrap>
        <el-button @click="$router.push('/orders')">我的订单</el-button>
        <el-button @click="$router.push('/cart')">购物车</el-button>
        <el-button v-if="isFarmer" type="success" @click="$router.push('/merchant')"
          >商户后台</el-button
        >
        <el-button v-if="isFarmer" @click="$router.push('/merchant/verify')">身份认证</el-button>
      </el-space>
    </el-card>

    <!-- 收货地址 -->
    <el-card>
      <template #header>收货地址管理（最多10个）</template>
      <el-alert
        v-if="addressStore.error"
        type="warning"
        show-icon
        :closable="false"
        :title="addressStore.error"
        style="margin-bottom: 12px"
      />
      <el-empty
        v-if="!addresses.length && !addressStore.loading"
        description="暂无收货地址，点击右上角新增"
      />
      <el-table v-else v-loading="addressStore.loading" :data="addresses" border>
        <el-table-column prop="receiver" label="收货人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="address" label="收货地址" />
        <el-table-column label="默认" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isDefault" type="success">默认</el-tag>
            <el-button v-else text type="primary" @click="handleSetDefault(scope.row)"
              >设为默认</el-button
            >
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-space>
              <el-button text type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button text type="danger" @click="handleRemove(scope.row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑收货地址' : '新增收货地址'"
      width="520px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="收货人姓名" prop="receiver">
          <el-input v-model="form.receiver" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input
            v-model="form.address"
            type="textarea"
            :rows="3"
            placeholder="请输入省市区+详细地址"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </el-space>
      </template>
    </el-dialog>
  </PageContainer>
</template>
