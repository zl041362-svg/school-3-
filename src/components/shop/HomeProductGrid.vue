<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import LoadingState from '@/components/LoadingState.vue'
import EmptyState from '@/components/EmptyState.vue'

const props = defineProps({
  products: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
})

const router = useRouter()
const cartStore = useCartStore()
const addingId = ref(null)

async function handleAddToCart(item) {
  if (!item || item.stock <= 0) {
    ElMessage.warning('当前商品缺货，暂时无法加入购物车')
    return
  }
  addingId.value = item.id
  try {
    await cartStore.addItem(item, 1)
    ElMessage.success(`${item.name} 已加入购物车`)
  } catch (error) {
    ElMessage.error(error?.message || cartStore.error || '加入购物车失败，请稍后重试。')
  } finally {
    addingId.value = null
  }
}
</script>

<template>
  <section class="section">
    <div class="section-head">
      <div>
        <h2 class="section-title">时令推荐</h2>
        <p class="section-desc">当季鲜选，产地直发</p>
      </div>
      <button class="section-more" @click="router.push('/products')">
        查看全部 <span class="arrow">→</span>
      </button>
    </div>

    <LoadingState v-if="loading" :rows="3" />
    <div v-else class="product-grid">
      <article
        v-for="(item, idx) in products"
        :key="item.id"
        class="product-card"
        :style="{ animationDelay: idx * 0.08 + 's' }"
        @click="router.push(`/products/${item.id}`)"
      >
        <div class="card-visual">
          <div class="card-img">{{ item.category === '水果' ? '🍎' : item.category === '蔬菜' ? '🥦' : item.category === '粮油' ? '🌾' : item.category === '茶饮' ? '🍵' : '🌿' }}</div>
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
      </article>
      <EmptyState v-if="!products.length" description="暂无推荐商品" />
    </div>
  </section>
</template>

<style scoped>
.section {
  margin-bottom: 24px;
}
.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 12px;
}
.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}
.section-desc {
  margin: 1px 0 0;
  font-size: 12px;
  color: var(--color-text-hint);
}
.section-more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--color-text-hint);
  cursor: pointer;
  border: none;
  background: none;
}
.section-more:hover { color: var(--color-primary); }

/* Grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
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
  height: 140px;
  background: #F7F7F7;
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-img {
  font-size: 52px;
}
.sold-out-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 2px 10px;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 12px;
}

.card-body { padding: 10px 12px 12px; }
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 2px;
}
.card-name {
  margin: 0;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
}
.card-origin {
  font-size: 11px;
  color: var(--color-text-hint);
  flex-shrink: 0;
}
.card-desc {
  margin: 0 0 8px;
  font-size: 12px;
  color: var(--color-text-hint);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
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
  gap: 1px;
}
.price-symbol {
  font-size: 12px;
  font-weight: 600;
  color: #E4393C;
}
.price-value {
  font-size: 18px;
  font-weight: 700;
  color: #E4393C;
}

.add-btn {
  width: 32px;
  height: 32px;
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

@media (max-width: 1100px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 760px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 480px) {
  .product-grid { grid-template-columns: 1fr; }
}
</style>
