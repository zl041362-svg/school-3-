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
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
  color: var(--color-soil);
}
.add-address-btn {
  padding: 9px 22px;
  border: none;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-terracotta), var(--color-amber));
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s var(--ease-smooth);
  box-shadow: 0 2px 12px rgba(193, 114, 69, 0.3);
}
.add-address-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 18px rgba(193, 114, 69, 0.4);
}
</style>
