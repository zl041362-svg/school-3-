<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ErrorAlert from '@/components/ErrorAlert.vue'
import {
  createMerchantNewsApi,
  deleteMerchantNewsApi,
  getMerchantNewsApi,
  updateMerchantNewsApi,
} from '@/api/modules/merchant'
import { resolveItems } from '@/utils/apiResponse'

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
    newsList.value = resolveItems(result)
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
  <div class="merchant-page">
    <div class="page-head">
      <div>
        <h1 class="page-title">资讯管理</h1>
        <p class="page-sub">发布三农资讯与动态</p>
      </div>
      <button class="add-btn" @click="handleCreate">+ 新增资讯</button>
    </div>

    <ErrorAlert v-if="error" :message="error" />

    <div class="filter-bar">
      <button
        v-for="s in statusList" :key="s"
        class="filter-btn" :class="{ active: activeStatus === s }"
        @click="activeStatus = s"
      >{{ s }}</button>
    </div>

    <el-table v-loading="loading" :data="filteredNews()" border>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="STATUS_TAG[scope.row.status]">{{ STATUS_LABEL[scope.row.status] || scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishedAt" label="发布时间" width="130" />
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-space>
            <el-button text type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              v-if="scope.row.status === 'draft' || scope.row.status === 'rejected'"
              text type="success"
              @click="handlePublish(scope.row)"
            >提交审核</el-button>
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
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="（选填）请输入内容摘要" />
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
  </div>
</template>

<style scoped>
.merchant-page {
  padding-bottom: 32px;
}
.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
}
.page-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
  color: var(--color-soil);
}
.page-sub {
  margin: 4px 0 0;
  font-size: 14px;
  color: var(--color-text-muted);
}
.add-btn {
  padding: 9px 22px;
  border: none;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-terracotta), var(--color-amber));
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s var(--ease-smooth);
  box-shadow: 0 2px 12px rgba(193, 114, 69, 0.3);
}
.add-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 18px rgba(193, 114, 69, 0.4);
}

.filter-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.filter-btn {
  padding: 5px 16px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--color-border);
  background: var(--color-paper-white);
  color: var(--color-text-soft);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.filter-btn:hover {
  border-color: var(--color-terracotta-soft);
  color: var(--color-terracotta);
}
.filter-btn.active {
  background: var(--color-terracotta);
  border-color: var(--color-terracotta);
  color: #fff;
  font-weight: 600;
}
</style>
