<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'
import { createAdminPermissionApi, updateAdminPermissionApi, deleteAdminPermissionApi } from '@/api/modules/admin'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.permissions)
const pagination = computed(() => moderationStore.pagination('permissions'))
const searchKeyword = ref('')

const filteredRows = computed(() => {
  if (!searchKeyword.value) return rows.value
  return rows.value.filter((p) =>
    (p.module || '').includes(searchKeyword.value) ||
    (p.action || '').includes(searchKeyword.value) ||
    (p.role || '').includes(searchKeyword.value)
  )
})

const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({ module: '', action: '', role: '' })

function handlePageChange(page) { moderationStore.hydrateSection('permissions', { page }) }

function handleCreate() {
  editingId.value = null
  form.module = ''; form.action = ''; form.role = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  editingId.value = row.id
  form.module = row.module; form.action = row.action; form.role = row.role
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.module || !form.action || !form.role) { ElMessage.warning('请填写完整信息'); return }
  try {
    if (editingId.value) {
      await updateAdminPermissionApi(editingId.value, form)
      ElMessage.success('权限已更新')
    } else {
      await createAdminPermissionApi(form)
      ElMessage.success('权限已添加')
    }
    dialogVisible.value = false
    moderationStore.hydrateSection('permissions')
  } catch (err) { ElMessage.error(err?.message || '操作失败') }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该权限？', '提示', { type: 'warning' })
  try {
    await deleteAdminPermissionApi(row.id)
    ElMessage.success('权限已删除')
    moderationStore.hydrateSection('permissions')
  } catch (err) { ElMessage.error(err?.message || '删除失败') }
}

onMounted(() => { moderationStore.hydrateSection('permissions') })
</script>

<template>
  <PageContainer title="权限配置">
    <template #actions>
      <el-button type="primary" @click="handleCreate">+ 新增权限</el-button>
      <el-button @click="moderationStore.hydrateSection('permissions')">刷新</el-button>
    </template>

    <ErrorAlert v-if="moderationStore.error" :message="moderationStore.error" />

    <div class="filter-bar">
      <el-input v-model="searchKeyword" placeholder="搜索模块/操作/角色" style="max-width: 260px" clearable />
    </div>

    <el-table v-loading="moderationStore.loadingMap?.permissions" :data="filteredRows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="module" label="模块" />
      <el-table-column prop="role" label="角色" width="140" />
      <el-table-column prop="action" label="允许操作" width="200" />
      <el-table-column label="操作" width="160">
        <template #default="scope">
          <el-button text type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button text type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="pagination.total > pagination.pageSize" style="text-align: center; margin-top: 20px">
      <el-pagination :current-page="pagination.page" :page-size="pagination.pageSize" :total="pagination.total" layout="prev, pager, next" @current-change="handlePageChange" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑权限' : '新增权限'" width="460px">
      <el-form :model="form" label-position="top">
        <el-form-item label="模块"><el-input v-model="form.module" placeholder="如：product-review" /></el-form-item>
        <el-form-item label="操作"><el-input v-model="form.action" placeholder="如：approve" /></el-form-item>
        <el-form-item label="角色"><el-input v-model="form.role" placeholder="如：auditor" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>
