<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { submitVerificationApi } from '@/api/modules/merchant'

const submitting = ref(false)
const submitted = ref(false)
const formRef = ref(null)
const form = reactive({
  realName: '',
  idNumber: '',
  businessNo: '',
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确（18位）', trigger: 'blur' },
  ],
  businessNo: [
    { required: true, message: '请输入营业执照/统一社会信用代码', trigger: 'blur' },
    { min: 18, max: 18, message: '统一社会信用代码须为18位', trigger: 'blur' },
  ],
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    await submitVerificationApi({
      realName: form.realName,
      idNumber: form.idNumber,
      businessNo: form.businessNo,
    })
    submitted.value = true
    ElMessage.success('认证申请已提交，审核结果将在1-3个工作日内通知您')
  } catch (err) {
    ElMessage.error(err?.message || '提交认证申请失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="verify-page">
    <div class="page-head">
      <h1 class="page-title">农户身份认证</h1>
      <p class="page-sub">请填写真实信息，审核通过后方可发布农产品及资讯</p>
    </div>

    <div v-if="submitted" class="success-card">
      <div class="success-icon">✓</div>
      <h2 class="success-title">认证申请已提交</h2>
      <p class="success-sub">平台将在1-3个工作日内完成审核，请耐心等待</p>
      <button class="back-btn" @click="$router.push('/merchant')">返回商户后台</button>
    </div>

    <template v-else>
      <div class="info-banner">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
        <span>请填写真实信息，审核通过后方可发布农产品及资讯</span>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="verify-form"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入法人/经营者真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="form.idNumber" placeholder="请输入18位身份证号码" maxlength="18" />
        </el-form-item>
        <el-form-item label="营业执照/统一社会信用代码" prop="businessNo">
          <el-input v-model="form.businessNo" placeholder="请输入统一社会信用代码（18位）或营业执照编号" maxlength="18" />
          <div class="form-hint">个体工商户/家庭农场/农业企业均可填写</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交认证申请</el-button>
          <el-button @click="$router.push('/merchant')">暂不认证</el-button>
        </el-form-item>
      </el-form>
    </template>
  </div>
</template>

<style scoped>
.verify-page {
  max-width: 640px;
  margin: 0 auto;
  padding-bottom: 32px;
}
.page-head {
  margin-bottom: 24px;
}
.page-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
  color: var(--color-soil);
}
.page-sub {
  margin: 4px 0 0;
  font-size: 14px;
  color: var(--color-text-muted);
}

.success-card {
  text-align: center;
  padding: 48px 32px;
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
}
.success-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  border-radius: 50%;
  background: var(--color-sage-soft);
  color: var(--color-success);
  font-size: 28px;
  font-weight: 800;
}
.success-title {
  margin: 0 0 8px;
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 800;
  color: var(--color-soil);
}
.success-sub {
  margin: 0 0 20px;
  color: var(--color-text-soft);
  font-size: 14px;
}
.back-btn {
  padding: 10px 28px;
  border: none;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-terracotta), var(--color-amber));
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s var(--ease-smooth);
}

.info-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  margin-bottom: 24px;
  background: var(--color-cream-dark);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  color: var(--color-text-soft);
  font-size: 13px;
}

.verify-form {
  background: var(--color-paper-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: 28px;
}
.form-hint {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 4px;
}
</style>
