<script setup>
import { reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ROLE_HOME_MAP } from '@/constants/auth'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const formRef = ref(null)
const isDev = computed(() => import.meta.env.DEV)

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
    return
  } finally {
    loading.value = false
  }
  const redirect = route.query.redirect
  const path = typeof redirect === 'string' ? redirect : ROLE_HOME_MAP[authStore.role] || '/'
  router.push(path)
}
</script>

<template>
  <div>
    <h2 class="auth-title">账号登录</h2>
    <p class="auth-sub">登录后享受完整的三农平台服务</p>

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
      <button
        class="submit-btn"
        :disabled="loading"
        @click="handleLogin"
      >
        <span v-if="loading" class="btn-spinner"></span>
        <span v-else>登录</span>
      </button>
    </el-form>

    <div v-if="isDev" class="dev-hint">
      <div class="dev-hint-title">示例账号（种子数据）</div>
      <div>管理员：13800000000 / 12345678</div>
      <div>消费者：13900000000 / 12345678</div>
      <div>农户：13600000000 / 12345678</div>
    </div>

    <div class="auth-switch">
      还没有账号？
      <button class="link-btn" @click="$router.push('/auth/register')">立即注册</button>
    </div>
  </div>
</template>

<style scoped>
.auth-title {
  margin: 0 0 6px;
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 800;
  color: var(--color-soil);
}
.auth-sub {
  margin: 0 0 24px;
  font-size: 13px;
  color: var(--color-text-muted);
}

.submit-btn {
  width: 100%;
  margin-top: 4px;
  padding: 12px 0;
  border: none;
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}
.submit-btn:hover { background: var(--color-primary-hover); }
.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.btn-spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.dev-hint {
  margin-top: 20px;
  padding: 12px 16px;
  background: var(--color-cream-dark);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.8;
}
.dev-hint-title {
  font-weight: 600;
  color: var(--color-text-soft);
  margin-bottom: 4px;
}

.auth-switch {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: var(--color-text-muted);
}
.link-btn {
  padding: 0;
  border: none;
  background: none;
  color: var(--color-terracotta);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.link-btn:hover {
  text-decoration: underline;
}
</style>
