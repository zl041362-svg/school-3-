<template>
  <div class="auth-layout">
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
  display: grid;
  place-items: center;
  padding: 24px;
  background: var(--color-cream);
}

.auth-wrapper {
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
  width: 56px;
  height: 56px;
  margin-bottom: 12px;
  background: var(--color-primary);
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  border-radius: 8px;
  box-shadow: none;
}
.brand-name {
  margin: 0 0 6px;
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text);
}
.brand-tagline {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-hint);
  line-height: 1.6;
}

/* Card panel */
.auth-card {
  flex: 1;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
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
