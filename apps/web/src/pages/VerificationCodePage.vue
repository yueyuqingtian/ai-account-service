<template>
  <section class="verify-layout">
    <div class="panel query-panel">
      <span class="eyebrow">OpenAI 验证码</span>
      <h1>查询登录验证码</h1>
      <p class="muted">输入已兑换账号邮箱和账号密码，系统会校验归属后查询最近验证码。</p>
      <div class="form">
        <input v-model.trim="accountEmail" placeholder="已购买账号邮箱" />
        <input v-model="accountPassword" type="password" placeholder="已购买账号密码" />
        <button :disabled="loading" @click="queryCode">{{ loading ? '查询中...' : '查询验证码' }}</button>
      </div>
      <p v-if="message" class="muted">{{ message }}</p>
    </div>

    <div class="notice-card">
      <strong>查询规则</strong>
      <span>仅支持查询当前用户已兑换账号</span>
      <span>匹配邮件收件人、转发头和正文账号</span>
      <span>默认查找最近 30 分钟 6 位数字验证码</span>
    </div>

    <div v-if="result" class="result-card">
      <span class="status">查询成功</span>
      <h2>{{ result.code }}</h2>
      <p>账号：{{ result.accountEmail }}</p>
      <p>来源邮箱：{{ result.sourceMailbox }}</p>
      <p>邮件主题：{{ result.subject }}</p>
      <p>接收时间：{{ result.receivedAt }}</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const accountEmail = ref('')
const accountPassword = ref('')
const loading = ref(false)
const message = ref('')
const result = ref<any>(null)

async function queryCode() {
  if (!auth.token) {
    router.push('/login')
    return
  }
  loading.value = true
  message.value = ''
  result.value = null
  try {
    const res: any = await api.post('/api/verification-code/query', {
      accountEmail: accountEmail.value,
      accountPassword: accountPassword.value
    })
    result.value = res.data
  } catch (e: any) {
    message.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.verify-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 330px;
  gap: 20px;
}

.query-panel h1 {
  margin: 14px 0 10px;
  font-size: 40px;
}

.eyebrow {
  display: inline-flex;
  border-radius: 999px;
  padding: 6px 10px;
  background: #fef3c7;
  color: #92400e;
  font-size: 12px;
  font-weight: 900;
}

.form {
  display: grid;
  gap: 12px;
  max-width: 520px;
  margin-top: 22px;
}

.notice-card {
  display: grid;
  gap: 12px;
  height: fit-content;
  border-radius: 8px;
  background: linear-gradient(135deg, #0f172a 0%, #0f766e 100%);
  color: #fff;
  padding: 22px;
  box-shadow: 0 20px 52px rgba(15, 23, 42, .16);
}

.notice-card span {
  color: #d8eef0;
  line-height: 1.6;
}

.result-card {
  grid-column: 1 / -1;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  background: #f0fdf4;
  padding: 24px;
  box-shadow: 0 18px 48px rgba(15, 23, 42, .08);
}

.result-card h2 {
  margin: 12px 0 10px;
  color: #0f766e;
  font-size: 46px;
  letter-spacing: 0;
}

.result-card p {
  margin: 8px 0;
  color: #475569;
  overflow-wrap: anywhere;
}

@media (max-width: 820px) {
  .verify-layout {
    grid-template-columns: 1fr;
  }

  .query-panel h1 {
    font-size: 32px;
  }
}
</style>
