<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'
import { updateAdminProductApi, updateAdminProductStatusApi } from '@/api/modules/admin'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.products)
const pagination = computed(() => moderationStore.pagination('products'))
const searchKeyword = ref('')
const activeStatus = ref('全部')

const statusOptions = ['全部', '草稿', '待审核', '已上架', '已驳回', '已下架']
const STATUS_MAP = { 草稿: 'draft', 待审核: 'pending', 已上架: 'published', 已驳回: 'rejected', 已下架: 'offline' }
const STATUS_LABEL = { draft: '草稿', pending: '待审核', published: '已上架', rejected: '已驳回', offline: '已下架' }
const STATUS_TAG = { draft: 'info', pending: 'warning', published: 'success', rejected: 'danger', offline: '' }

const filteredRows = computed(() => {
  let list = rows.value
  if (searchKeyword.value) {
    list = list.filter((p) => (p.name || '').includes(searchKeyword.value) || (p.farmer || '').includes(searchKeyword.value))
  }
  if (activeStatus.value !== '全部') {
    list = list.filter((p) => p.status === STATUS_MAP[activeStatus.value])
  }
  return list
})

const detailVisible = ref(false)
const detailRow = ref(null)
const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({ name: '', category: '', region: '', price: '', stock: '', summary: '', description: '', spec: '', qualification: '', farmer: '', status: '' })
const editingId = ref(null)

function handlePageChange(page) {
  moderationStore.hydrateSection('products', { page })
}

function showDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(editForm, {
    name: row.name || '',
    category: row.category || '',
    region: row.region || '',
    price: row.price || '',
    stock: row.stock || '',
    summary: row.summary || '',
    description: row.description || '',
    spec: row.spec || '',
    qualification: row.qualification || '',
    farmer: row.farmer || '',
    status: row.status || '',
  })
  editVisible.value = true
}

async function handleEditSave() {
  saving.value = true
  try {
    await updateAdminProductApi(editingId.value, {
      name: editForm.name,
      category: editForm.category,
      region: editForm.region,
      price: Number(editForm.price),
      stock: Number(editForm.stock),
      summary: editForm.summary,
      description: editForm.description,
      spec: editForm.spec,
      qualification: editForm.qualification,
      farmer: editForm.farmer,
      status: editForm.status,
    })
    ElMessage.success('商品信息已更新')
    editVisible.value = false
    moderationStore.hydrateSection('products')
  } catch (err) {
    ElMessage.error(err?.message || '更新失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const nextStatus = row.status === 'published' ? 'offline' : 'published'
  const label = nextStatus === 'published' ? '上架' : '下架'
  try {
    await updateAdminProductStatusApi(row.id, { status: nextStatus })
    ElMessage.success(`商品已${label}`)
    moderationStore.hydrateSection('products')
  } catch (err) {
    ElMessage.error(err?.message || `商品${label}失败`)
  }
}

onMounted(() => {
  moderationStore.hydrateSection('products')
})
</script>

<template>
  <PageContainer title="商品管理">
    <template #actions>
      <el-button @click="moderationStore.hydrateSection('products')">刷新</el-button>
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
      <el-input v-model="searchKeyword" placeholder="搜索商品名或农户" style="max-width: 260px" clearable />
      <el-tag
        v-for="s in statusOptions"
        :key="s"
        :type="activeStatus === s ? 'primary' : 'info'"
        :effect="activeStatus === s ? 'dark' : 'plain'"
        style="cursor: pointer"
        @click="activeStatus = s"
      >{{ s }}</el-tag>
    </div>

    <el-table v-loading="moderationStore.loadingMap?.products" :data="filteredRows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="farmer" label="发布农户" width="140" />
      <el-table-column label="价格" width="100">
        <template #default="scope">￥{{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
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
          <el-button v-else-if="scope.row.status === 'offline'" text type="success" @click="toggleStatus(scope.row)">上架</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无商品数据" />
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

    <el-dialog v-model="detailVisible" title="商品详情" width="560px">
      <el-descriptions v-if="detailRow" border :column="2">
        <el-descriptions-item label="商品名称" :span="2">{{ detailRow.name }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ detailRow.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产地">{{ detailRow.region || '-' }}</el-descriptions-item>
        <el-descriptions-item label="价格">￥{{ detailRow.price }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ detailRow.stock }}</el-descriptions-item>
        <el-descriptions-item label="农户">{{ detailRow.farmer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ STATUS_LABEL[detailRow.status] || detailRow.status }}</el-descriptions-item>
        <el-descriptions-item label="规格" :span="2">{{ detailRow.spec || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资质" :span="2">{{ detailRow.qualification || '-' }}</el-descriptions-item>
        <el-descriptions-item label="摘要" :span="2">{{ detailRow.summary || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detailRow.description || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑商品" width="560px">
      <el-form :model="editForm" label-position="top">
        <el-row :gutter="16">
          <el-col :sm="12">
            <el-form-item label="商品名称"><el-input v-model="editForm.name" /></el-form-item>
          </el-col>
          <el-col :sm="12">
            <el-form-item label="分类"><el-input v-model="editForm.category" /></el-form-item>
          </el-col>
          <el-col :sm="12">
            <el-form-item label="产地"><el-input v-model="editForm.region" /></el-form-item>
          </el-col>
          <el-col :sm="12">
            <el-form-item label="农户"><el-input v-model="editForm.farmer" /></el-form-item>
          </el-col>
          <el-col :sm="12">
            <el-form-item label="价格（元）"><el-input-number v-model="editForm.price" :min="0" :precision="2" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :sm="12">
            <el-form-item label="库存"><el-input-number v-model="editForm.stock" :min="0" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :sm="12">
            <el-form-item label="状态">
              <el-select v-model="editForm.status" style="width: 100%">
                <el-option v-for="(label, value) in STATUS_LABEL" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :sm="12">
            <el-form-item label="规格"><el-input v-model="editForm.spec" /></el-form-item>
          </el-col>
          <el-col :sm="24">
            <el-form-item label="摘要"><el-input v-model="editForm.summary" type="textarea" :rows="2" /></el-form-item>
          </el-col>
          <el-col :sm="24">
            <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="3" /></el-form-item>
          </el-col>
          <el-col :sm="24">
            <el-form-item label="资质"><el-input v-model="editForm.qualification" type="textarea" :rows="2" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleEditSave">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>
