<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ErrorAlert from '@/components/ErrorAlert.vue'
import LoadingState from '@/components/LoadingState.vue'
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
  <div class="merchant-page">
    <div class="page-head">
      <div>
        <h1 class="page-title">商户后台</h1>
        <p class="page-sub">管理您的商品、资讯与订单</p>
      </div>
    </div>

    <ErrorAlert v-if="error" :message="error" />

    <!-- 发布入口 -->
    <div class="publish-row">
      <div class="publish-card" @click="openPublish('product')">
        <div class="publish-icon">🌾</div>
        <div class="publish-text">
          <div class="publish-title">发布商品</div>
          <div class="publish-desc">将农产品上架到商城，审核通过后即可售卖</div>
        </div>
        <span class="publish-arrow">→</span>
      </div>
      <div class="publish-card news" @click="openPublish('news')">
        <div class="publish-icon">📰</div>
        <div class="publish-text">
          <div class="publish-title">发布资讯</div>
          <div class="publish-desc">分享农技经验与产业动态，吸引更多关注</div>
        </div>
        <span class="publish-arrow">→</span>
      </div>
    </div>

    <!-- 统计数据 -->
    <LoadingState v-if="loading" :rows="2" />
    <div v-else class="stats-row">
      <div class="stat-card" @click="router.push('/merchant/products')">
        <div class="stat-num">{{ stats.productCount }}</div>
        <div class="stat-label">我的商品</div>
      </div>
      <div class="stat-card" @click="router.push('/merchant/orders')">
        <div class="stat-num">{{ stats.pendingOrderCount }}</div>
        <div class="stat-label">待处理订单</div>
      </div>
      <div class="stat-card" @click="router.push('/merchant/news')">
        <div class="stat-num">{{ stats.newsCount }}</div>
        <div class="stat-label">我的资讯</div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-links">
      <h3 class="section-title">全部功能</h3>
      <div class="link-grid">
        <button class="link-btn" @click="router.push('/merchant/products')">🌾 商品管理</button>
        <button class="link-btn" @click="router.push('/merchant/news')">📰 资讯管理</button>
        <button class="link-btn" @click="router.push('/merchant/orders')">📦 订单管理</button>
        <button class="link-btn" @click="router.push('/merchant/verify')">✅ 身份认证</button>
      </div>
    </div>

    <!-- 快捷发布弹窗 -->
    <el-dialog v-model="publishDialog" :title="publishType === 'product' ? '发布商品' : '发布资讯'" width="520px">
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
  </div>
</template>

<style scoped>
.merchant-page {
  padding-bottom: 32px;
}
.page-head {
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

/* Publish cards */
.publish-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}
.publish-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px 28px;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.35s var(--ease-smooth);
  background: linear-gradient(135deg, var(--color-terracotta), #B55D38);
  color: #fff;
}
.publish-card.news {
  background: linear-gradient(135deg, var(--color-leaf), #4F6B2E);
}
.publish-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 32px rgba(74, 55, 40, 0.25);
}
.publish-icon { font-size: 40px; flex-shrink: 0; }
.publish-text { flex: 1; min-width: 0; }
.publish-title { font-size: 18px; font-weight: 700; margin-bottom: 4px; }
.publish-desc { font-size: 13px; opacity: 0.85; }
.publish-arrow { font-size: 24px; flex-shrink: 0; opacity: 0.7; }

/* Stats */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}
.stat-card {
  text-align: center;
  padding: 24px;
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.35s var(--ease-smooth);
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-terracotta-soft);
}
.stat-num {
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 900;
  color: var(--color-terracotta);
}
.stat-label {
  font-size: 14px;
  color: var(--color-text-muted);
  margin-top: 6px;
}

/* Quick links */
.quick-links {
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: 24px;
}
.section-title {
  margin: 0 0 16px;
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--color-soil);
}
.link-grid {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.link-btn {
  padding: 10px 22px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--color-border);
  background: var(--color-paper-white);
  color: var(--color-text-soft);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.link-btn:hover {
  border-color: var(--color-terracotta);
  color: var(--color-terracotta);
}

@media (max-width: 700px) {
  .publish-row { grid-template-columns: 1fr; }
  .stats-row { grid-template-columns: repeat(2, 1fr); }
}
</style>
