<script setup>
defineProps({
  message: { type: String, default: '' },
  closable: { type: Boolean, default: false },
})
</script>

<template>
  <el-alert
    v-if="message"
    type="warning"
    show-icon
    :closable="closable"
    :title="message"
    style="margin-bottom: 16px"
  />
</template>
