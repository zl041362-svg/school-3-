<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { updateProfileApi } from '@/api/modules/auth'
import { AUTH_USER_KEY } from '@/constants/auth'
import { writeJsonStorage } from '@/utils/storage'

const authStore = useAuthStore()
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
</script>

<template>
  <div class="user-header">
    <div class="user-avatar">
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
        <circle cx="12" cy="7" r="4"/>
      </svg>
    </div>
    <div class="user-info">
      <div class="user-name-row">
        <span class="user-name">{{ authStore.user?.name || '用户' }}</span>
        <el-tag :type="authStore.role === 'farmer' ? 'success' : 'primary'" size="small">
          {{ authStore.role === 'farmer' ? '农户' : '消费者' }}
        </el-tag>
      </div>
      <div class="user-meta">
        <span>{{ authStore.user?.phone || '' }}</span>
        <slot name="meta" />
      </div>
    </div>
    <button class="edit-btn" @click="openProfileEdit">编辑资料</button>
  </div>

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
</template>

<style scoped>
.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: linear-gradient(135deg, var(--color-cream-dark), var(--color-paper-white));
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  margin-bottom: 20px;
}
.user-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-terracotta-soft), var(--color-amber-glow));
  color: var(--color-terracotta);
  display: flex;
  align-items: center;
  justify-content: center;
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
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--color-soil);
}
.user-meta {
  display: flex;
  gap: 8px;
  color: var(--color-text-muted);
  font-size: 13px;
}
.edit-btn {
  padding: 7px 18px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--color-text-soft);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.edit-btn:hover {
  border-color: var(--color-terracotta);
  color: var(--color-terracotta);
}

@media (max-width: 600px) {
  .user-header {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
