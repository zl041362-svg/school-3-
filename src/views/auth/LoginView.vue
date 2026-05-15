<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ROLE_HOME_MAP } from '@/constants/auth'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  phone: '',
  password: '',
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度为8-20位', trigger: 'blur' },
  ],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.login({ phone: form.phone, password: form.password })
  } catch {
    ElMessage.error('登录失败，请检查账号密码。')
    loading.value = false
    return
  }
  const redirect = route.query.redirect
  const path = typeof redirect === 'string' ? redirect : ROLE_HOME_MAP[authStore.role] || '/'
  router.push(path)
}
</script>

<template>
  <div>
    <h2 style="margin: 0 0 6px; font-size: 22px">账号登录</h2>
    <p style="margin: 0 0 24px; color: #888; font-size: 13px">登录后享受完整的三农平台服务</p>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      @submit.prevent="handleLogin"
    >
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入11位手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="登录密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          placeholder="请输入密码（8-20位字母+数字）"
        />
      </el-form-item>
      <el-button
        type="primary"
        style="width: 100%; margin-top: 4px"
        :loading="loading"
        @click="handleLogin"
      >
        登录
      </el-button>
    </el-form>

    <div style="margin-top: 20px; padding: 12px 16px; background: #f5f7fa; border-radius: 8px; font-size: 12px; color: #888; line-height: 1.8">
      <div style="font-weight: 600; color: #666; margin-bottom: 4px">示例账号（种子数据）</div>
      <div>管理员：13800000000 / 12345678</div>
      <div>消费者：13900000000 / 12345678</div>
      <div>农户：13600000000 / 12345678</div>
    </div>

    <div style="margin-top: 16px; text-align: center; font-size: 13px; color: #888">
      还没有账号？
      <el-link type="primary" @click="$router.push('/auth/register')">立即注册</el-link>
    </div>
  </div>
</template>
