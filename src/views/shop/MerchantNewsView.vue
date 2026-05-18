<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import {
  createMerchantNewsApi,
  deleteMerchantNewsApi,
  getMerchantNewsApi,
  updateMerchantNewsApi,
} from '@/api/modules/merchant'

const loading = ref(false)
const error = ref('')
const saving = ref(false)
const newsList = ref([])
const activeStatus = ref('全部')
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const form = reactive({ title: '', category: '', summary: '', content: '' })

const statusList = ['全部', '草稿', '待审核', '已发布', '已驳回']
const categories = ['政策解读', '种植技术', '产业动态', '市场行情']

const STATUS_LABEL = {
  draft: '草稿',
  pending: '待审核',
  published: '已发布',
  rejected: '已驳回',
  offline: '已下架',
}
const STATUS_TAG = {
  draft: 'info',
  pending: 'warning',
  published: 'success',
  rejected: 'danger',
  offline: '',
}

function filteredNews() {
  if (activeStatus.value === '全部') return newsList.value
  const map = { 草稿: 'draft', 待审核: 'pending', 已发布: 'published', 已驳回: 'rejected' }
  return newsList.value.filter((n) => n.status === map[activeStatus.value])
}

async function loadNews() {
  loading.value = true
  error.value = ''
  try {
    const result = await getMerchantNewsApi({ page: 1, pageSize: 100 })
    newsList.value = result.items || result.list || result.data || []
  } catch (err) {
    error.value = err?.message || '加载资讯列表失败'
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  editingId.value = null
  Object.assign(form, { title: '', category: '', summary: '', content: '' })
  dialogVisible.value = true
}

function handleEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    title: row.title || '',
    category: row.category || '',
    summary: row.summary || '',
    content: row.content || '',
  })
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    await formRef.value?.validate()
    if (editingId.value) {
      await updateMerchantNewsApi(editingId.value, form)
      ElMessage.success('资讯已更新')
    } else {
      await createMerchantNewsApi(form)
      ElMessage.success('资讯已保存为草稿')
    }
    dialogVisible.value = false
    await loadNews()
  } catch (err) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function handlePublish(row) {
  try {
    await updateMerchantNewsApi(row.id, {
      title: row.title,
      category: row.category,
      summary: row.summary || '',
      content: row.content || '',
    })
    ElMessage.success('已提交审核，请等待平台审核')
    await loadNews()
  } catch (err) {
    ElMessage.error(err?.message || '提交审核失败')
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除资讯「${row.title}」？`, '提示', { type: 'warning' })
  try {
    await deleteMerchantNewsApi(row.id)
    newsList.value = newsList.value.filter((n) => n.id !== row.id)
    ElMessage.success('已删除')
  } catch (err) {
    ElMessage.error(err?.message || '删除失败')
  }
}

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }],
}

onMounted(loadNews)
</script>

<template>
  <PageContainer title="资讯管理">
    <template #actions>
      <el-button type="primary" @click="handleCreate">+ 新增资讯</el-button>
    </template>

    <el-alert
      v-if="error"
      type="warning"
      show-icon
      :closable="false"
      :title="error"
      style="margin-bottom: 16px"
    />

    <div style="margin-bottom: 16px">
      <el-tag
        v-for="s in statusList"
        :key="s"
        :type="activeStatus === s ? 'primary' : 'info'"
        :effect="activeStatus === s ? 'dark' : 'plain'"
        style="margin-right: 8px; cursor: pointer"
        @click="activeStatus = s"
        >{{ s }}</el-tag
      >
    </div>

    <el-table v-loading="loading" :data="filteredNews()" border>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="STATUS_TAG[scope.row.status]">{{
            STATUS_LABEL[scope.row.status] || scope.row.status
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishedAt" label="发布时间" width="130" />
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-space>
            <el-button text type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              v-if="scope.row.status === 'draft' || scope.row.status === 'rejected'"
              text
              type="success"
              @click="handlePublish(scope.row)"
              >提交审核</el-button
            >
            <el-button text type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </el-space>
        </template>
        </el-table-column>
      <template #empty>
        <el-empty description="暂无资讯，快去发布吧" />
      </template>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑资讯' : '新增资讯'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入资讯标题" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="2"
            placeholder="（选填）请输入内容摘要"
          />
        </el-form-item>
        <el-form-item label="正文内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入正文内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存草稿</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>
