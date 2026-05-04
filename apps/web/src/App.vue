<template>
  <div>
    <header class="topbar">
      <RouterLink class="brand" to="/">GPT Plus Service</RouterLink>
      <nav>
        <RouterLink to="/">商品</RouterLink>
        <RouterLink to="/redeem">兑换</RouterLink>
        <RouterLink to="/verification-code">验证码</RouterLink>
        <RouterLink to="/user">我的</RouterLink>
        <RouterLink v-if="!auth.token" to="/login">登录</RouterLink>
        <button v-else @click="auth.logout()">退出 {{ auth.username }}</button>
      </nav>
    </header>
    <main class="page">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from './stores/auth'
const auth = useAuthStore()
</script>
