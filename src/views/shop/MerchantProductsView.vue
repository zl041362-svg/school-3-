<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import {
  createMerchantProductApi,
  deleteMerchantProductApi,
  getMerchantProductsApi,
  updateMerchantProductApi,
} from '@/api/modules/merchant'

const loading = ref(false)
const error = ref('')
const saving = ref(false)
const products = ref([])
const activeStatus = ref('全部')
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const form = reactive({
  name: '',
  category: '',
  region: '',
  price: '',
  stock: '',
  summary: '',
  description: '',
  spec: '',
  qualification: '',
})

const statusList = ['全部', '草稿', '待审核', '已上架', '已驳回', '已下架']
const categories = ['粮油', '水果', '蔬菜', '茶饮', '肉禽蛋', '水产']

const STATUS_LABEL = {
  draft: '草稿',
  pending: '待审核',
  published: '已上架',
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

function filteredProducts() {
  if (activeStatus.value === '全部') return products.value
  const map = {
    草稿: 'draft',
    待审核: 'pending',
    已上架: 'published',
    已驳回: 'rejected',
    已下架: 'offline',
  }
  return products.value.filter((p) => p.status === map[activeStatus.value])
}

async function loadProducts() {
  loading.value = true
  error.value = ''
  try {
    const result = await getMerchantProductsApi({ page: 1, pageSize: 100 })
    products.value = result.items || result.list || result.data || []
  } catch (err) {
    error.value = err?.message || '加载商品列表失败'
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  editingId.value = null
  Object.assign(form, {
    name: '',
    category: '',
    region: '',
    price: '',
    stock: '',
    summary: '',
    description: '',
    spec: '',
    qualification: '',
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name || '',
    category: row.category || '',
    region: row.region || '',
    price: row.price || '',
    stock: row.stock || '',
    summary: row.summary || '',
    description: row.description || '',
    spec: row.spec || '',
    qualification: row.qualification || '',
  })
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    await formRef.value?.validate()
    const payload = {
      name: form.name,
      category: form.category,
      region: form.region,
      price: Number(form.price),
      stock: Number(form.stock),
      summary: form.summary,
      description: form.description,
      spec: form.spec,
      qualification: form.qualification,
    }
    if (editingId.value) {
      await updateMerchantProductApi(editingId.value, payload)
      ElMessage.success('商品信息已更新')
    } else {
      await createMerchantProductApi(payload)
      ElMessage.success('商品已保存为草稿')
    }
    dialogVisible.value = false
    await loadProducts()
  } catch (err) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function handlePublish(row) {
  try {
    await updateMerchantProductApi(row.id, {
      name: row.name,
      category: row.category,
      region: row.region || '',
      price: row.price,
      stock: row.stock,
      summary: row.summary || '',
      description: row.description || '',
      spec: row.spec || '',
      qualification: row.qualification || '',
    })
    ElMessage.success('已提交审核，请等待平台审核')
    await loadProducts()
  } catch (err) {
    ElMessage.error(err?.message || '提交审核失败')
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除商品「${row.name}」？`, '提示', { type: 'warning' })
  try {
    await deleteMerchantProductApi(row.id)
    products.value = products.value.filter((p) => p.id !== row.id)
    ElMessage.success('已删除')
  } catch (err) {
    ElMessage.error(err?.message || '删除失败')
  }
}

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
}

onMounted(loadProducts)
</script>

<template>
  <PageContainer title="商品管理">
    <template #actions>
      <el-button type="primary" @click="handleCreate">+ 新增商品</el-button>
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

    <el-table v-loading="loading" :data="filteredProducts()" border>
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="region" label="产地" width="100" />
      <el-table-column label="价格(元)" width="110">
        <template #default="scope">￥{{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="STATUS_TAG[scope.row.status]">{{
            STATUS_LABEL[scope.row.status] || scope.row.status
          }}</el-tag>
        </template>
      </el-table-column>
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
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑商品' : '新增商品'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :sm="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :sm="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择" style="width: 100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :sm="12">
            <el-form-item label="产地">
              <el-input v-model="form.region" placeholder="如：黑龙江五常" />
            </el-form-item>
          </el-col>
          <el-col :sm="12">
            <el-form-item label="规格/包装">
              <el-input v-model="form.spec" placeholder="如：5kg/袋" />
            </el-form-item>
          </el-col>
          <el-col :sm="12">
            <el-form-item label="售价（元）" prop="price">
              <el-input-number
                v-model="form.price"
                :min="0.01"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :sm="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :sm="24">
            <el-form-item label="商品描述">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="3"
                placeholder="请介绍商品特点、种植方式等"
              />
            </el-form-item>
          </el-col>
          <el-col :sm="24">
            <el-form-item label="商品摘要">
              <el-input
                v-model="form.summary"
                placeholder="简短的商品摘要说明（选填）"
                maxlength="255"
              />
            </el-form-item>
          </el-col>
          <el-col :sm="24">
            <el-form-item label="资质证明">
              <el-input
                v-model="form.qualification"
                type="textarea"
                :rows="2"
                placeholder="如绿色食品认证、有机认证编号等"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存草稿</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>
