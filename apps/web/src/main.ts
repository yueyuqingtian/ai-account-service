import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import HomePage from './pages/HomePage.vue'
import ProductPage from './pages/ProductPage.vue'
import LoginPage from './pages/LoginPage.vue'
import UserPage from './pages/UserPage.vue'
import RedeemPage from './pages/RedeemPage.vue'
import VerificationCodePage from './pages/VerificationCodePage.vue'
import './style.css'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomePage },
    { path: '/product/:id', component: ProductPage },
    { path: '/login', component: LoginPage },
    { path: '/user', component: UserPage },
    { path: '/redeem', component: RedeemPage },
    { path: '/verification-code', component: VerificationCodePage },
    { path: '/pay/result', component: UserPage }
  ]
})

createApp(App).use(createPinia()).use(router).mount('#app')
