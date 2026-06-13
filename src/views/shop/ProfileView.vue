<script setup>
import { onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useAddressStore } from '@/stores/address'
import { readJsonStorage } from '@/utils/storage'
import UserInfoHeader from '@/components/shop/UserInfoHeader.vue'
import ProfileMenuGrid from '@/components/shop/ProfileMenuGrid.vue'
import RecentViewsRow from '@/components/shop/RecentViewsRow.vue'
import AddressManager from '@/components/shop/AddressManager.vue'

const authStore = useAuthStore()
const addressStore = useAddressStore()
const addressManagerRef = ref(null)
const recentViews = ref([])

onMounted(() => {
  addressStore.hydrate()
  recentViews.value = readJsonStorage('ZHHS_RECENT_VIEWS', [])
})
</script>

<template>
  <div class="profile-page">
    <div class="page-head">
      <h1 class="page-title">个人中心</h1>
      <button class="add-address-btn" @click="addressManagerRef?.handleCreate()">新增收货地址</button>
    </div>

    <UserInfoHeader>
      <template #meta>
        <span>·</span>
        <span>{{ addressStore.count }} 个收货地址</span>
      </template>
    </UserInfoHeader>

    <ProfileMenuGrid />
    <RecentViewsRow :views="recentViews" />
    <AddressManager ref="addressManagerRef" />
  </div>
</template>

<style scoped>
.profile-page {
  padding-bottom: 32px;
}
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text);
}
.add-address-btn {
  padding: 8px 20px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}
.add-address-btn:hover { background: var(--color-primary-hover); }
</style>
