<script setup>
import { computed, onMounted } from 'vue'
import PageContainer from '@/components/PageContainer.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.permissions)

onMounted(() => {
  moderationStore.hydrateSection('permissions')
})
</script>

<template>
  <PageContainer title="权限配置">
    <template #actions>
      <el-button @click="moderationStore.hydrateSection('permissions')">刷新</el-button>
    </template>

    <el-alert
      v-if="moderationStore.error"
      type="warning"
      show-icon
      :closable="false"
      :title="moderationStore.error"
      style="margin-bottom: 16px"
    />

    <el-table v-loading="moderationStore.loadingMap?.permissions" :data="rows" border>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="module" label="模块" />
      <el-table-column prop="role" label="角色" width="140" />
      <el-table-column prop="action" label="允许操作" width="200" />
      <template #empty>
        <el-empty description="暂无权限配置" />
      </template>
    </el-table>
  </PageContainer>
</template>
