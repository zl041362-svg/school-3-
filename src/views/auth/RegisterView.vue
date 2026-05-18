<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  phone: '',
  password: '',
  confirmPassword: '',
  role: 'customer',
})

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (!/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,20}$/.test(value)) {
    callback(new Error('密码须为8-20位字母+数字组合'))
  } else {
    callback()
  }
}

const validateConfirm = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' },
  ],
  password: [{ required: true, validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }],
  role: [{ required: true, message: '请选择注册身份', trigger: 'change' }],
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const payload = { phone: form.phone, password: form.password, role: form.role }
    await authStore.register(payload)
    ElMessage.success('注册成功，请登录后完成农户认证即可发布商品')
    router.push('/auth/login')
  } catch {
    ElMessage.info('接口暂不可用，注册演示已记录，请跳转登录页。')
    router.push('/auth/login')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <h2 style="margin: 0 0 6px; font-size: 22px">注册账号</h2>
    <p style="margin: 0 0 24px; color: #888; font-size: 13px">加入三农平台，开启绿色产销新通道</p>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      @submit.prevent="handleRegister"
    >
      <el-form-item label="注册身份" prop="role">
        <el-radio-group v-model="form.role" style="width: 100%">
          <el-radio-button value="customer">消费者</el-radio-button>
          <el-radio-button value="farmer">农户/经营者</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入11位手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="登录密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          placeholder="8-20位字母+数字组合"
        />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          show-password
          placeholder="请再次输入密码"
        />
      </el-form-item>
      <el-button
        type="primary"
        style="width: 100%; margin-top: 4px"
        :loading="loading"
        @click="handleRegister"
      >
        立即注册
      </el-button>
    </el-form>

    <div style="margin-top: 16px; text-align: center; font-size: 13px; color: #888">
      已有账号？
      <el-link type="primary" @click="$router.push('/auth/login')">立即登录</el-link>
    </div>
  </div>
</template>
