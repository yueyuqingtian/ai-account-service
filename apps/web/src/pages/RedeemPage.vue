<template>
  <section class="redeem-layout">
    <div class="panel redeem-panel">
      <span class="eyebrow">CDKey 兑换</span>
      <h1>兑换你的账号权益</h1>
      <p class="muted">输入购买后获得的 CDKey，平台会校验归属并分配账号资源。交付密码仅在安全状态下展示。</p>
      <div class="form-row">
        <input v-model.trim="cdkCode" placeholder="ABCD-EFGH-IJKL-MNOP" />
        <button :disabled="loading" @click="submit">{{ loading ? '兑换中...' : '立即兑换' }}</button>
      </div>
      <p v-if="message" class="muted">{{ message }}</p>
    </div>

    <div class="side-card">
      <strong>兑换后你将获得</strong>
      <span>账号邮箱与账号密码</span>
      <span>可在“验证码”页面查询 OpenAI 登录验证码</span>
      <span>兑换记录可在“我的权益”中追踪</span>
    </div>

    <div v-if="result" class="card result-card">
      <div>
        <span class="status">兑换成功</span>
        <h2>{{ result.account }}</h2>
        <p class="muted">请妥善保存账号信息，并尽快完成登录验证。</p>
      </div>
      <div class="secret-box">
        <span>账号密码</span>
        <strong>{{ showPassword ? result.password : '******' }}</strong>
      </div>
      <div class="row">
        <button class="secondary" @click="showPassword = !showPassword">{{ showPassword ? '隐藏密码' : '查看密码' }}</button>
        <RouterLink class="verify-link" to="/verification-code">查询登录验证码</RouterLink>
      </div>
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
const cdkCode = ref('')
const loading = ref(false)
const message = ref('')
const result = ref<any>(null)
const showPassword = ref(false)

async function submit() {
  if (!auth.token) {
    router.push('/login')
    return
  }
  loading.value = true
  result.value = null
  message.value = ''
  try {
    const res: any = await api.post('/api/redeem', { cdkCode: cdkCode.value })
    result.value = res.data
  } catch (e: any) {
    message.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.redeem-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 20px;
}

.redeem-panel h1 {
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

.form-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  margin-top: 22px;
}

.side-card {
  display: grid;
  gap: 12px;
  height: fit-content;
  border-radius: 8px;
  background: linear-gradient(135deg, #0f172a 0%, #0f766e 100%);
  color: #fff;
  padding: 22px;
  box-shadow: 0 20px 52px rgba(15, 23, 42, .16);
}

.side-card span {
  color: #d8eef0;
  line-height: 1.6;
}

.result-card {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px auto;
  gap: 18px;
  align-items: center;
}

.result-card h2 {
  margin: 12px 0 4px;
}

.secret-box {
  display: grid;
  gap: 6px;
  border-radius: 8px;
  background: #f1f5f9;
  padding: 14px;
}

.secret-box span {
  color: #64748b;
  font-size: 13px;
}

.verify-link {
  border-radius: 8px;
  padding: 11px 14px;
  background: #0f766e;
  color: #fff;
  font-weight: 900;
}

@media (max-width: 820px) {
  .redeem-layout, .result-card, .form-row {
    grid-template-columns: 1fr;
  }

  .redeem-panel h1 {
    font-size: 32px;
  }
}
</style>
