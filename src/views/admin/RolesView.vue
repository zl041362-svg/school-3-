<script setup>
import { computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.roles)

async function handleMembersChange(row, value) {
  await moderationStore.updateRoleMembers(row.id, Number(value))
  ElMessage.success('角色成员数已更新')
}

onMounted(() => {
  moderationStore.hydrateSection('roles')
})
</script>

<template>
  <PageContainer title="角色管理">
    <template #actions>
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
      <template #empty>
        <el-empty description="暂无角色数据" />
      </template>
    </el-table>
  </PageContainer>
</template>
