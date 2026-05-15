<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
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
    { min: 15, message: '统一社会信用代码至少15位', trigger: 'blur' },
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
  <PageContainer title="农户身份认证">
    <el-result
      v-if="submitted"
      icon="success"
      title="认证申请已提交"
      sub-title="平台将在1-3个工作日内完成审核，请耐心等待"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/merchant')">返回商户后台</el-button>
      </template>
    </el-result>

    <template v-else>
      <el-alert
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 20px"
        title="请填写真实信息，审核通过后方可发布农产品及资讯"
      />
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        style="max-width: 600px"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入法人/经营者真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="form.idNumber" placeholder="请输入18位身份证号码" maxlength="18" />
        </el-form-item>
        <el-form-item label="营业执照/统一社会信用代码" prop="businessNo">
          <el-input
            v-model="form.businessNo"
            placeholder="请输入统一社会信用代码（18位）或营业执照编号"
            maxlength="18"
          />
          <div style="font-size: 12px; color: #999; margin-top: 4px">
            个体工商户/家庭农场/农业企业均可填写
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit"
            >提交认证申请</el-button
          >
          <el-button @click="$router.push('/merchant')">暂不认证</el-button>
        </el-form-item>
      </el-form>
    </template>
  </PageContainer>
</template>
