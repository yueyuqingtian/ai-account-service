<template>
  <section class="auth-page">
    <div class="auth-copy">
      <span class="eyebrow">账号权益服务</span>
      <h1>{{ mode === 'login' ? '欢迎回来' : '创建你的购买账户' }}</h1>
    </div>
    <section class="panel form">
      <h2>{{ mode === 'login' ? '用户登录' : '用户注册' }}</h2>
      <input v-model="username" placeholder="用户名" />
      <input v-model="password" type="password" placeholder="密码" />
      <input v-if="mode === 'register'" v-model="email" placeholder="邮箱" />
      <div v-if="mode === 'register'" class="code-row">
        <input v-model="verifyCode" placeholder="邮箱验证码" />
        <button class="secondary" :disabled="codeSending" @click="sendCode">{{ codeSending ? '发送中' : '发送验证码' }}</button>
      </div>
      <button @click="submit">{{ mode === 'login' ? '登录' : '注册并登录' }}</button>
      <button class="secondary" @click="mode = mode === 'login' ? 'register' : 'login'">
        {{ mode === 'login' ? '还没有账号，去注册' : '已有账号，去登录' }}
      </button>
      <p v-if="message" class="muted">{{ message }}</p>
    </section>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const mode = ref<'login' | 'register'>('login')
const username = ref('')
const password = ref('')
const email = ref('')
const verifyCode = ref('')
const codeSending = ref(false)
const message = ref('')

async function sendCode() {
  codeSending.value = true
  message.value = ''
  try {
    await auth.sendRegisterCode(email.value)
    message.value = '验证码已发送'
  } catch (e: any) {
    message.value = e.message
  } finally {
    codeSending.value = false
  }
}

async function submit() {
  try {
    if (mode.value === 'login') await auth.login(username.value, password.value)
    else await auth.register(username.value, password.value, email.value, verifyCode.value)
    router.push('/')
  } catch (e: any) {
    message.value = e.message
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 130px);
  display: grid;
  grid-template-columns: minmax(0, 1fr) 430px;
  align-items: center;
  gap: 34px;
}

.auth-copy h1 {
  margin: 16px 0;
  font-size: 48px;
}

.auth-copy p {
  max-width: 560px;
  color: #637469;
  line-height: 1.8;
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

.mini-list {
  display: grid;
  gap: 10px;
  margin-top: 22px;
}

.mini-list span {
  width: fit-content;
  border: 1px solid #dce7df;
  border-radius: 8px;
  background: #fff;
  padding: 10px 12px;
  color: #0f766e;
  font-weight: 800;
}

.form {
  display: grid;
  gap: 12px;
}

.form h2 {
  margin: 0 0 8px;
}

.code-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 130px;
  gap: 10px;
}

@media (max-width: 820px) {
  .auth-page {
    grid-template-columns: 1fr;
    align-items: start;
  }

  .auth-copy h1 {
    font-size: 36px;
  }

  .code-row {
    grid-template-columns: 1fr;
  }
}
</style>
