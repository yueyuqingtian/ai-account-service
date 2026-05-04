<template>
  <section class="panel form">
    <h1>管理员登录</h1>
    <input v-model="username" placeholder="账号" />
    <input v-model="password" type="password" placeholder="密码" />
    <button @click="submit">登录</button>
    <p v-if="message" class="muted">{{ message }}</p>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '../stores/auth'

const auth = useAdminAuthStore()
const router = useRouter()
const username = ref('')
const password = ref('')
const message = ref('')

async function submit() {
  try {
    await auth.login(username.value, password.value)
    router.push('/')
  } catch (e: any) {
    message.value = e.message
  }
}
</script>

<style scoped>
.form {
  max-width: 420px;
  display: grid;
  gap: 12px;
}
</style>
