<template>
  <div class="center-screen">
    <div class="error-card">
      <div class="error-code">403</div>
      <h2 class="error-title">访问受限</h2>
      <p class="error-desc">您没有权限访问此页面</p>
      <button class="back-btn" @click="$router.push('/')">返回首页</button>
    </div>
  </div>
</template>

<style scoped>
.center-screen {
  min-height: 60vh;
  display: grid;
  place-items: center;
}
.error-card {
  text-align: center;
  padding: 48px;
}
.error-code {
  font-family: var(--font-display);
  font-size: 96px;
  font-weight: 900;
  color: var(--color-berry);
  line-height: 1;
  margin-bottom: 12px;
  opacity: 0.4;
}
.error-title {
  margin: 0 0 8px;
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 700;
  color: var(--color-soil);
}
.error-desc {
  margin: 0 0 24px;
  color: var(--color-text-muted);
  font-size: 14px;
}
.back-btn {
  padding: 10px 28px;
  border: none;
  background: var(--color-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}
.back-btn:hover { background: var(--color-primary-hover); }
</style>
