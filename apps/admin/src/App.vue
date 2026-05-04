<template>
  <div class="shell">
    <aside>
      <h2>权益运营台</h2>
      <RouterLink to="/">仪表盘</RouterLink>
      <RouterLink to="/products">商品管理</RouterLink>
      <RouterLink to="/inventory">库存导入</RouterLink>
      <RouterLink to="/payment-config">支付配置</RouterLink>
      <RouterLink to="/email-config">邮箱配置</RouterLink>
      <RouterLink to="/records">订单与记录</RouterLink>
      <RouterLink v-if="!auth.token" to="/login">登录</RouterLink>
      <button v-else @click="auth.logout()">退出 {{ auth.username }}</button>
    </aside>
    <main>
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useAdminAuthStore } from './stores/auth'
const auth = useAdminAuthStore()
</script>
