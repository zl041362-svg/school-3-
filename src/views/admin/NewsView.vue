<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'
import { updateAdminNewsApi, updateAdminNewsStatusApi } from '@/api/modules/admin'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.news)
const pagination = computed(() => moderationStore.pagination('news'))
const searchKeyword = ref('')
const activeStatus = ref('全部')

const statusOptions = ['全部', '草稿', '待审核', '已发布', '已驳回', '已下架']
const STATUS_MAP = { 草稿: 'draft', 待审核: 'pending', 已发布: 'published', 已驳回: 'rejected', 已下架: 'offline' }
const STATUS_LABEL = { draft: '草稿', pending: '待审核', published: '已发布', rejected: '已驳回', offline: '已下架' }
const STATUS_TAG = { draft: 'info', pending: 'warning', published: 'success', rejected: 'danger', offline: '' }

const filteredRows = computed(() => {
  let list = rows.value
  if (searchKeyword.value) {
    list = list.filter((n) => (n.title || '').includes(searchKeyword.value) || (n.author || '').includes(searchKeyword.value))
  }
  if (activeStatus.value !== '全部') {
    list = list.filter((n) => n.status === STATUS_MAP[activeStatus.value])
  }
  return list
})

const detailVisible = ref(false)
const detailRow = ref(null)
const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({ title: '', category: '', summary: '', content: '', author: '', status: '' })
const editingId = ref(null)

function handlePageChange(page) {
  moderationStore.hydrateSection('news', { page })
}

function showDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(editForm, {
    title: row.title || '',
    category: row.category || '',
    summary: row.summary || '',
    content: row.content || '',
    author: row.author || '',
    status: row.status || '',
  })
  editVisible.value = true
}

async function handleEditSave() {
  saving.value = true
  try {
    await updateAdminNewsApi(editingId.value, editForm)
    ElMessage.success('资讯已更新')
    editVisible.value = false
    moderationStore.hydrateSection('news')
  } catch (err) {
    ElMessage.error(err?.message || '更新失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const nextStatus = row.status === 'published' ? 'offline' : 'published'
  const label = nextStatus === 'published' ? '发布' : '下架'
  try {
    await updateAdminNewsStatusApi(row.id, { status: nextStatus })
    ElMessage.success(`资讯已${label}`)
    moderationStore.hydrateSection('news')
  } catch (err) {
    ElMessage.error(err?.message || `资讯${label}失败`)
  }
}

onMounted(() => {
  moderationStore.hydrateSection('news')
})
</script>

<template>
  <PageContainer title="资讯管理">
    <template #actions>
      <el-button @click="moderationStore.hydrateSection('news')">刷新</el-button>
    </template>

    <el-alert
      v-if="moderationStore.error"
      type="warning"
      show-icon
      :closable="false"
      :title="moderationStore.error"
      style="margin-bottom: 16px"
    />

    <div style="display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; align-items: center">
      <el-input v-model="searchKeyword" placeholder="搜索标题或作者" style="max-width: 260px" clearable />
      <el-tag
        v-for="s in statusOptions"
        :key="s"
        :type="activeStatus === s ? 'primary' : 'info'"
        :effect="activeStatus === s ? 'dark' : 'plain'"
        style="cursor: pointer"
        @click="activeStatus = s"
      >{{ s }}</el-tag>
    </div>

    <el-table v-loading="moderationStore.loadingMap?.news" :data="filteredRows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="author" label="作者" width="140" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="publishedAt" label="发布时间" width="170" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="STATUS_TAG[scope.row.status]">{{ STATUS_LABEL[scope.row.status] || scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button text type="primary" @click="showDetail(scope.row)">查看</el-button>
          <el-button text type="primary" @click="openEdit(scope.row)">编辑</el-button>
          <el-button v-if="scope.row.status === 'published'" text type="danger" @click="toggleStatus(scope.row)">下架</el-button>
          <el-button v-else-if="scope.row.status === 'offline'" text type="success" @click="toggleStatus(scope.row)">发布</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无资讯数据" />
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

    <el-dialog v-model="detailVisible" title="资讯详情" width="600px">
      <template v-if="detailRow">
        <h2>{{ detailRow.title }}</h2>
        <p style="color: #999; margin-bottom: 8px">{{ detailRow.author }} · {{ detailRow.category || '未分类' }} · {{ detailRow.publishedAt || '-' }}</p>
        <el-divider />
        <div style="margin-bottom: 12px; color: #555; background: #f8f9fa; padding: 12px; border-radius: 6px">{{ detailRow.summary || '无摘要' }}</div>
        <div style="white-space: pre-wrap; line-height: 1.8">{{ detailRow.content || '无内容' }}</div>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑资讯" width="600px">
      <el-form :model="editForm" label-position="top">
        <el-form-item label="标题"><el-input v-model="editForm.title" /></el-form-item>
        <el-row :gutter="16">
          <el-col :sm="12">
            <el-form-item label="分类"><el-input v-model="editForm.category" /></el-form-item>
          </el-col>
          <el-col :sm="12">
            <el-form-item label="作者"><el-input v-model="editForm.author" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" style="width: 100%">
            <el-option v-for="(label, value) in STATUS_LABEL" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要"><el-input v-model="editForm.summary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="editForm.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleEditSave">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>
