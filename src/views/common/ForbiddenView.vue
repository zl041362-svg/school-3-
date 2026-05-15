<template>
  <section class="center-screen">
    <el-result
      icon="warning"
      title="403"
      sub-title="您没有权限访问此页面"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>
  </section>
</template>
