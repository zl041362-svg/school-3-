<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductsApi } from '@/api/modules/products'
import { mockProducts } from '@/mocks/shop'
import { useCartStore } from '@/stores/cart'
import { resolveItems } from '@/utils/apiResponse'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'

const route = useRoute()
const cartStore = useCartStore()
const loading = ref(false)
const error = ref('')
const addingId = ref(null)
const total = ref(0)
const rows = ref([])

const categories = ['全部', '粮油', '水果', '蔬菜', '茶饮', '肉禽蛋', '水产']
const activeCategory = ref('全部')

const query = reactive({
  keyword: route.query.keyword || '',
  category: route.query.category || '',
  region: '',
  page: 1,
  pageSize: 12,
})

async function loadProducts() {
  loading.value = true
  error.value = ''
  try {
    const result = await getProductsApi(query)
    rows.value = resolveItems(result)
    total.value = result.total || rows.value.length
  } catch (err) {
    rows.value = mockProducts.filter((item) => {
      const keywordMatched = !query.keyword || item.name.includes(query.keyword)
      const categoryMatched =
        !query.category || query.category === '全部' || item.category?.includes(query.category)
      const regionMatched = !query.region || item.region.includes(query.region)
      return keywordMatched && categoryMatched && regionMatched
    })
    total.value = rows.value.length
    error.value = err.message || '商品列表加载失败，当前显示本地演示数据'
  } finally {
    loading.value = false
  }
}

function handleCategoryChange(cat) {
  activeCategory.value = cat
  query.category = cat === '全部' ? '' : cat
  query.page = 1
  loadProducts()
}

function handleSearch() {
  query.page = 1
  loadProducts()
}

function handlePageChange(page) {
  query.page = page
  loadProducts()
}

async function handleAddToCart(row) {
  if (!row || row.stock <= 0) {
    ElMessage.warning('当前商品缺货，暂时无法加入购物车')
    return
  }
  addingId.value = row.id
  try {
    await cartStore.addItem(row, 1)
    ElMessage.success(`${row.name} 已加入购物车`)
  } catch {
    ElMessage.error('加入购物车失败，请稍后重试')
  } finally {
    addingId.value = null
  }
}

onMounted(loadProducts)
</script>

<template>
  <div class="product-list-page">
    <!-- 搜索区 -->
    <div class="search-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索农产品名称、产地"
        style="max-width: 340px"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-input v-model="query.region" placeholder="产地筛选" style="max-width: 180px" clearable />
      <el-button type="primary" :loading="loading" @click="handleSearch">搜索</el-button>
    </div>

    <!-- 分类标签 -->
    <div class="category-bar">
      <button
        v-for="cat in categories"
        :key="cat"
        class="cat-btn"
        :class="{ active: activeCategory === cat }"
        @click="handleCategoryChange(cat)"
      >{{ cat }}</button>
    </div>

    <ErrorAlert v-if="error" :message="error" />

    <LoadingState v-if="loading" :rows="4" />
    <template v-else>
      <div v-if="rows.length" class="product-grid">
        <article
          v-for="(item, idx) in rows"
          :key="item.id"
          class="product-card"
          :style="{ animationDelay: idx * 0.06 + 's' }"
          @click="$router.push(`/products/${item.id}`)"
        >
          <div class="card-visual">
            <div class="card-img">{{ item.category === '水果' ? '🍎' : item.category === '蔬菜' ? '🥦' : item.category === '粮油' ? '🌾' : item.category === '茶饮' ? '🍵' : item.category === '肉禽蛋' ? '🥩' : item.category === '水产' ? '🐟' : '🌿' }}</div>
            <div v-if="item.stock <= 0" class="sold-out-badge">售罄</div>
          </div>
          <div class="card-body">
            <div class="card-head">
              <h3 class="card-name">{{ item.name }}</h3>
              <span class="card-origin">{{ item.region }}</span>
            </div>
            <p class="card-desc">{{ item.summary }}</p>
            <div class="card-foot">
              <div class="card-price">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ Number(item.price).toFixed(2) }}</span>
              </div>
              <div class="card-right">
                <el-tag v-if="item.stock <= 0" type="danger" size="small">已售罄</el-tag>
                <el-tag v-else size="small" type="success">库存 {{ item.stock }}</el-tag>
                <button
                  v-if="item.stock > 0"
                  class="add-btn"
                  :class="{ loading: addingId === item.id }"
                  :disabled="addingId === item.id"
                  @click.stop="handleAddToCart(item)"
                >
                  <svg v-if="addingId !== item.id" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 5v14M5 12h14"/></svg>
                  <span v-else class="mini-spinner"></span>
                </button>
              </div>
            </div>
          </div>
        </article>
      </div>
      <EmptyState v-else description="暂无符合条件的商品" />
    </template>

    <div v-if="total >= query.pageSize" class="pagination-wrap">
      <el-pagination
        :current-page="query.page"
        :page-size="query.pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.product-list-page {
  padding-bottom: 24px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.category-bar {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.cat-btn {
  padding: 5px 16px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;
}
.cat-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.cat-btn.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}

/* Grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

/* Card */
.product-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: border-color 0.15s;
}
.product-card:hover {
  border-color: var(--color-primary);
}
.card-visual {
  position: relative;
  height: 180px;
  background: #F7F7F7;
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-img {
  font-size: 64px;
}
.sold-out-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 2px 10px;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 12px;
}

.card-body { padding: 14px 16px 16px; }
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 4px;
}
.card-name {
  margin: 0;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text);
}
.card-origin {
  font-size: 12px;
  color: var(--color-text-hint);
  flex-shrink: 0;
}
.card-desc {
  margin: 0 0 12px;
  font-size: 12px;
  color: var(--color-text-hint);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-price {
  display: flex;
  align-items: baseline;
  gap: 2px;
}
.price-symbol {
  font-size: 14px;
  font-weight: 600;
  color: #E4393C;
}
.price-value {
  font-size: 22px;
  font-weight: 700;
  color: #E4393C;
}
.card-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  background: var(--color-surface);
  color: var(--color-text-hint);
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;
}
.add-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.add-btn.loading {
  pointer-events: none;
  opacity: 0.6;
}

.mini-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.pagination-wrap {
  text-align: center;
  margin-top: 28px;
}

@media (max-width: 1100px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 800px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 520px) {
  .product-grid { grid-template-columns: 1fr; }
}
</style>
