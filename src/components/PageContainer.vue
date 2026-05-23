<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' },
})
</script>

<template>
  <section class="page-container">
    <header class="page-header">
      <div>
        <h1 class="page-title">{{ title }}</h1>
        <p v-if="description" class="page-description">{{ description }}</p>
      </div>
      <div class="page-actions">
        <slot name="actions" />
      </div>
    </header>
    <div class="page-body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.page-container {
  padding: 28px;
  border-radius: var(--radius-lg);
  background: var(--color-paper-white);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--color-border-light);
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}
.page-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 800;
  color: var(--color-soil);
}
.page-description {
  margin: 6px 0 0;
  color: var(--color-text-soft);
  font-size: 14px;
}
.page-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
.page-body {
  border-top: 1px solid var(--color-border-light);
  padding-top: 20px;
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }
  .page-title {
    font-size: 20px;
  }
}
</style>
