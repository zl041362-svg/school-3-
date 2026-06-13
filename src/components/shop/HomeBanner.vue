<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <section class="hero">
    <div class="hero-content">
      <h1 class="hero-title">从田间到餐桌</h1>
      <p class="hero-sub">每一颗果实都有它的来处。我们连接农户与你，让好物不再山高路远。</p>
      <div class="hero-actions">
        <button class="hero-btn primary" @click="router.push('/products')">
          逛市集
        </button>
        <button class="hero-btn ghost" @click="router.push('/auth/register')">
          成为农户
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  display: flex;
  align-items: center;
  padding: 36px 40px;
  margin-bottom: 24px;
  background: #3C2A1C;
}

.hero-content {
  flex: 1;
  min-width: 0;
}

.hero-title {
  margin: 0 0 10px;
  font-size: 32px;
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1.2;
}

.hero-sub {
  margin: 0 0 18px;
  font-size: 14px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.55);
  max-width: 420px;
}

.hero-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 22px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: opacity 0.2s;
}

.hero-btn.primary {
  background: #FFFFFF;
  color: #222222;
}
.hero-btn.primary:hover { opacity: 0.88; }

.hero-btn.ghost {
  background: transparent;
  color: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.25);
}
.hero-btn.ghost:hover {
  color: #FFFFFF;
  border-color: rgba(255, 255, 255, 0.45);
}

@media (max-width: 860px) {
  .hero {
    padding: 28px 20px;
  }
  .hero-title { font-size: 26px; }
  .hero-sub { max-width: 100%; }
}
</style>
