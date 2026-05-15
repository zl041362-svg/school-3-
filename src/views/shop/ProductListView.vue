<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductsApi } from '@/api/modules/products'
import { mockProducts } from '@/mocks/shop'
import { useCartStore } from '@/stores/cart'

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
    rows.value = result.items || result.list || result.data || []
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
    // error handled by cartStore
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
      <el-tag
        v-for="cat in categories"
        :key="cat"
        :type="activeCategory === cat ? 'primary' : 'info'"
        :effect="activeCategory === cat ? 'dark' : 'plain'"
        class="cat-tag"
        @click="handleCategoryChange(cat)"
        >{{ cat }}</el-tag
      >
    </div>

    <el-alert
      v-if="error"
      type="warning"
      show-icon
      :closable="false"
      :title="error"
      style="margin-bottom: 16px"
    />

    <el-skeleton v-if="loading" :rows="4" animated />
    <div v-else class="product-grid">
      <div
        v-for="item in rows"
        :key="item.id"
        class="product-card"
        @click="$router.push(`/products/${item.id}`)"
      >
        <div class="product-img">🌿</div>
        <div class="product-info">
          <div class="product-name">{{ item.name }}</div>
          <div class="product-meta">{{ item.region }} · {{ item.category || '农产品' }}</div>
          <div class="product-summary">{{ item.summary }}</div>
          <div class="product-footer">
            <span class="product-price">￥{{ item.price }}</span>
            <div>
              <el-tag v-if="item.stock <= 0" type="danger" size="small">已售罄</el-tag>
              <el-tag v-else type="success" size="small">库存 {{ item.stock }}</el-tag>
              <el-button
                size="small"
                type="primary"
                plain
                style="margin-left: 8px"
                :disabled="item.stock <= 0"
                :loading="addingId === item.id"
                @click.stop="handleAddToCart(item)"
                >加入购物车</el-button
              >
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!rows.length" description="暂无符合条件的商品" />
    </div>

    <div v-if="total > query.pageSize" style="text-align: center; margin-top: 24px">
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
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}
.category-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.cat-tag {
  cursor: pointer;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.product-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e8f5e9;
  overflow: hidden;
  cursor: pointer;
  transition:
    box-shadow 0.2s,
    transform 0.2s;
}
.product-card:hover {
  box-shadow: 0 4px 20px rgba(46, 125, 50, 0.12);
  transform: translateY(-2px);
}
.product-img {
  background: #e8f5e9;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
}
.product-info {
  padding: 12px;
}
.product-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}
.product-meta {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.product-summary {
  font-size: 12px;
  color: #666;
  margin-bottom: 10px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 4px;
}
.product-price {
  font-size: 16px;
  font-weight: 700;
  color: #e53935;
}
@media (max-width: 900px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
