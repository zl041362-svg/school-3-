<template>
  <div class="auth-layout">
    <div class="auth-box">
      <div class="auth-brand">
        <div class="auth-logo">🌾</div>
        <h1>智慧三农</h1>
        <p>农产品产销直连平台</p>
      </div>
      <div class="auth-panel">
        <RouterView />
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
}
.auth-box {
  display: flex;
  gap: 48px;
  align-items: center;
  max-width: 880px;
  width: 100%;
  padding: 0 24px;
}
.auth-brand {
  flex: 1;
  text-align: center;
  color: #2e7d32;
}
.auth-logo {
  font-size: 64px;
  margin-bottom: 12px;
}
.auth-brand h1 {
  font-size: 32px;
  margin: 0 0 8px;
  font-weight: 700;
}
.auth-brand p {
  margin: 0;
  opacity: 0.8;
  font-size: 15px;
}
.auth-panel {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);
  padding: 40px 36px;
  min-width: 360px;
}
@media (max-width: 700px) {
  .auth-box {
    flex-direction: column;
    gap: 24px;
  }
  .auth-brand {
    display: none;
  }
  .auth-panel {
    min-width: 0;
    padding: 28px 20px;
  }
}
</style>
