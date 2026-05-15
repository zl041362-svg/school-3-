<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.users)
const pagination = computed(() => moderationStore.pagination('users'))
const searchKeyword = ref('')
const filterRole = ref('')

const filteredRows = computed(() => {
  let list = rows.value
  if (searchKeyword.value) {
    list = list.filter((u) => (u.name || '').includes(searchKeyword.value))
  }
  if (filterRole.value) {
    list = list.filter((u) => u.role === filterRole.value)
  }
  return list
})

function handlePageChange(page) {
  moderationStore.hydrateSection('users', { page })
}

async function toggleStatus(row) {
  const nextStatus = row.status === 'active' ? 'disabled' : 'active'
  const action = nextStatus === 'disabled' ? '禁用' : '启用'
  await ElMessageBox.confirm(`确认${action}用户「${row.name}」？`, '提示', { type: 'warning' })
  await moderationStore.updateUserStatus(row.id, nextStatus)
  ElMessage.success(`用户已${action}`)
}

onMounted(() => {
  moderationStore.hydrateSection('users')
})
</script>

<template>
  <PageContainer title="用户管理">
    <template #actions>
      <el-button @click="moderationStore.hydrateSection('users')">刷新</el-button>
    </template>

    <el-alert
      v-if="moderationStore.error"
      type="warning"
      show-icon
      :closable="false"
      :title="moderationStore.error"
      style="margin-bottom: 16px"
    />

    <div style="display: flex; gap: 12px; margin-bottom: 16px">
      <el-input v-model="searchKeyword" placeholder="搜索用户名" style="max-width: 200px" clearable />
      <el-select v-model="filterRole" placeholder="角色筛选" style="width: 120px" clearable>
        <el-option label="管理员" value="admin" />
        <el-option label="农户" value="farmer" />
        <el-option label="消费者" value="customer" />
      </el-select>
    </div>

    <el-table v-loading="moderationStore.loadingMap.users" :data="filteredRows" border>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="name" label="用户名" />
      <el-table-column prop="role" label="角色" width="140" />
      <el-table-column prop="createdAt" label="注册时间" width="180" />
      <el-table-column label="状态" width="140">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">{{
            scope.row.status === 'active' ? '正常' : '已禁用'
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="scope">
          <el-button text type="primary" @click="toggleStatus(scope.row)">
            {{ scope.row.status === 'active' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无用户数据" />
      </template>
    </el-table>

    <div v-if="pagination.total > pagination.pageSize" style="text-align: center; margin-top: 16px">
      <el-pagination
        :current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </PageContainer>
</template>
