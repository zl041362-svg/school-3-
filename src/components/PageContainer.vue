<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '',
  },
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
