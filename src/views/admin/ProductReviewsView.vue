<script setup>
import { computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { useAdminModerationStore } from '@/stores/adminModeration'

const moderationStore = useAdminModerationStore()
const rows = computed(() => moderationStore.productReviews)
const pagination = computed(() => moderationStore.pagination('productReviews'))

function handlePageChangeReviews(page) {
  moderationStore.hydrateSection('productReviews', { page })
}

async function handleReview(row, approved) {
  let reason = ''

  if (!approved) {
    const result = await ElMessageBox.prompt('请输入驳回原因', '商品驳回', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPlaceholder: '原因将同步给农户',
    }).catch(() => null)

    if (!result) {
      return
    }

    reason = result.value
  } else {
    try {
      await ElMessageBox.confirm('确认通过该商品审核？', '审核通过', { type: 'warning', confirmButtonText: '通过', cancelButtonText: '取消' })
    } catch {
      return
    }
  }

  await moderationStore.reviewProduct(row.id, approved, reason)
  ElMessage.success(approved ? '商品审核已通过' : '商品审核已驳回')
}

onMounted(() => {
  moderationStore.hydrateSection('productReviews')
  moderationStore.hydrateSection('products')
})
</script>

<template>
  <PageContainer title="商品审核">
    <template #actions>
      <el-button @click="moderationStore.hydrateSection('productReviews')">刷新</el-button>
    </template>

    <el-alert
      v-if="moderationStore.error"
      type="warning"
      show-icon
      :closable="false"
      :title="moderationStore.error"
      style="margin-bottom: 16px"
    />

    <el-table v-loading="moderationStore.loadingMap.productReviews" :data="rows" border>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="product" label="商品名称" />
      <el-table-column prop="farmer" label="发布农户" width="160" />
      <el-table-column label="价格" width="120">
        <template #default="scope">￥{{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column prop="submittedAt" label="提交时间" width="180" />
      <el-table-column label="状态" width="120">
        <template #default="scope">
          <el-tag
            :type="
              scope.row.status === 'approved'
                ? 'success'
                : scope.row.status === 'rejected'
                  ? 'danger'
                  : 'warning'
            "
            >{{
              scope.row.status === 'approved'
                ? '已通过'
                : scope.row.status === 'rejected'
                  ? '已驳回'
                  : '待审核'
            }}</el-tag
          >
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-space>
            <el-button text type="primary" @click="handleReview(scope.row, true)">通过</el-button>
            <el-button text type="danger" @click="handleReview(scope.row, false)">驳回</el-button>
          </el-space>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无待审商品" />
      </template>
    </el-table>

    <div v-if="pagination.total > pagination.pageSize" style="text-align: center; margin-top: 16px">
      <el-pagination
        :current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        layout="prev, pager, next"
        @current-change="handlePageChangeReviews"
      />
    </div>
  </PageContainer>
</template>
