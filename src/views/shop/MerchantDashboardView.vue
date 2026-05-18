<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { getMerchantDashboardApi, createMerchantProductApi, createMerchantNewsApi } from '@/api/modules/merchant'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const stats = ref({ productCount: 0, newsCount: 0, pendingOrderCount: 0 })

const publishDialog = ref(false)
const publishType = ref('product')
const saving = ref(false)
const productForm = reactive({ name: '', category: '', region: '', price: '', stock: '', summary: '' })
const newsForm = reactive({ title: '', category: '', summary: '', content: '' })
const productCategories = ['粮油', '水果', '蔬菜', '茶饮', '肉禽蛋', '水产']
const newsCategories = ['政策解读', '种植技术', '产业动态', '市场行情']

async function loadDashboard() {
  loading.value = true
  error.value = ''
  try {
    const result = await getMerchantDashboardApi()
    stats.value = result
  } catch (err) {
    error.value = err?.message || '加载仪表盘数据失败'
  } finally {
    loading.value = false
  }
}

function openPublish(type) {
  publishType.value = type
  if (type === 'product') {
    Object.assign(productForm, { name: '', category: '', region: '', price: '', stock: '', summary: '' })
  } else {
    Object.assign(newsForm, { title: '', category: '', summary: '', content: '' })
  }
  publishDialog.value = true
}

async function handleProductPublish() {
  if (!productForm.name || !productForm.category || !productForm.price || !productForm.stock) {
    ElMessage.warning('请填写商品名称、分类、价格和库存')
    return
  }
  saving.value = true
  try {
    await createMerchantProductApi({
      name: productForm.name,
      category: productForm.category,
      region: productForm.region,
      price: Number(productForm.price),
      stock: Number(productForm.stock),
      summary: productForm.summary,
      description: productForm.summary,
    })
    ElMessage.success('商品已提交，等待平台审核')
    publishDialog.value = false
    loadDashboard()
  } catch (err) {
    ElMessage.error(err?.message || '发布失败')
  } finally {
    saving.value = false
  }
}

async function handleNewsPublish() {
  if (!newsForm.title || !newsForm.category || !newsForm.content) {
    ElMessage.warning('请填写标题、分类和内容')
    return
  }
  saving.value = true
  try {
    await createMerchantNewsApi(newsForm)
    ElMessage.success('资讯已提交，等待平台审核')
    publishDialog.value = false
    loadDashboard()
  } catch (err) {
    ElMessage.error(err?.message || '发布失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadDashboard)
</script>

<template>
  <PageContainer title="商户后台">
    <el-alert
      v-if="error"
      type="warning"
      show-icon
      :closable="false"
      :title="error"
      style="margin-bottom: 16px"
    />

    <!-- 发布入口卡片 -->
    <el-row :gutter="16" style="margin-bottom: 24px">
      <el-col :md="12" :sm="24" style="margin-bottom: 12px">
        <div class="publish-card" @click="openPublish('product')">
          <div class="publish-icon">🌾</div>
          <div class="publish-text">
            <div class="publish-title">发布商品</div>
            <div class="publish-desc">将农产品上架到商城，审核通过后即可售卖</div>
          </div>
          <div class="publish-arrow">→</div>
        </div>
      </el-col>
      <el-col :md="12" :sm="24" style="margin-bottom: 12px">
        <div class="publish-card" @click="openPublish('news')">
          <div class="publish-icon">📰</div>
          <div class="publish-text">
            <div class="publish-title">发布资讯</div>
            <div class="publish-desc">分享农技经验与产业动态，吸引更多关注</div>
          </div>
          <div class="publish-arrow">→</div>
        </div>
      </el-col>
    </el-row>

    <!-- 统计数据 -->
    <el-row :gutter="16" style="margin-bottom: 24px">
      <el-col :md="8" :sm="24" style="margin-bottom: 12px">
        <el-card class="stat-card" shadow="hover" @click="router.push('/merchant/products')">
          <div v-loading="loading" class="stat-num">{{ stats.productCount }}</div>
          <div class="stat-label">我的商品</div>
        </el-card>
      </el-col>
      <el-col :md="8" :sm="24" style="margin-bottom: 12px">
        <el-card class="stat-card" shadow="hover" @click="router.push('/merchant/orders')">
          <div v-loading="loading" class="stat-num">{{ stats.pendingOrderCount }}</div>
          <div class="stat-label">待处理订单</div>
        </el-card>
      </el-col>
      <el-col :md="8" :sm="24" style="margin-bottom: 12px">
        <el-card class="stat-card" shadow="hover" @click="router.push('/merchant/news')">
          <div v-loading="loading" class="stat-num">{{ stats.newsCount }}</div>
          <div class="stat-label">我的资讯</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card>
      <template #header>全部功能</template>
      <el-space wrap size="large">
        <el-button type="primary" @click="router.push('/merchant/products')">🌾 商品管理</el-button>
        <el-button type="success" @click="router.push('/merchant/news')">📰 资讯管理</el-button>
        <el-button @click="router.push('/merchant/orders')">📦 订单管理</el-button>
        <el-button @click="router.push('/merchant/verify')">✅ 身份认证</el-button>
      </el-space>
    </el-card>

    <!-- 快捷发布弹窗 -->
    <el-dialog v-model="publishDialog" :title="publishType === 'product' ? '发布商品' : '发布资讯'" width="520px">
      <!-- 商品表单 -->
      <template v-if="publishType === 'product'">
        <el-form :model="productForm" label-position="top">
          <el-row :gutter="16">
            <el-col :span="14">
              <el-form-item label="商品名称" required>
                <el-input v-model="productForm.name" placeholder="请输入商品名称" />
              </el-form-item>
            </el-col>
            <el-col :span="10">
              <el-form-item label="分类" required>
                <el-select v-model="productForm.category" placeholder="请选择" style="width: 100%">
                  <el-option v-for="c in productCategories" :key="c" :label="c" :value="c" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="产地">
                <el-input v-model="productForm.region" placeholder="如：黑龙江" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="售价" required>
                <el-input-number v-model="productForm.price" :min="0.01" :precision="2" style="width: 100%" placeholder="元" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="库存" required>
                <el-input-number v-model="productForm.stock" :min="1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="商品摘要">
                <el-input v-model="productForm.summary" type="textarea" :rows="2" placeholder="简短说明商品特色" maxlength="255" show-word-limit />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </template>

      <!-- 资讯表单 -->
      <template v-else>
        <el-form :model="newsForm" label-position="top">
          <el-form-item label="标题" required>
            <el-input v-model="newsForm.title" placeholder="请输入资讯标题" />
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="分类" required>
                <el-select v-model="newsForm.category" placeholder="请选择" style="width: 100%">
                  <el-option v-for="c in newsCategories" :key="c" :label="c" :value="c" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="摘要">
            <el-input v-model="newsForm.summary" type="textarea" :rows="2" placeholder="（选填）" maxlength="255" show-word-limit />
          </el-form-item>
          <el-form-item label="正文内容" required>
            <el-input v-model="newsForm.content" type="textarea" :rows="5" placeholder="请输入资讯正文" />
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="publishDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="publishType === 'product' ? handleProductPublish() : handleNewsPublish()">
          提交审核
        </el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.stat-card {
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-num {
  font-size: 32px;
  font-weight: 700;
  color: var(--zhhs-primary, #2e7d32);
}
.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 6px;
}

.publish-card {
  background: linear-gradient(135deg, var(--zhhs-primary, #2e7d32), #43a047);
  border-radius: 12px;
  padding: 24px 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  color: #fff;
}
.publish-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(46, 125, 50, 0.3);
}
.publish-icon {
  font-size: 40px;
  flex-shrink: 0;
}
.publish-text {
  flex: 1;
  min-width: 0;
}
.publish-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
}
.publish-desc {
  font-size: 13px;
  opacity: 0.85;
}
.publish-arrow {
  font-size: 24px;
  flex-shrink: 0;
  opacity: 0.7;
}
</style>
