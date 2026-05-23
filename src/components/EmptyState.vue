<script setup>
defineProps({
  description: { type: String, default: '暂无数据' },
})
</script>

<template>
  <el-empty :description="description">
    <template v-if="$slots.extra" #extra>
      <slot name="extra" />
    </template>
  </el-empty>
</template>
