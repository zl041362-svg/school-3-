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
  padding-bottom: 32px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
}

.category-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}
.cat-btn {
  padding: 6px 18px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--color-border);
  background: var(--color-paper-white);
  color: var(--color-text-soft);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.cat-btn:hover {
  border-color: var(--color-terracotta-soft);
  color: var(--color-terracotta);
}
.cat-btn.active {
  background: var(--color-terracotta);
  border-color: var(--color-terracotta);
  color: #fff;
  font-weight: 600;
}

/* Grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

/* Card */
.product-card {
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.35s var(--ease-smooth);
  animation: fadeUp 0.5s var(--ease-out) both;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-terracotta-soft);
}
.card-visual {
  position: relative;
  height: 180px;
  background: linear-gradient(160deg, var(--color-cream-dark), var(--color-paper));
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-img {
  font-size: 64px;
  transition: transform 0.35s var(--ease-spring);
}
.product-card:hover .card-img {
  transform: scale(1.1);
}
.sold-out-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  background: rgba(0,0,0,0.55);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.card-body { padding: 16px 18px 18px; }
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 4px;
}
.card-name {
  margin: 0;
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 700;
  color: var(--color-soil);
}
.card-origin {
  font-size: 12px;
  color: var(--color-text-muted);
  flex-shrink: 0;
}
.card-desc {
  margin: 0 0 14px;
  font-size: 13px;
  color: var(--color-text-soft);
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
  font-size: 13px;
  font-weight: 600;
  color: var(--color-berry);
}
.price-value {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 800;
  color: var(--color-berry);
}
.card-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--radius-full);
  background: var(--color-terracotta);
  color: #fff;
  cursor: pointer;
  transition: all 0.3s var(--ease-smooth);
  box-shadow: 0 2px 8px rgba(193, 114, 69, 0.3);
}
.add-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 4px 16px rgba(193, 114, 69, 0.4);
}
.add-btn.loading {
  pointer-events: none;
  opacity: 0.7;
}

.mini-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.pagination-wrap {
  text-align: center;
  margin-top: 32px;
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
