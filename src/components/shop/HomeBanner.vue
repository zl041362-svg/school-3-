<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <section class="hero">
    <div class="hero-bg">
      <div class="hero-orb orb-1"></div>
      <div class="hero-orb orb-2"></div>
      <div class="hero-orb orb-3"></div>
    </div>
    <div class="hero-content">
      <div class="hero-tag">产地直供</div>
      <h1 class="hero-title">
        <span class="title-line">从田间</span>
        <span class="title-line">到餐桌</span>
      </h1>
      <p class="hero-sub">
        每一颗果实都有它的来处。<br/>
        我们连接农户与你，让好物不再山高路远。
      </p>
      <div class="hero-actions">
        <button class="hero-btn primary" @click="router.push('/products')">
          逛市集
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </button>
        <button class="hero-btn ghost" @click="router.push('/auth/register')">
          成为农户
        </button>
      </div>
    </div>
    <div class="hero-visual">
      <div class="visual-card card-1">
        <span class="visual-emoji">🌾</span>
        <span class="visual-label">五常大米</span>
      </div>
      <div class="visual-card card-2">
        <span class="visual-emoji">🍎</span>
        <span class="visual-label">洛川苹果</span>
      </div>
      <div class="visual-card card-3">
        <span class="visual-emoji">🍵</span>
        <span class="visual-label">明前龙井</span>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* ═══ Hero ═══ */
.hero {
  position: relative;
  display: flex;
  align-items: center;
  gap: 48px;
  padding: 56px 52px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  margin-bottom: 48px;
  background: linear-gradient(135deg, #3C2A1C 0%, #5C3D2E 30%, #6B4F3A 60%, #4A3728 100%);
}

.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}
.hero-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
}
.orb-1 {
  width: 300px;
  height: 300px;
  background: rgba(212, 151, 59, 0.18);
  top: -80px;
  right: -40px;
}
.orb-2 {
  width: 240px;
  height: 240px;
  background: rgba(193, 114, 69, 0.12);
  bottom: -60px;
  left: 20%;
}
.orb-3 {
  width: 180px;
  height: 180px;
  background: rgba(139, 157, 107, 0.1);
  top: 40%;
  left: -40px;
}

/* Content */
.hero-content {
  position: relative;
  z-index: 1;
  flex: 1;
  min-width: 0;
}
.hero-tag {
  display: inline-block;
  padding: 5px 16px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.12);
  color: var(--color-amber-glow);
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 20px;
  letter-spacing: 0.06em;
  backdrop-filter: blur(4px);
}
.hero-title {
  margin: 0 0 20px;
  font-family: var(--font-display);
  font-size: 56px;
  font-weight: 900;
  line-height: 1.15;
  color: #fff;
  letter-spacing: 0.03em;
}
.title-line {
  display: block;
}
.title-line:last-child {
  color: var(--color-amber-glow);
}
.hero-sub {
  margin: 0 0 32px;
  font-size: 16px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.65);
  max-width: 360px;
}
.hero-actions {
  display: flex;
  gap: 14px;
  align-items: center;
}
.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 13px 28px;
  border-radius: var(--radius-full);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.3s var(--ease-smooth);
}
.hero-btn.primary {
  background: linear-gradient(135deg, var(--color-amber), #E8A840);
  color: var(--color-soil);
  box-shadow: 0 4px 20px rgba(212, 151, 59, 0.4);
}
.hero-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(212, 151, 59, 0.5);
}
.hero-btn.ghost {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  backdrop-filter: blur(4px);
}
.hero-btn.ghost:hover {
  background: rgba(255, 255, 255, 0.18);
}

/* Visual cards */
.hero-visual {
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  width: 280px;
  height: 280px;
}
.visual-card {
  position: absolute;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: var(--radius-md);
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.visual-emoji { font-size: 28px; }
.visual-label {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
}
.card-1 { top: 20px; right: 0; animation: floatA 4s ease-in-out infinite; }
.card-2 { top: 110px; left: 0; animation: floatB 5s ease-in-out infinite; }
.card-3 { bottom: 20px; right: 20px; animation: floatA 4.5s ease-in-out infinite 1s; }

@keyframes floatA {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
@keyframes floatB {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-6px) rotate(1deg); }
}

@media (max-width: 860px) {
  .hero {
    flex-direction: column;
    padding: 40px 28px;
    gap: 28px;
  }
  .hero-title { font-size: 38px; }
  .hero-sub { max-width: 100%; }
  .hero-visual { width: 100%; height: 100px; }
  .card-1 { top: 0; right: 10%; }
  .card-2 { top: 30px; left: 5%; }
  .card-3 { top: 10px; right: 40%; }
}
</style>
