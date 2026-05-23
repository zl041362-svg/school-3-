<template>
  <div class="auth-layout">
    <div class="auth-bg">
      <div class="auth-orb orb-1"></div>
      <div class="auth-orb orb-2"></div>
      <div class="auth-orb orb-3"></div>
    </div>
    <div class="auth-wrapper">
      <div class="auth-brand">
        <div class="brand-glyph">丰</div>
        <h1 class="brand-name">智慧三农</h1>
        <p class="brand-tagline">农产品产销直连 · 从田间到餐桌</p>
      </div>
      <div class="auth-card">
        <RouterView />
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-layout {
  min-height: 100vh;
  position: relative;
  display: grid;
  place-items: center;
  padding: 24px;
  background: var(--color-cream);
}

/* Ambient background */
.auth-bg {
  position: fixed;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}
.auth-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
}
.orb-1 {
  width: 400px;
  height: 400px;
  background: rgba(212, 151, 59, 0.1);
  top: -100px;
  right: -80px;
}
.orb-2 {
  width: 320px;
  height: 320px;
  background: rgba(193, 114, 69, 0.08);
  bottom: -80px;
  left: -60px;
}
.orb-3 {
  width: 240px;
  height: 240px;
  background: rgba(139, 157, 107, 0.08);
  top: 50%;
  left: 40%;
}

.auth-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 56px;
  align-items: center;
  max-width: 920px;
  width: 100%;
}

/* Brand panel */
.auth-brand {
  flex: 1;
  text-align: center;
}
.brand-glyph {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, var(--color-terracotta), var(--color-amber));
  color: #fff;
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 900;
  border-radius: 18px;
  box-shadow: 0 8px 32px rgba(193, 114, 69, 0.3);
}
.brand-name {
  margin: 0 0 8px;
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 900;
  color: var(--color-soil);
  letter-spacing: 0.04em;
}
.brand-tagline {
  margin: 0;
  font-size: 15px;
  color: var(--color-text-soft);
  line-height: 1.6;
}

/* Card panel */
.auth-card {
  flex: 1;
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: 36px 32px;
  min-width: 400px;
}

@media (max-width: 760px) {
  .auth-wrapper {
    flex-direction: column;
    gap: 28px;
  }
  .auth-brand {
    display: none;
  }
  .auth-card {
    min-width: 0;
    width: 100%;
    padding: 28px 20px;
  }
}
</style>
