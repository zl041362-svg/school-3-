<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { useAuthStore } from '@/stores/auth'
import { useAddressStore } from '@/stores/address'
import { updateProfileApi } from '@/api/modules/auth'
import { AUTH_USER_KEY } from '@/constants/auth'
import { readJsonStorage, writeJsonStorage } from '@/utils/storage'

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
const recentViews = ref([])

const profileDialogVisible = ref(false)
const profileName = ref('')

function openProfileEdit() {
  profileName.value = authStore.user?.name || ''
  profileDialogVisible.value = true
}

async function saveProfile() {
  if (!profileName.value.trim()) { ElMessage.warning('请输入用户名'); return }
  try {
    const r = await updateProfileApi({ name: profileName.value.trim() })
    if (authStore.user) authStore.user = r.user
    writeJsonStorage(AUTH_USER_KEY, r.user)
    ElMessage.success('资料已更新')
    profileDialogVisible.value = false
  } catch (err) {
    ElMessage.error(err?.message || '更新失败')
  }
}

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
  if (addressStore.count >= 10) {
    ElMessage.warning('最多添加10个收货地址')
    return
  }
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
    await ElMessageBox.confirm('确认删除该地址？', '提示', { type: 'warning' })
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

const menuItems = computed(() => {
  const items = [
    { icon: '📦', label: '我的订单', color: '#409eff', path: '/orders' },
    { icon: '🛒', label: '购物车', color: '#e6a23c', path: '/cart' },
    { icon: '⭐', label: '我的评价', color: '#f56c6c', path: '/profile/reviews' },
    { icon: '❤️', label: '我的收藏', color: '#e040fb', path: '/profile/favorites' },
  ]
  if (isFarmer.value) {
    items.push({ icon: '🌾', label: '商户后台', color: '#67c23a', path: '/merchant' })
    items.push({ icon: '✅', label: '身份认证', color: '#909399', path: '/merchant/verify' })
  }
  return items
})

onMounted(() => {
  addressStore.hydrate()
  recentViews.value = readJsonStorage('ZHHS_RECENT_VIEWS', [])
})
</script>

<template>
  <PageContainer title="个人中心">
    <template #actions>
      <el-button type="primary" @click="handleCreate">新增收货地址</el-button>
    </template>

    <!-- 用户头部 -->
    <div class="user-header">
      <div class="user-avatar">👤</div>
      <div class="user-info">
        <div class="user-name-row">
          <span class="user-name">{{ authStore.user?.name || '用户' }}</span>
          <el-tag :type="isFarmer ? 'success' : 'primary'" size="small">{{ isFarmer ? '农户' : '消费者' }}</el-tag>
        </div>
        <div class="user-meta">
          <span>{{ authStore.user?.phone || '' }}</span>
          <span>·</span>
          <span>{{ addressStore.count }} 个收货地址</span>
        </div>
      </div>
      <el-button size="small" plain @click="openProfileEdit">编辑资料</el-button>
    </div>

    <!-- 功能入口网格 -->
    <div class="menu-grid">
      <div v-for="m in menuItems" :key="m.path" class="menu-card" @click="$router.push(m.path)">
        <div class="menu-icon" :style="{ background: m.color + '15', color: m.color }">{{ m.icon }}</div>
        <span class="menu-label">{{ m.label }}</span>
      </div>
    </div>

    <!-- 最近浏览 -->
    <el-card v-if="recentViews.length" class="recent-card">
      <template #header><span>最近浏览</span></template>
      <div class="recent-scroll">
        <div v-for="v in recentViews" :key="v.id" class="recent-product" @click="$router.push(`/products/${v.id}`)">
          <div class="recent-product-icon">🌿</div>
          <div class="recent-product-name">{{ v.name }}</div>
          <div class="recent-product-time">{{ v.time?.slice(5) || '' }}</div>
        </div>
      </div>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑收货地址' : '新增收货地址'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="收货人姓名" prop="receiver">
          <el-input v-model="form.receiver" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="3" placeholder="请输入省市区+详细地址" />
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

    <el-dialog v-model="profileDialogVisible" title="编辑个人资料" width="400px">
      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="profileName" placeholder="请输入新的用户名" maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: linear-gradient(135deg, #e8f5e9, #fff);
  border-radius: 12px;
  margin-bottom: 20px;
}
.user-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #c8e6c9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}
.user-info {
  flex: 1;
  min-width: 0;
}
.user-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.user-name {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}
.user-meta {
  display: flex;
  gap: 8px;
  color: #888;
  font-size: 13px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}
.menu-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 20px 16px;
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.menu-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}
.menu-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin: 0 auto 8px;
}
.menu-label {
  font-size: 13px;
  color: #555;
}

.recent-card {
  margin-bottom: 20px;
}
.recent-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}
.recent-scroll::-webkit-scrollbar { height: 4px; }
.recent-scroll::-webkit-scrollbar-thumb { background: #ddd; border-radius: 2px; }
.recent-product {
  flex-shrink: 0;
  width: 100px;
  text-align: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background 0.2s;
}
.recent-product:hover {
  background: #f5f7fa;
}
.recent-product-icon {
  width: 48px;
  height: 48px;
  background: #e8f5e9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin: 0 auto 6px;
}
.recent-product-name {
  font-size: 12px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.recent-product-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 2px;
}

@media (max-width: 600px) {
  .menu-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .user-header {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
