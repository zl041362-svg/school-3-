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
  margin-bottom: 48px;
}
.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24px;
}
.section-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
  color: var(--color-soil);
}
.section-desc {
  margin: 4px 0 0;
  font-size: 14px;
  color: var(--color-text-muted);
}
.section-more {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--color-text-soft);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s var(--ease-smooth);
}
.section-more:hover {
  border-color: var(--color-terracotta);
  color: var(--color-terracotta);
}
.arrow {
  transition: transform 0.25s var(--ease-smooth);
}
.section-more:hover .arrow { transform: translateX(3px); }

/* Grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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
  height: 200px;
  background: linear-gradient(160deg, var(--color-cream-dark), var(--color-paper));
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-img {
  font-size: 72px;
  transition: transform 0.35s var(--ease-spring);
}
.product-card:hover .card-img {
  transform: scale(1.12);
}
.sold-out-badge {
  position: absolute;
  top: 14px;
  right: 14px;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  background: rgba(0,0,0,0.55);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.card-body { padding: 18px 20px 20px; }
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 6px;
}
.card-name {
  margin: 0;
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 700;
  color: var(--color-soil);
}
.card-origin {
  font-size: 12px;
  color: var(--color-text-muted);
  flex-shrink: 0;
}
.card-desc {
  margin: 0 0 16px;
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
  font-size: 14px;
  font-weight: 600;
  color: var(--color-berry);
}
.price-value {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 800;
  color: var(--color-berry);
}

.add-btn {
  width: 40px;
  height: 40px;
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
  width: 16px;
  height: 16px;
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

@media (max-width: 960px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 560px) {
  .product-grid { grid-template-columns: 1fr; }
}
</style>
