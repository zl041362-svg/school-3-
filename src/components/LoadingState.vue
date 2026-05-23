<script setup>
defineProps({
  rows: { type: Number, default: 5 },
})
</script>

<template>
  <el-skeleton :rows="rows" animated />
</template>
