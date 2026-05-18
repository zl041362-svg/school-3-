<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'
import { createAdminRoleApi, deleteAdminRoleApi, editAdminRoleApi, updateAdminRoleApi } from '@/api/modules/admin'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.roles)

const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({ role: '', description: '' })

function handleCreate() {
  editingId.value = null
  form.role = ''
  form.description = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  editingId.value = row.id
  form.role = row.role
  form.description = row.description || ''
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.role || !form.description) {
    ElMessage.warning('请填写角色名和描述')
    return
  }
  try {
    if (editingId.value) {
      await editAdminRoleApi(editingId.value, form)
      ElMessage.success('角色已更新')
    } else {
      await createAdminRoleApi(form)
      ElMessage.success('角色已创建')
    }
    dialogVisible.value = false
    moderationStore.hydrateSection('roles')
  } catch (err) {
    ElMessage.error(err?.message || '操作失败')
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除角色「${row.role}」？`, '提示', { type: 'warning' })
  await deleteAdminRoleApi(row.id)
  ElMessage.success('角色已删除')
  moderationStore.hydrateSection('roles')
}

async function handleMembersChange(row, value) {
  await updateAdminRoleApi(row.id, { members: Number(value) })
  ElMessage.success('角色成员数已更新')
}

onMounted(() => {
  moderationStore.hydrateSection('roles')
})
</script>

<template>
  <PageContainer title="角色管理">
    <template #actions>
      <el-button type="primary" @click="handleCreate">+ 新增角色</el-button>
      <el-button @click="moderationStore.hydrateSection('roles')">刷新</el-button>
    </template>

    <el-alert
      v-if="moderationStore.error"
      type="warning"
      show-icon
      :closable="false"
      :title="moderationStore.error"
      style="margin-bottom: 16px"
    />

    <el-table v-loading="moderationStore.loadingMap?.roles" :data="rows" border>
      <el-table-column prop="role" label="角色名称" />
      <el-table-column prop="description" label="说明" />
      <el-table-column label="成员数" width="160">
        <template #default="scope">
          <el-input-number
            :model-value="scope.row.members"
            :min="0"
            @change="(value) => handleMembersChange(scope.row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="scope">
          <el-button text type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button text type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无角色数据" />
      </template>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑角色' : '新增角色'" width="460px">
      <el-form :model="form" label-position="top">
        <el-form-item label="角色名"><el-input v-model="form.role" placeholder="如：auditor" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="2" placeholder="如：负责商品、资讯、认证审核" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>
